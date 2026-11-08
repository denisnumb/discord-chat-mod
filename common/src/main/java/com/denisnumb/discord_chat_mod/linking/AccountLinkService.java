package com.denisnumb.discord_chat_mod.linking;

import com.denisnumb.discord_chat_mod.config.configs.AccountLinkingConfig;
import com.denisnumb.discord_chat_mod.linking.model.*;
import com.denisnumb.discord_chat_mod.locale.MinecraftLocaleProvider;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.denisnumb.discord_chat_mod.utils.MinecraftUtils.logErrorToServer;


public final class AccountLinkService {
    private AccountLinkService() {}

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FILE_NAME = "discord_account_links.json";
    private static final Object LOCK = new Object();
    private static final LinkCodeManager CODES = new LinkCodeManager();
    private static final List<AccountLinkListener> LISTENERS = new CopyOnWriteArrayList<>();

    private static volatile AccountLinkStorage storage;

    public record RedeemResult(Status status, @Nullable LinkResult linkResult) {
        public enum Status { INVALID_CODE, RATE_LIMITED, PROCESSED }
    }

    private sealed interface LinkEvent permits Linked, Unlinked {}
    private record Linked(AccountLink link) implements LinkEvent {}
    private record Unlinked(AccountLink link, UnlinkReason reason) implements LinkEvent {}

    // =================================================================================================================
    //                                                  LIFECYCLE
    // =================================================================================================================

    public static void load(MinecraftServer server) {
        storage = AccountLinkStorage.load(server.getWorldPath(LevelResource.ROOT).resolve(FILE_NAME));
    }

    public static void unload() {
        storage = null;
        CODES.clear();
    }

    public static boolean isLoaded() {
        return storage != null;
    }

    public static void addListener(AccountLinkListener listener) {
        LISTENERS.add(listener);
    }

    // =================================================================================================================
    //                                                   QUERIES
    // =================================================================================================================

    public static Optional<AccountLink> getByUuid(UUID uuid) {
        return storage == null ? Optional.empty() : storage.get(uuid);
    }

    public static Optional<AccountLink> getByName(String name) {
        return storage == null ? Optional.empty() : storage.getByName(name);
    }

    public static List<AccountLink> getByDiscordId(String discordId) {
        return storage == null ? List.of() : storage.getByDiscordId(discordId);
    }

    public static List<AccountLink> getAll() {
        return storage == null ? List.of() : storage.getAll();
    }

    public static Set<String> getAllDiscordIds() {
        return storage == null ? Set.of() : storage.getAllDiscordIds();
    }

    public static boolean isLinked(String discordId) {
        return !getByDiscordId(discordId).isEmpty();
    }

    // =================================================================================================================
    //                                                    CODES
    // =================================================================================================================

    public static LinkCodeManager.PendingCode getOrCreateCode(UUID uuid, String playerName) {
        return CODES.getOrCreate(uuid, playerName, Duration.ofSeconds(AccountLinkingConfig.LINK_CODE_TTL_SECONDS.get()));
    }

    public static RedeemResult redeemCode(String discordId, String rawCode) {
        if (CODES.isRateLimited(discordId))
            return new RedeemResult(RedeemResult.Status.RATE_LIMITED, null);

        Optional<LinkCodeManager.PendingCode> pending = CODES.find(rawCode);
        if (pending.isEmpty()) {
            CODES.registerFailedAttempt(discordId);
            return new RedeemResult(RedeemResult.Status.INVALID_CODE, null);
        }

        LinkCodeManager.PendingCode code = pending.get();
        LinkResult result = link(code.uuid(), code.playerName(), discordId);
        if (result.isSuccess()) {
            CODES.invalidate(code.uuid());
            CODES.clearFailedAttempts(discordId);
        }

        return new RedeemResult(RedeemResult.Status.PROCESSED, result);
    }

    // =================================================================================================================
    //                                                   CHANGES
    // =================================================================================================================

    private static LinkResult link(UUID uuid, String playerName, String discordId) {
        List<LinkEvent> events = new ArrayList<>();
        LinkResult result;

        synchronized (LOCK) {
            AccountLinkStorage s = storage;
            if (s == null)
                return LinkResult.of(LinkResult.Status.UNAVAILABLE);

            Optional<AccountLink> existing = s.get(uuid);
            if (existing.isPresent() && existing.get().discordId().equals(discordId)) {
                AccountLink link = existing.get();
                if (!link.name().equals(playerName)) {
                    link = link.withName(playerName);
                    s.put(link);
                }

                return new LinkResult(LinkResult.Status.ALREADY_LINKED, link);
            }

            if (s.getByDiscordId(discordId).size() >= AccountLinkingConfig.MAX_MINECRAFT_ACCOUNTS_PER_DISCORD_USER.get())
                return LinkResult.of(LinkResult.Status.LIMIT_REACHED);

            AccountLink link = new AccountLink(uuid, playerName, discordId, System.currentTimeMillis());
            s.put(link);

            existing.ifPresent(replaced -> {
                events.add(new Unlinked(replaced, UnlinkReason.REPLACED));
                LOGGER.info("[AccountLinking] Previous link of {} to Discord user {} was replaced", playerName, replaced.discordId());
            });
            events.add(new Linked(link));
            result = new LinkResult(LinkResult.Status.LINKED, link);

            LOGGER.info("[AccountLinking] {} ({}) linked to Discord user {}", playerName, uuid, discordId);
        }

        fire(events);
        return result;
    }

    public static Optional<AccountLink> unlink(UUID uuid, UnlinkReason reason) {
        Optional<AccountLink> removed;
        synchronized (LOCK) {
            AccountLinkStorage s = storage;
            if (s == null)
                return Optional.empty();
            removed = s.remove(uuid);
        }

        removed.ifPresent(link -> {
            LOGGER.info("[AccountLinking] {} ({}) unlinked from Discord user {}: {}", link.name(), uuid, link.discordId(), reason);
            fire(List.of(new Unlinked(link, reason)));
        });

        return removed;
    }

    public static List<AccountLink> unlinkAll(String discordId, UnlinkReason reason) {
        List<AccountLink> removed = new ArrayList<>();
        for (AccountLink link : getByDiscordId(discordId))
            unlink(link.uuid(), reason).ifPresent(removed::add);
        return removed;
    }

    public static Optional<AccountLink> updateName(UUID uuid, String playerName) {
        synchronized (LOCK) {
            AccountLinkStorage s = storage;
            if (s == null)
                return Optional.empty();

            Optional<AccountLink> existing = s.get(uuid);
            if (existing.isEmpty() || existing.get().name().equals(playerName))
                return existing;

            AccountLink updated = existing.get().withName(playerName);
            s.put(updated);
            return Optional.of(updated);
        }
    }

    private static void fire(List<LinkEvent> events) {
        for (LinkEvent event : events) {
            for (AccountLinkListener listener : LISTENERS) {
                try {
                    switch (event) {
                        case Linked(AccountLink link) -> listener.onLinked(link);
                        case Unlinked(AccountLink link, UnlinkReason reason) -> listener.onUnlinked(link, reason);
                    }
                } catch (Exception e) {
                    logErrorToServer(MinecraftLocaleProvider.Linking.Error.unexpected(listener.getClass().getSimpleName(), String.valueOf(e.getMessage())));
                    LOGGER.error("", e);
                }
            }
        }
    }
}

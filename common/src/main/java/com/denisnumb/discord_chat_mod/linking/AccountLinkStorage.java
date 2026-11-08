package com.denisnumb.discord_chat_mod.linking;

import com.denisnumb.discord_chat_mod.linking.model.AccountLink;
import com.denisnumb.discord_chat_mod.locale.MinecraftLocaleProvider;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static com.denisnumb.discord_chat_mod.utils.MinecraftUtils.logErrorToServer;


public final class AccountLinkStorage {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final int FORMAT_VERSION = 1;

    private final Path file;
    private final Map<UUID, AccountLink> byUuid = new LinkedHashMap<>();
    private final Map<String, Set<UUID>> byDiscordId = new HashMap<>();

    private static final class FileData {
        int version = FORMAT_VERSION;
        List<AccountLink> links = new ArrayList<>();
    }

    private AccountLinkStorage(Path file) {
        this.file = file;
    }

    public static AccountLinkStorage load(Path file) {
        AccountLinkStorage storage = new AccountLinkStorage(file);
        if (!Files.exists(file))
            return storage;

        try {
            FileData data = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), FileData.class);
            if (data != null && data.links != null) {
                for (AccountLink link : data.links) {
                    if (link == null || link.uuid() == null || link.discordId() == null || link.discordId().isBlank())
                        continue;

                    storage.putInternal(link.name() == null ? link.withName("") : link);
                }
            }
            LOGGER.info("Loaded {} linked accounts", storage.byUuid.size());
        } catch (Exception e) {
            String backup = backupBrokenFile(file);
            logErrorToServer(MinecraftLocaleProvider.Linking.Error.storageLoad(String.valueOf(e.getMessage()), backup));
            LOGGER.error("", e);
        }

        return storage;
    }

    public synchronized Optional<AccountLink> get(UUID uuid) {
        return Optional.ofNullable(byUuid.get(uuid));
    }

    public synchronized Optional<AccountLink> getByName(String name) {
        return byUuid.values().stream()
                .filter(link -> link.name().equalsIgnoreCase(name))
                .findFirst();
    }

    public synchronized List<AccountLink> getByDiscordId(String discordId) {
        Set<UUID> uuids = byDiscordId.get(discordId);
        if (uuids == null)
            return List.of();

        return uuids.stream()
                .map(byUuid::get)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingLong(AccountLink::linkedAt))
                .toList();
    }

    public synchronized List<AccountLink> getAll() {
        return List.copyOf(byUuid.values());
    }

    public synchronized Set<String> getAllDiscordIds() {
        return Set.copyOf(byDiscordId.keySet());
    }

    public synchronized Optional<AccountLink> put(AccountLink link) {
        Optional<AccountLink> previous = removeInternal(link.uuid());
        putInternal(link);
        save();
        return previous;
    }

    public synchronized Optional<AccountLink> remove(UUID uuid) {
        Optional<AccountLink> removed = removeInternal(uuid);
        if (removed.isPresent())
            save();
        return removed;
    }

    private void putInternal(AccountLink link) {
        byUuid.put(link.uuid(), link);
        byDiscordId.computeIfAbsent(link.discordId(), id -> new LinkedHashSet<>()).add(link.uuid());
    }

    private Optional<AccountLink> removeInternal(UUID uuid) {
        AccountLink removed = byUuid.remove(uuid);
        if (removed == null)
            return Optional.empty();

        Set<UUID> uuids = byDiscordId.get(removed.discordId());
        if (uuids != null) {
            uuids.remove(uuid);
            if (uuids.isEmpty())
                byDiscordId.remove(removed.discordId());
        }
        return Optional.of(removed);
    }

    private void save() {
        FileData data = new FileData();
        data.links = new ArrayList<>(byUuid.values());

        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.writeString(tmp, GSON.toJson(data), StandardCharsets.UTF_8);
            try {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            logErrorToServer(MinecraftLocaleProvider.Linking.Error.storageSave(String.valueOf(e.getMessage())));
            LOGGER.error("", e);
        }
    }

    private static String backupBrokenFile(Path file) {
        try {
            Path backup = file.resolveSibling(file.getFileName() + ".broken-" + System.currentTimeMillis());
            Files.copy(file, backup, StandardCopyOption.REPLACE_EXISTING);
            return backup.toString();
        } catch (IOException e) {
            LOGGER.error("Failed to back up {}", file, e);
            return "-";
        }
    }
}

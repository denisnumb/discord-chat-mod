package com.denisnumb.discord_chat_mod.compat.permissions;

import com.denisnumb.discord_chat_mod.utils.LegacyTextUtils;
import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankManager;
import dev.ftb.mods.ftbranks.api.event.PlayerAddedToRankEvent;
import dev.ftb.mods.ftbranks.api.event.PlayerRemovedFromRankEvent;
import dev.ftb.mods.ftbranks.api.event.RankEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.server;

final class FtbRanksCompat implements IPermissionsCompat {
    private static final String NAME_FORMAT_NODE = "ftbranks.name_format";
    private static final String NAME_PLACEHOLDER = "{name}";

    @Override
    public String id() {
        return "ftbranks";
    }

    private static Optional<RankManager> manager() {
        try {
            return Optional.of(FTBRanksAPI.manager());
        } catch (NullPointerException | IllegalStateException notReady) {
            return Optional.empty();
        }
    }

    private static Optional<String> getNameFormat(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer))
            return Optional.empty();

        return manager()
                .flatMap(manager -> manager.getPermissionValue(serverPlayer, NAME_FORMAT_NODE).asString())
                .filter(format -> format.contains(NAME_PLACEHOLDER));
    }

    private static Optional<Component> toComponent(String text) {
        return text.isBlank() ? Optional.empty() : Optional.of(LegacyTextUtils.toComponent(text));
    }

    @Override
    public Optional<Component> getPrefix(Player player) {
        return getNameFormat(player)
                .map(format -> format.substring(0, format.indexOf(NAME_PLACEHOLDER)))
                .flatMap(FtbRanksCompat::toComponent);
    }

    @Override
    public Optional<Component> getSuffix(Player player) {
        return getNameFormat(player)
                .map(format -> format.substring(format.indexOf(NAME_PLACEHOLDER) + NAME_PLACEHOLDER.length()))
                .flatMap(FtbRanksCompat::toComponent);
    }

    @Override
    public boolean supportsGroups() {
        return true;
    }

    @Override
    public boolean groupExists(String group) {
        return manager().map(manager -> manager.getRank(group).isPresent()).orElse(false);
    }

    @Override
    public CompletableFuture<Set<String>> getGroups(UUID uuid, String playerName) {
        return onServerThread(manager -> manager.getAddedRanks(new NameAndId(uuid, playerName)).stream()
                .map(Rank::getId)
                .collect(Collectors.toUnmodifiableSet()), Set.of());
    }

    @Override
    public CompletableFuture<Boolean> addGroup(UUID uuid, String playerName, String group) {
        return onServerThread(manager -> manager.getRank(group)
                .map(rank -> rank.add(new NameAndId(uuid, playerName)))
                .orElse(false), false);
    }

    @Override
    public CompletableFuture<Boolean> removeGroup(UUID uuid, String playerName, String group) {
        return onServerThread(manager -> manager.getRank(group)
                .map(rank -> rank.remove(new NameAndId(uuid, playerName)))
                .orElse(false), false);
    }

    private static <T> CompletableFuture<T> onServerThread(Function<RankManager, T> action, T fallback) {
        MinecraftServer srv = server;
        if (srv == null)
            return CompletableFuture.completedFuture(fallback);

        return srv.submit(() -> manager().map(action).orElse(fallback));
    }

    @Override
    public Optional<AutoCloseable> subscribeGroupChanges(GroupChangeListener listener) {
        Consumer<PlayerAddedToRankEvent> onAdded = event -> {
            if (event.getRank() != null)
                listener.onGroupsChanged(event.getPlayer().id(), Set.of(event.getRank().getId()), Set.of());
        };
        Consumer<PlayerRemovedFromRankEvent> onRemoved = event -> {
            if (event.getRank() != null)
                listener.onGroupsChanged(event.getPlayer().id(), Set.of(), Set.of(event.getRank().getId()));
        };

        RankEvent.ADD_PLAYER.register(onAdded);
        RankEvent.REMOVE_PLAYER.register(onRemoved);

        return Optional.of(() -> {
            RankEvent.ADD_PLAYER.unregister(onAdded);
            RankEvent.REMOVE_PLAYER.unregister(onRemoved);
        });
    }
}

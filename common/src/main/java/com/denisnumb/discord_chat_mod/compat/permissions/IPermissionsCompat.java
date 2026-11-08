package com.denisnumb.discord_chat_mod.compat.permissions;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Abstraction over permission/rank mods (LuckPerms, FTB Ranks).
 * <p>
 * Used for {@code {prefix}}/{@code {suffix}} template parameters and for Discord role synchronization.
 * Group methods must be safe to call from any thread.
 */
public interface IPermissionsCompat {
    String id();

    Optional<Component> getPrefix(Player player);

    Optional<Component> getSuffix(Player player);

    /** @return true if this provider can manage groups (used by role sync) */
    default boolean supportsGroups() {
        return false;
    }

    default boolean groupExists(String group) {
        return false;
    }

    /** Groups directly assigned to the player (inherited groups are not included) */
    default CompletableFuture<Set<String>> getGroups(UUID uuid, String playerName) {
        return CompletableFuture.completedFuture(Set.of());
    }

    default CompletableFuture<Boolean> addGroup(UUID uuid, String playerName, String group) {
        return CompletableFuture.completedFuture(false);
    }

    default CompletableFuture<Boolean> removeGroup(UUID uuid, String playerName, String group) {
        return CompletableFuture.completedFuture(false);
    }

    @FunctionalInterface
    interface GroupChangeListener {
        void onGroupsChanged(UUID uuid, Set<String> addedGroups, Set<String> removedGroups);
    }

    /**
     * Subscribes to group changes of players (including changes made by other mods or commands).
     * @return handle to cancel the subscription, or empty if the provider cannot track changes
     */
    default Optional<AutoCloseable> subscribeGroupChanges(GroupChangeListener listener) {
        return Optional.empty();
    }

    IPermissionsCompat NOOP = new IPermissionsCompat() {
        @Override
        public String id() {
            return "none";
        }

        @Override
        public Optional<Component> getPrefix(Player player) {
            return Optional.empty();
        }

        @Override
        public Optional<Component> getSuffix(Player player) {
            return Optional.empty();
        }
    };
}

package com.denisnumb.discord_chat_mod.compat.vanish;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public interface IVanishCompat {
    String id();

    boolean isVanished(ServerPlayer player);

    default boolean canSeePlayer(ServerPlayer actor, ServerPlayer observer) {
        return actor.equals(observer) || !isVanished(actor);
    }

    default boolean announcesFakeJoinLeave() {
        return true;
    }

    /**
     * If the vanish mod forbids private messages for vanished players, returns the message to show to the sender.
     */
    default Optional<Component> getPrivateMessageDenial(ServerPlayer sender) {
        return Optional.empty();
    }

    IVanishCompat NOOP = new IVanishCompat() {
        @Override
        public String id() {
            return "none";
        }

        @Override
        public boolean isVanished(ServerPlayer player) {
            return false;
        }
    };
}

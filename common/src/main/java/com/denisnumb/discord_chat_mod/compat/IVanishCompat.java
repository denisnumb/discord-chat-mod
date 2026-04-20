package com.denisnumb.discord_chat_mod.compat;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

public interface IVanishCompat {
    boolean isVanished(Player player);
    String[] filterVanishedPlayers(MinecraftServer server, String[] playerNames);
    int getVisiblePlayerCount(MinecraftServer server);

    IVanishCompat NOOP = new IVanishCompat() {
        @Override
        public boolean isVanished(Player player) {
            return false;
        }

        @Override
        public String[] filterVanishedPlayers(MinecraftServer server, String[] playerNames) {
            return playerNames;
        }

        @Override
        public int getVisiblePlayerCount(MinecraftServer server) {
            if (server != null && server.getPlayerList() != null)
                return server.getPlayerCount();
            return 0;
        }
    };
}

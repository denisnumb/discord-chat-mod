package com.denisnumb.discord_chat_mod.network;

import net.minecraft.server.level.ServerPlayer;

public class PlatformPacketDistributor {
    private static IPlatformPacketDistributor impl;

    public static void setHandler(IPlatformPacketDistributor handler) {
        impl = handler;
    }

    public static void sendToServer(CustomPayloadPackets.CustomPacketPayload payload) {
        impl.sendToServer(payload);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPayloadPackets.CustomPacketPayload payload) {
        impl.sendToPlayer(player, payload);
    }
}

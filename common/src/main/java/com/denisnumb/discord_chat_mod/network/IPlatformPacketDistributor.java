package com.denisnumb.discord_chat_mod.network;

import net.minecraft.server.level.ServerPlayer;

public interface IPlatformPacketDistributor {
    void sendToServer(CustomPayloadPackets.CustomPacketPayload payload);
    void sendToPlayer(ServerPlayer player, CustomPayloadPackets.CustomPacketPayload payload);
}

package com.denisnumb.discord_chat_mod.forge.network;

import com.denisnumb.discord_chat_mod.network.IPlatformPacketDistributor;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public class ForgePacketDistributor implements IPlatformPacketDistributor {
    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ForgeNetworking.sendToServer(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ForgeNetworking.sendToPlayer(player, payload);
    }
}

package com.denisnumb.discord_chat_mod.forge.network;

import com.denisnumb.discord_chat_mod.network.IPlatformPacketDistributor;
import net.minecraft.server.level.ServerPlayer;

import static com.denisnumb.discord_chat_mod.network.CustomPayloadPackets.*;

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

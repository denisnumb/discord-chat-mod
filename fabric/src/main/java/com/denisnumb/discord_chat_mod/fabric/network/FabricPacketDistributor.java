package com.denisnumb.discord_chat_mod.fabric.network;

import com.denisnumb.discord_chat_mod.network.IPlatformPacketDistributor;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

import static com.denisnumb.discord_chat_mod.network.CustomPayloadPackets.*;
import com.denisnumb.discord_chat_mod.fabric.network.CustomPayloadPacketsFabric.*;

public class FabricPacketDistributor implements IPlatformPacketDistributor {
    public static FabricPacket convertCustomPayloadPacketToFabricPacket(CustomPacketPayload packet){
        if (packet instanceof RequestDiscordMentionsPacket)
            return new RequestDiscordMentionsPacketFabric();
        if (packet instanceof DiscordMentionsPartPacket p)
            return new DiscordMentionsPartPacketFabric(p.sendTime, p.partIndex, p.totalParts, p.data);
        if (packet instanceof ScreenshotPartPacketClient p)
            return new ScreenshotPartPacketClientFabric(p.sendTime, p.partIndex, p.totalParts, p.data);
        if (packet instanceof ScreenshotPartPacketServer p)
            return new ScreenshotPartPacketServerFabric(p.sendTime, p.partIndex, p.totalParts, p.data);
        if (packet instanceof DiscordEmojisPartPacket p)
            return new DiscordEmojisPartPacketFabric(p.sendTime, p.partIndex, p.totalParts, p.data);
        if (packet instanceof RequestDiscordEmojisPacket)
            return new RequestDiscordEmojisPacketFabric();
        if (packet instanceof DiscordStickersPartPacket p)
            return new DiscordStickersPartPacketFabric(p.sendTime, p.partIndex, p.totalParts, p.data);
        if (packet instanceof RequestDiscordStickersPacket)
            return new RequestDiscordStickersPacketFabric();
        return null;
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(convertCustomPayloadPacketToFabricPacket(payload));
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, convertCustomPayloadPacketToFabricPacket(payload));
    }
}

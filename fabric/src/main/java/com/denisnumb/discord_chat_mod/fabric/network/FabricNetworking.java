package com.denisnumb.discord_chat_mod.fabric.network;

import com.denisnumb.discord_chat_mod.network.PacketHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import com.denisnumb.discord_chat_mod.fabric.network.CustomPayloadPacketsFabric.*;
import com.denisnumb.discord_chat_mod.network.CustomPayloadPackets.*;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;

public class FabricNetworking {
    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(
                RequestDiscordMentionsPacketFabric.TYPE,
                (packet, player, sender) -> {
                    MinecraftServer server = player.getServer();
                    if (server != null)
                        server.execute(() -> PacketHandler.handleRequestDiscordMentionsPacket(player));
                }
        );

        ServerPlayNetworking.registerGlobalReceiver(
                RequestDiscordEmojisPacketFabric.TYPE,
                (packet, player, sender) -> {
                    MinecraftServer server = player.getServer();
                    if (server != null)
                        server.execute(() -> PacketHandler.handleRequestDiscordEmojisPacket(player));
                }
        );

        ServerPlayNetworking.registerGlobalReceiver(
                RequestDiscordStickersPacketFabric.TYPE,
                (packet, player, sender) -> {
                    MinecraftServer server = player.getServer();
                    if (server != null)
                        server.execute(() -> PacketHandler.handleRequestDiscordStickersPacket(player));
                }
        );

        ServerPlayNetworking.registerGlobalReceiver(
                ScreenshotPartPacketServerFabric.TYPE,
                (p, player, sender) -> {
                    MinecraftServer server = player.getServer();
                    if (server != null)
                        server.execute(() -> PacketHandler.handleScreenshotPartPacketServerSide(
                                new ScreenshotPartPacketServer(p.sendTime, p.partIndex, p.totalParts, p.data), player)
                        );
                }
        );

    }

    public static void initClient(){
        ClientPlayNetworking.registerGlobalReceiver(
                ScreenshotPartPacketClientFabric.TYPE,
                (p, player, sender) -> {
                    Minecraft.getInstance().execute(() -> PacketHandler.handleScreenshotPartPacketClientSide(
                            new ScreenshotPartPacketClient(p.sendTime, p.partIndex, p.totalParts, p.data))
                    );
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(
                DiscordEmojisPartPacketFabric.TYPE,
                (p, player, sender) -> {
                    Minecraft.getInstance().execute(() -> PacketHandler.handleDiscordEmojisPacket(
                            new DiscordEmojisPartPacket(p.sendTime, p.partIndex, p.totalParts, p.data))
                    );
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(
                DiscordStickersPartPacketFabric.TYPE,
                (p, player, sender) -> {
                    Minecraft.getInstance().execute(() -> PacketHandler.handleDiscordStickersPacket(
                            new DiscordStickersPartPacket(p.sendTime, p.partIndex, p.totalParts, p.data))
                    );
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(
                DiscordMentionsPartPacketFabric.TYPE,
                (p, player, sender) -> {
                    Minecraft.getInstance().execute(() -> PacketHandler.handleDiscordMentionsPacket(
                            new DiscordMentionsPartPacket(p.sendTime, p.partIndex, p.totalParts, p.data))
                    );
                }
        );
    }
}

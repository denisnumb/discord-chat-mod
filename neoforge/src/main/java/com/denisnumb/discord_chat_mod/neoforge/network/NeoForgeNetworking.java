package com.denisnumb.discord_chat_mod.neoforge.network;

import com.denisnumb.discord_chat_mod.DiscordChatMod;
import com.denisnumb.discord_chat_mod.network.PacketHandler;
import com.denisnumb.discord_chat_mod.network.emoji.DiscordEmojisPartPacket;
import com.denisnumb.discord_chat_mod.network.emoji.RequestDiscordEmojisPacket;
import com.denisnumb.discord_chat_mod.network.mentions.DiscordMentionsPartPacket;
import com.denisnumb.discord_chat_mod.network.mentions.RequestDiscordMentionsPacket;
import com.denisnumb.discord_chat_mod.network.screenshot.ScreenshotPartPacket;
import com.denisnumb.discord_chat_mod.network.sticker.DiscordStickersPartPacket;
import com.denisnumb.discord_chat_mod.network.sticker.RequestDiscordStickersPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.MainThreadPayloadHandler;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = DiscordChatMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class NeoForgeNetworking {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("2").executesOn(HandlerThread.NETWORK).optional();

        registrar.playToServer(
                RequestDiscordMentionsPacket.TYPE,
                RequestDiscordMentionsPacket.STREAM_CODEC,
                new MainThreadPayloadHandler<>((data, context) -> {
                    if (context.player() instanceof ServerPlayer player)
                        PacketHandler.handleRequestDiscordMentionsPacket(player);
                })
        );

        registrar.playToClient(
                DiscordMentionsPartPacket.TYPE,
                DiscordMentionsPartPacket.STREAM_CODEC,
                new MainThreadPayloadHandler<>((data, context) ->
                        PacketHandler.handleDiscordMentionsPacket(data)
                )
        );

        registrar.playBidirectional(
                ScreenshotPartPacket.TYPE,
                ScreenshotPartPacket.STREAM_CODEC,
                new MainThreadPayloadHandler<>((data, context) -> {
                    if (context.player() instanceof ServerPlayer player)
                        PacketHandler.handleScreenshotPartPacketServerSide(data, player);
                    else
                        PacketHandler.handleScreenshotPartPacketClientSide(data);
                })
        );

        registrar.playToClient(
                DiscordEmojisPartPacket.TYPE,
                DiscordEmojisPartPacket.STREAM_CODEC,
                new MainThreadPayloadHandler<>((data, context) ->
                        PacketHandler.handleDiscordEmojisPacket(data)
                )
        );

        registrar.playToServer(
                RequestDiscordEmojisPacket.TYPE,
                RequestDiscordEmojisPacket.STREAM_CODEC,
                new MainThreadPayloadHandler<>((data, context) -> {
                    if (context.player() instanceof ServerPlayer player)
                        PacketHandler.handleRequestDiscordEmojisPacket(player);
                })
        );

        registrar.playToClient(
                DiscordStickersPartPacket.TYPE,
                DiscordStickersPartPacket.STREAM_CODEC,
                new MainThreadPayloadHandler<>((data, context) ->
                        PacketHandler.handleDiscordStickersPacket(data)
                )
        );

        registrar.playToServer(
                RequestDiscordStickersPacket.TYPE,
                RequestDiscordStickersPacket.STREAM_CODEC,
                new MainThreadPayloadHandler<>((data, context) -> {
                    if (context.player() instanceof ServerPlayer player)
                        PacketHandler.handleRequestDiscordStickersPacket(player);
                })
        );
    }
}

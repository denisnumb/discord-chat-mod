package com.denisnumb.discord_chat_mod.forge.network;

import com.denisnumb.discord_chat_mod.DiscordChatMod;
import com.denisnumb.discord_chat_mod.network.PacketHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.SimpleChannel;

import static com.denisnumb.discord_chat_mod.network.CustomPayloadPackets.*;

@Mod.EventBusSubscriber(modid = DiscordChatMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeNetworking {
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(DiscordChatMod.MOD_ID, "main"))
            .serverAcceptedVersions(status -> true)
            .clientAcceptedVersions(status -> true)
            .networkProtocolVersion(() -> "2")
            .simpleChannel();

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            int id = 0;

            CHANNEL.messageBuilder(RequestDiscordMentionsPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                    .encoder(RequestDiscordMentionsPacket::encode)
                    .decoder(RequestDiscordMentionsPacket::new)
                    .consumerMainThread((data, context)
                            -> PacketHandler.handleRequestDiscordMentionsPacket(context.get().getSender()))
                    .add();

            CHANNEL.messageBuilder(DiscordMentionsPartPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                    .encoder(DiscordMentionsPartPacket::encode)
                    .decoder(DiscordMentionsPartPacket::new)
                    .consumerMainThread((data, context)
                            -> PacketHandler.handleDiscordMentionsPacket(data))
                    .add();

            CHANNEL.messageBuilder(ScreenshotPartPacketServer.class, id++, NetworkDirection.PLAY_TO_SERVER)
                    .encoder(ScreenshotPartPacketServer::encode)
                    .decoder(ScreenshotPartPacketServer::new)
                    .consumerMainThread((data, context) -> {
                        ServerPlayer player = context.get().getSender();
                        if (player == null)
                            return;
                        PacketHandler.handleScreenshotPartPacketServerSide(data, player);
                    })
                    .add();

            CHANNEL.messageBuilder(ScreenshotPartPacketClient.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                    .encoder(ScreenshotPartPacketClient::encode)
                    .decoder(ScreenshotPartPacketClient::new)
                    .consumerMainThread((data, context) ->
                        PacketHandler.handleScreenshotPartPacketClientSide(data))
                    .add();

            CHANNEL.messageBuilder(DiscordEmojisPartPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                    .encoder(DiscordEmojisPartPacket::encode)
                    .decoder(DiscordEmojisPartPacket::new)
                    .consumerMainThread((data, context)
                            -> PacketHandler.handleDiscordEmojisPacket(data))
                    .add();

            CHANNEL.messageBuilder(RequestDiscordEmojisPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                    .encoder(RequestDiscordEmojisPacket::encode)
                    .decoder(RequestDiscordEmojisPacket::new)
                    .consumerMainThread((data, context)
                            -> PacketHandler.handleRequestDiscordEmojisPacket(context.get().getSender()))
                    .add();

            CHANNEL.messageBuilder(DiscordStickersPartPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                    .encoder(DiscordStickersPartPacket::encode)
                    .decoder(DiscordStickersPartPacket::new)
                    .consumerMainThread((data, context)
                            -> PacketHandler.handleDiscordStickersPacket(data))
                    .add();

            CHANNEL.messageBuilder(RequestDiscordStickersPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                    .encoder(RequestDiscordStickersPacket::encode)
                    .decoder(RequestDiscordStickersPacket::new)
                    .consumerMainThread((data, context)
                            -> PacketHandler.handleRequestDiscordStickersPacket(context.get().getSender()))
                    .add();
        });
    }

    public static void sendToServer(CustomPacketPayload msg){
        CHANNEL.send(PacketDistributor.SERVER.noArg(), msg);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload msg){
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), msg);
    }
}

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

            CHANNEL.messageBuilder(ImagePartPacketServer.class, id++, NetworkDirection.PLAY_TO_SERVER)
                    .encoder(ImagePartPacketServer::encode)
                    .decoder(ImagePartPacketServer::new)
                    .consumerMainThread((data, context) -> {
                        ServerPlayer player = context.get().getSender();
                        if (player == null)
                            return;
                        PacketHandler.handleImagePartPacketServerSide(data, player);
                    })
                    .add();

            CHANNEL.messageBuilder(ImagePartPacketClient.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                    .encoder(ImagePartPacketClient::encode)
                    .decoder(ImagePartPacketClient::new)
                    .consumerMainThread((data, context) ->
                        PacketHandler.handleImagePartPacketClientSide(data))
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

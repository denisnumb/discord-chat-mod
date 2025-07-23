package com.denisnumb.discord_chat_mod.forge.network;

import com.denisnumb.discord_chat_mod.DiscordChatMod;
import com.denisnumb.discord_chat_mod.network.PacketHandler;
import com.denisnumb.discord_chat_mod.network.emoji.DiscordEmojisPartPacket;
import com.denisnumb.discord_chat_mod.network.emoji.RequestDiscordEmojisPacket;
import com.denisnumb.discord_chat_mod.network.mentions.DiscordMentionsPartPacket;
import com.denisnumb.discord_chat_mod.network.mentions.RequestDiscordMentionsPacket;
import com.denisnumb.discord_chat_mod.network.screenshot.ScreenshotPartPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.*;
import org.jetbrains.annotations.Nullable;

import static com.denisnumb.discord_chat_mod.forge.network.CustomPayloadPacketsForge.*;

@Mod.EventBusSubscriber(modid = DiscordChatMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeNetworking {
    public static final SimpleChannel CHANNEL = ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(DiscordChatMod.MOD_ID, "main"))
            .networkProtocolVersion(2)
            .optional()
            .simpleChannel();

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            int id = 0;

            CHANNEL.messageBuilder(RequestDiscordMentionsPacketForge.class, id++, NetworkDirection.PLAY_TO_SERVER)
                    .encoder(RequestDiscordMentionsPacketForge::encode)
                    .decoder(RequestDiscordMentionsPacketForge::new)
                    .consumerMainThread((data, context)
                            -> PacketHandler.handleRequestDiscordMentionsPacket(context.getSender())
                    )
                    .add();

            CHANNEL.messageBuilder(DiscordMentionsPartPacketForge.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                    .encoder(DiscordMentionsPartPacketForge::encode)
                    .decoder(DiscordMentionsPartPacketForge::new)
                    .consumerMainThread((data, context)
                            -> PacketHandler.handleDiscordMentionsPacket(
                                    new DiscordMentionsPartPacket(data.sendTime, data.partIndex, data.totalParts, data.data)
                            )
                    )
                    .add();

            CHANNEL.messageBuilder(ScreenshotPartPacketServerForge.class, id++, NetworkDirection.PLAY_TO_SERVER)
                    .encoder(ScreenshotPartPacketServerForge::encode)
                    .decoder(ScreenshotPartPacketServerForge::new)
                    .consumerMainThread((data, context) -> {
                        ServerPlayer player = context.getSender();
                        if (player == null)
                            return;

                        PacketHandler.handleScreenshotPartPacketServerSide(
                                new ScreenshotPartPacket(data.sendTime, data.partIndex, data.totalParts, data.data),
                                player
                        );
                    })
                    .add();

            CHANNEL.messageBuilder(ScreenshotPartPacketClientForge.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                    .encoder(ScreenshotPartPacketClientForge::encode)
                    .decoder(ScreenshotPartPacketClientForge::new)
                    .consumerMainThread((data, context) ->
                        PacketHandler.handleScreenshotPartPacketClientSide(
                                new ScreenshotPartPacket(data.sendTime, data.partIndex, data.totalParts, data.data)
                        )
                    )
                    .add();

            CHANNEL.messageBuilder(DiscordEmojisPartPacketForge.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                    .encoder(DiscordEmojisPartPacketForge::encode)
                    .decoder(DiscordEmojisPartPacketForge::new)
                    .consumerMainThread((data, context)
                            -> PacketHandler.handleDiscordEmojisPacket(
                                    new DiscordEmojisPartPacket(data.sendTime, data.partIndex, data.totalParts, data.data)
                            )
                    )
                    .add();

            CHANNEL.messageBuilder(RequestDiscordEmojisPacketForge.class, id++, NetworkDirection.PLAY_TO_SERVER)
                    .encoder(RequestDiscordEmojisPacketForge::encode)
                    .decoder(RequestDiscordEmojisPacketForge::new)
                    .consumerMainThread((data, context)
                            -> PacketHandler.handleRequestDiscordEmojisPacket(context.getSender()))
                    .add();
        });
    }

    @Nullable
    private static CustomPayloadPacketsForge.CustomPacketPayloadForge convertCustomPacketPayloadToForgePacketPayload(
            CustomPacketPayload packet,
            boolean isPacketForServer
    ){
        if (packet instanceof RequestDiscordMentionsPacket)
            return new RequestDiscordMentionsPacketForge();
        if (packet instanceof DiscordMentionsPartPacket(long sendTime, int partIndex, int totalParts, byte[] data))
            return new DiscordMentionsPartPacketForge(sendTime, partIndex, totalParts, data);
        if (packet instanceof ScreenshotPartPacket(long sendTime, int partIndex, int totalParts, byte[] data)){
            return isPacketForServer
                    ? new ScreenshotPartPacketServerForge(sendTime, partIndex, totalParts, data)
                    : new ScreenshotPartPacketClientForge(sendTime, partIndex, totalParts, data);
        }
        if (packet instanceof DiscordEmojisPartPacket(long sendTime, int partIndex, int totalParts, byte[] data))
            return new DiscordEmojisPartPacketForge(sendTime, partIndex, totalParts, data);
        if (packet instanceof RequestDiscordEmojisPacket)
            return new RequestDiscordEmojisPacketForge();
        return null;
    }

    public static void sendToServer(CustomPacketPayload msg){
        CustomPacketPayloadForge packet = convertCustomPacketPayloadToForgePacketPayload(msg, true);
        if (packet != null)
            CHANNEL.send(packet, PacketDistributor.SERVER.noArg());
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload msg){
        CustomPacketPayloadForge packet = convertCustomPacketPayloadToForgePacketPayload(msg, false);
        if (packet != null)
            CHANNEL.send(packet, PacketDistributor.PLAYER.with(player));
    }
}

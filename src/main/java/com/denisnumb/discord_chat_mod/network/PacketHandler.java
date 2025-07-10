package com.denisnumb.discord_chat_mod.network;

import com.denisnumb.discord_chat_mod.network.emoji.DiscordEmojisPartPacket;
import com.denisnumb.discord_chat_mod.network.emoji.DiscordEmojisTransceiver;
import com.denisnumb.discord_chat_mod.network.emoji.RequestDiscordEmojisPacket;
import com.denisnumb.discord_chat_mod.network.mentions.DiscordMentionsPartPacket;
import com.denisnumb.discord_chat_mod.network.mentions.DiscordMentionsTransceiver;
import com.denisnumb.discord_chat_mod.network.mentions.RequestDiscordMentionsPacket;
import com.denisnumb.discord_chat_mod.network.screenshot.ScreenshotPartPacket;
import com.denisnumb.discord_chat_mod.network.screenshot.ScreenshotTransceiver;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.isDiscordConnected;
import static com.denisnumb.discord_chat_mod.MinecraftUtils.getTranslate;
import static com.denisnumb.discord_chat_mod.ModLanguageKey.SERVER_IS_NOT_CONNECTED_TO_DISCORD;

public class PacketHandler {
    public static void handleDiscordEmojisPacket(final DiscordEmojisPartPacket data, final IPayloadContext ignoredContext) {
        DiscordEmojisTransceiver.receiveDiscordEmojisPart(data);
    }

    public static void handleRequestDiscordEmojisPacket(final RequestDiscordEmojisPacket ignoredData, final IPayloadContext context){
        if (context.player() instanceof ServerPlayer player)
            DiscordEmojisTransceiver.sendDiscordEmojisDataToPlayer(player);
    }

    public static void handleDiscordMentionsPacket(final DiscordMentionsPartPacket data, final IPayloadContext ignoredContext) {
        DiscordMentionsTransceiver.receiveDiscordMentionsPart(data);
    }

    public static void handleRequestDiscordMentionsPacket(final RequestDiscordMentionsPacket ignoredData, final IPayloadContext context){
        if (context.player() instanceof ServerPlayer player)
            DiscordMentionsTransceiver.sendDiscordMemberDataToPlayer(player);
    }

    public static void handleScreenshotPartPacket(final ScreenshotPartPacket data, final IPayloadContext context){
        if (context.player() instanceof ServerPlayer player){
            if (isDiscordConnected())
                ScreenshotTransceiver.receivePart(data, player);
            else if (data.partIndex() == 0){
                context.enqueueWork(() -> player.connection.send(new ClientboundSetActionBarTextPacket(
                        Component.literal(getTranslate(
                                SERVER_IS_NOT_CONNECTED_TO_DISCORD,
                                "Server is not connected to Discord")
                        ).withStyle(ChatFormatting.RED)
                )));
            }
        }
    }
}

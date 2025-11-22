package com.denisnumb.discord_chat_mod.network;

import com.denisnumb.discord_chat_mod.network.emoji.DiscordEmojisPartPacket;
import com.denisnumb.discord_chat_mod.network.emoji.DiscordEmojisTransceiver;
import com.denisnumb.discord_chat_mod.network.mentions.DiscordMentionsPartPacket;
import com.denisnumb.discord_chat_mod.network.mentions.DiscordMentionsTransceiver;
import com.denisnumb.discord_chat_mod.network.screenshot.ScreenshotPartPacket;
import com.denisnumb.discord_chat_mod.network.screenshot.ScreenshotTransceiver;
import com.denisnumb.discord_chat_mod.network.sticker.DiscordStickersPartPacket;
import com.denisnumb.discord_chat_mod.network.sticker.DiscordStickersTransceiver;
import net.minecraft.server.level.ServerPlayer;


public class PacketHandler {
    public static void handleDiscordStickersPacket(final DiscordStickersPartPacket data) {
        DiscordStickersTransceiver.receiveDiscordStickersPart(data);
    }

    public static void handleRequestDiscordStickersPacket(ServerPlayer player){
        DiscordStickersTransceiver.sendDiscordStickersDataToPlayer(player);
    }

    public static void handleDiscordEmojisPacket(final DiscordEmojisPartPacket data) {
        DiscordEmojisTransceiver.receiveDiscordEmojisPart(data);
    }

    public static void handleRequestDiscordEmojisPacket(ServerPlayer player){
        DiscordEmojisTransceiver.sendDiscordEmojisDataToPlayer(player);
    }

    public static void handleDiscordMentionsPacket(final DiscordMentionsPartPacket data) {
        DiscordMentionsTransceiver.receiveDiscordMentionsPart(data);
    }

    public static void handleRequestDiscordMentionsPacket(ServerPlayer player){
        DiscordMentionsTransceiver.sendDiscordMemberDataToPlayer(player);
    }

    public static void handleScreenshotPartPacketServerSide(final ScreenshotPartPacket data, ServerPlayer player){
        ScreenshotTransceiver.receivePartServerSide(data, player);
    }

    public static void handleScreenshotPartPacketClientSide(final ScreenshotPartPacket data){
        ScreenshotTransceiver.receivePartClientSide(data);
    }
}

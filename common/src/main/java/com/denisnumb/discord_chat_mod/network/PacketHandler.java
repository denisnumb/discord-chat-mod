package com.denisnumb.discord_chat_mod.network;

import com.denisnumb.discord_chat_mod.network.emoji.DiscordEmojisTransceiver;
import com.denisnumb.discord_chat_mod.network.image.ImageTransceiver;
import com.denisnumb.discord_chat_mod.network.mentions.DiscordMentionsTransceiver;
import com.denisnumb.discord_chat_mod.network.sticker.DiscordStickersTransceiver;
import net.minecraft.server.level.ServerPlayer;

import static com.denisnumb.discord_chat_mod.network.CustomPayloadPackets.*;

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

    public static void handleImagePartPacketServerSide(final ImagePartPacketServer data, ServerPlayer player){
        ImageTransceiver.receivePartServerSide(data, player);
    }

    public static void handleImagePartPacketClientSide(final ImagePartPacketClient data){
        ImageTransceiver.receivePartClientSide(data);
    }
}

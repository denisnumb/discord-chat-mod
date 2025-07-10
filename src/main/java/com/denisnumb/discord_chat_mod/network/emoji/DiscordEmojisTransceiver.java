package com.denisnumb.discord_chat_mod.network.emoji;


import com.denisnumb.discord_chat_mod.discord.CustomEmojiProvider;
import com.denisnumb.discord_chat_mod.network.BigPacketsTransceiver;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.discordChannel;
import static com.denisnumb.discord_chat_mod.discord.CustomEmojiProvider.loadClient;

public class DiscordEmojisTransceiver {
    private static final Map<Long, ArrayList<byte[]>> receivedParts = new HashMap<>();
    private static final Gson gson = new Gson();
    private static final Type gsonType = new TypeToken<Map<String, String>>(){}.getType();

    public static void sendDiscordEmojisDataToPlayer(ServerPlayer player) {
        long sendTime = System.currentTimeMillis();
        byte[] data = gson.toJson(CustomEmojiProvider.getNameToUrlMap(discordChannel), gsonType).getBytes();
        BigPacketsTransceiver.send(data, (partIndex, totalParts, part) ->
                PacketDistributor.sendToPlayer(player, new DiscordEmojisPartPacket(sendTime, partIndex, totalParts, part))
        );
    }

    public static void receiveDiscordEmojisPart(DiscordEmojisPartPacket packet) {
        BigPacketsTransceiver.receivePart(
                receivedParts,
                packet.sendTime(),
                packet.partIndex(),
                packet.totalParts(),
                packet.data()
        ).ifPresent(data -> loadClient(gson.fromJson(new String(data), gsonType)));
    }
}

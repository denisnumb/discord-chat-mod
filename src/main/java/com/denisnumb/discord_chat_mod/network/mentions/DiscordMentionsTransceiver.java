package com.denisnumb.discord_chat_mod.network.mentions;

import com.denisnumb.discord_chat_mod.discord.ChannelMembersProvider;
import com.denisnumb.discord_chat_mod.discord.model.DiscordMemberData;
import com.denisnumb.discord_chat_mod.network.BigPacketsTransceiver;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.lang.reflect.Type;
import java.util.*;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.discordChannel;


public class DiscordMentionsTransceiver {
    private static final Map<Long, ArrayList<byte[]>> receivedParts = new HashMap<>();
    private static final Gson gson = new Gson();
    private static final Type gsonType = new TypeToken<List<DiscordMemberData>>(){}.getType();
    private static final Map<ServerPlayer, Long> lastMentionsRequestFromPlayer = new HashMap<>();

    public static void sendDiscordMemberDataToPlayer(ServerPlayer player) {
        long sendTime = System.currentTimeMillis();
        lastMentionsRequestFromPlayer.putIfAbsent(player, 0L);
        if (sendTime - lastMentionsRequestFromPlayer.get(player) < 5000)
            return;
        lastMentionsRequestFromPlayer.replace(player, sendTime);

        byte[] data = gson.toJson(ChannelMembersProvider.getMemberData(discordChannel), gsonType).getBytes();
        BigPacketsTransceiver.send(data, (partIndex, totalParts, part) ->
                PacketDistributor.sendToPlayer(player, new DiscordMentionsPartPacket(sendTime, partIndex, totalParts, part))
        );
    }

    public static void receiveDiscordMentionsPart(DiscordMentionsPartPacket packet) {
        BigPacketsTransceiver.receivePart(
                receivedParts,
                packet.sendTime(),
                packet.partIndex(),
                packet.totalParts(),
                packet.data()
        ).ifPresent(bytes -> ChannelMembersProvider.clientMemberData = gson.fromJson(new String(bytes), gsonType));
    }
}

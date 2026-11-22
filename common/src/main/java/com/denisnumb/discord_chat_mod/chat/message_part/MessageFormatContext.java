package com.denisnumb.discord_chat_mod.chat.message_part;

import com.denisnumb.discord_chat_mod.chat.placeholder.ChatPlaceholderValues;
import com.denisnumb.discord_chat_mod.discord.data_providers.ChannelMembersProvider;
import com.denisnumb.discord_chat_mod.discord.model.ChannelCategory;
import com.denisnumb.discord_chat_mod.discord.model.DiscordMentionData;
import com.denisnumb.discord_chat_mod.discord.model.DiscordUserData;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.isDiscordConnected;


public record MessageFormatContext(
        boolean discordConnected,
        List<DiscordUserData> members,
        Map<String, DiscordMentionData> mentions,
        @Nullable ChatPlaceholderValues placeholderValues
) {
    public static MessageFormatContext create(ChannelCategory chatCategoryToParseMembers, @Nullable ChatPlaceholderValues placeholderValues) {
        if (!isDiscordConnected())
            return new MessageFormatContext(false, List.of(), Map.of(), placeholderValues);

        List<DiscordUserData> members = ChannelMembersProvider.getMemberData(chatCategoryToParseMembers);

        Map<String, DiscordMentionData> mentions = new HashMap<>();
        for (DiscordUserData member : members)
            mentions.put(member.mentionString, new DiscordMentionData(member));

        return new MessageFormatContext(true, members, mentions, placeholderValues);
    }
}

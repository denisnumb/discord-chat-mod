package com.denisnumb.discord_chat_mod.chat;

import com.denisnumb.discord_chat_mod.discord.data_providers.ChannelMembersProvider;
import com.denisnumb.discord_chat_mod.discord.model.ChannelCategory;
import com.denisnumb.discord_chat_mod.discord.model.DiscordMentionData;
import com.denisnumb.discord_chat_mod.discord.model.DiscordUserData;
import com.denisnumb.discord_chat_mod.markdown.MarkdownParser;
import com.denisnumb.discord_chat_mod.markdown.MarkdownToComponentConverter;
import com.denisnumb.discord_chat_mod.markdown.MinecraftFormattingConverter;
import com.denisnumb.discord_chat_mod.emoji.EmojiUtils;
import com.denisnumb.discord_chat_mod.chat.placeholder.ChatPlaceholder;
import com.denisnumb.discord_chat_mod.chat.placeholder.ChatPlaceholderValues;
import com.denisnumb.discord_chat_mod.chat.placeholder.ChatPlaceholders;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.isDiscordConnected;

public final class CommonMessageFormatter {
    private CommonMessageFormatter() {}

    public record FormattedMessage(Component forMinecraft, String forDiscord) {}

    public static FormattedMessage formatMessage(String message, ChannelCategory chatCategoryToParseMembers) {
        return format(message, chatCategoryToParseMembers, null);
    }

    public static FormattedMessage formatMessage(String message, ChannelCategory chatCategoryToParseMembers, ChatPlaceholderValues placeholderValues) {
        return format(message, chatCategoryToParseMembers, placeholderValues);
    }

    private static FormattedMessage format(String message, ChannelCategory chatCategoryToParseMembers, @Nullable ChatPlaceholderValues placeholderValues) {
        Map<String, DiscordMentionData> mentions = Map.of();
        String forDiscord = message;

        if (isDiscordConnected()) {
            List<DiscordUserData> memberData
                    = ChannelMembersProvider.getMemberData(chatCategoryToParseMembers);

            for (DiscordUserData member : memberData)
                if (message.contains(member.prettyMention))
                    message = message.replace(member.prettyMention, member.mentionString);

            mentions = new HashMap<>();
            for (DiscordUserData member : memberData)
                mentions.put(member.mentionString, new DiscordMentionData(member));

            forDiscord = MinecraftFormattingConverter.toDiscordMarkdown(
                    MarkdownParser.removeColorTags(
                            EmojiUtils.replaceEmojiCodesToDiscordMentions(message)
                    )
            );
        }

        String forMinecraftText = message;
        Map<Character, Component> placeholderComponents = new HashMap<>();

        if (placeholderValues != null) {
            Map<ChatPlaceholder, Component> values = placeholderValues.forMinecraft(ChatPlaceholders.find(message));
            values.forEach((placeholder, value) -> placeholderComponents.put(placeholder.marker(), value));
            forMinecraftText = ChatPlaceholders.markForMinecraft(message, values.keySet());
        }

        Component forMinecraft = MarkdownToComponentConverter.convertTokens(
                MarkdownParser.parseMarkdown(forMinecraftText), mentions, placeholderComponents
        );

        return new FormattedMessage(forMinecraft, forDiscord);
    }
}

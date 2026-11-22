package com.denisnumb.discord_chat_mod.chat.message_part;

import com.denisnumb.discord_chat_mod.chat.CommonMessageFormatter.FormattedMessage;
import com.denisnumb.discord_chat_mod.chat.placeholder.ChatPlaceholder;
import com.denisnumb.discord_chat_mod.chat.placeholder.ChatPlaceholders;
import com.denisnumb.discord_chat_mod.discord.model.DiscordUserData;
import com.denisnumb.discord_chat_mod.emoji.EmojiUtils;
import com.denisnumb.discord_chat_mod.markdown.MarkdownParser;
import com.denisnumb.discord_chat_mod.markdown.MarkdownToComponentConverter;
import com.denisnumb.discord_chat_mod.markdown.MinecraftFormattingConverter;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Regular text of a message: mentions, emojis, markdown and {@link ChatPlaceholder}s
 */
public record TextPart(String text) implements MessagePart {
    @Override
    public FormattedMessage format(MessageFormatContext context) {
        String text = this.text;
        for (DiscordUserData member : context.members())
            if (text.contains(member.prettyMention))
                text = text.replace(member.prettyMention, member.mentionString);

        return new FormattedMessage(formatForMinecraft(text, context), formatForDiscord(text, context));
    }

    private static Component formatForMinecraft(String text, MessageFormatContext context) {
        Map<ChatPlaceholder, Component> placeholders = context.placeholderValues() == null
                ? Map.of()
                : context.placeholderValues().forMinecraft(ChatPlaceholders.find(text));

        Map<Character, Component> placeholderComponents = new HashMap<>();
        placeholders.forEach((placeholder, value) -> placeholderComponents.put(placeholder.marker(), value));

        String markedText = context.placeholderValues() == null
                ? text
                : ChatPlaceholders.markForMinecraft(text, placeholders.keySet());

        return MarkdownToComponentConverter.convertTokens(
                MarkdownParser.parseMarkdown(markedText), context.mentions(), placeholderComponents
        );
    }

    private static String formatForDiscord(String text, MessageFormatContext context) {
        if (!context.discordConnected())
            return text;

        return MinecraftFormattingConverter.toDiscordMarkdown(
                MarkdownParser.removeColorTags(
                        EmojiUtils.replaceEmojiCodesToDiscordMentions(text)
                )
        );
    }
}

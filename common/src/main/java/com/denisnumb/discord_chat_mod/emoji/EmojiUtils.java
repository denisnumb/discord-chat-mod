package com.denisnumb.discord_chat_mod.emoji;

import com.denisnumb.discord_chat_mod.discord.data_providers.CustomEmojiProvider;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EmojiUtils {
    private EmojiUtils() {}

    public static final Pattern EMOJI_PATTERN = Pattern.compile("(?<!\\\\):([a-zA-Z0-9_]{2,}(~[1-9][0-9]*)?):");
    private static final Pattern DISCORD_MENTION_PATTERN = Pattern.compile("(?<!\\\\)<a?:[a-zA-Z0-9_]+:\\d+>");
    public static final String ESCAPED_EMOJI_MARKER = "\u200C";

    public static String escapedEmojiCode(String name) {
        return ":" + ESCAPED_EMOJI_MARKER + name + ":";
    }

    public static String replaceDiscordEmojiMentionsToEmojiNames(String text) {
        Matcher matcher = DISCORD_MENTION_PATTERN.matcher(text);
        Map<String, CustomEmojiProvider.EmojiData> emojiMap = CustomEmojiProvider.getNameToEmojiDataMap();

        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String mentionString = matcher.group();
            emojiMap.entrySet().stream()
                    .filter(entry -> entry.getValue().mentionString().equals(mentionString))
                    .findFirst()
                    .map(Map.Entry::getKey)
                    .ifPresent(indexedName -> matcher.appendReplacement(result,
                            Matcher.quoteReplacement(String.format(":%s:", indexedName))));
        }

        matcher.appendTail(result);
        return result.toString();
    }

    public static String replaceEmojiCodesToDiscordMentions(String text) {
        Matcher matcher = EMOJI_PATTERN.matcher(text);
        Map<String, CustomEmojiProvider.EmojiData> emojiMap = CustomEmojiProvider.getNameToEmojiDataMap();

        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String indexedName = matcher.group(1);
            if (emojiMap.containsKey(indexedName)) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(emojiMap.get(indexedName).mentionString()));
                continue;
            }

            UnicodeEmojis.Entry unicode = matcher.group(2) == null ? UnicodeEmojis.byName(indexedName) : null;
            if (unicode != null)
                matcher.appendReplacement(result, Matcher.quoteReplacement(unicode.emoji()));
        }


        matcher.appendTail(result);
        return result.toString();
    }
}

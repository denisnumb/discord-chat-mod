package com.denisnumb.discord_chat_mod.api;

import java.time.Instant;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Helpers for Discord message text.
 *
 * @since 1.0.0
 */
public final class DiscordText {
    private DiscordText() {}

    private static final Pattern SNOWFLAKE = Pattern.compile("\\d{1,20}");
    private static final String MARKDOWN_CHARS = "\\*_~`|>#-[]()<:";

    /**
     * Styles of {@link #timestamp(Instant, TimestampStyle)}. Every reader sees the time in their own time zone.
     */
    public enum TimestampStyle {
        /**
         * 16:20
         */
        SHORT_TIME('t'),
        /**
         * 16:20:30
         */
        LONG_TIME('T'),
        /**
         * 20/04/2021
         */
        SHORT_DATE('d'),
        /**
         * 20 April 2021
         */
        LONG_DATE('D'),
        /**
         * 20 April 2021 16:20
         */
        SHORT_DATE_TIME('f'),
        /**
         * Tuesday, 20 April 2021 16:20
         */
        LONG_DATE_TIME('F'),
        /**
         * 2 months ago
         */
        RELATIVE('R');

        private final char code;

        TimestampStyle(char code) {
            this.code = code;
        }
    }

    /**
     * Escapes Discord markdown so that the text is displayed literally. Use it for any text written by players before
     * putting it into {@link DiscordMessage.Builder#content} or embeds.
     */
    public static String escapeMarkdown(String text) {
        Objects.requireNonNull(text, "text");
        StringBuilder result = new StringBuilder(text.length() + 16);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (MARKDOWN_CHARS.indexOf(c) >= 0)
                result.append('\\');
            result.append(c);
        }
        return result.toString();
    }

    /**
     * @return {@code <@userId>}. Notifies the user only if {@link MentionType#USERS} is allowed
     */
    public static String userMention(String userId) {
        return "<@" + snowflake(userId) + ">";
    }

    /**
     * @return {@code <@&roleId>}. Notifies the role only if {@link MentionType#ROLES} is allowed
     */
    public static String roleMention(String roleId) {
        return "<@&" + snowflake(roleId) + ">";
    }

    /**
     * @return {@code <#channelId>}
     */
    public static String channelMention(String channelId) {
        return "<#" + snowflake(channelId) + ">";
    }

    /**
     * @return a Discord timestamp, for example {@code <t:1618953630:R>}
     */
    public static String timestamp(Instant instant, TimestampStyle style) {
        Objects.requireNonNull(instant, "instant");
        Objects.requireNonNull(style, "style");
        return "<t:" + instant.getEpochSecond() + ":" + style.code + ">";
    }

    private static String snowflake(String id) {
        Objects.requireNonNull(id, "id");
        if (!SNOWFLAKE.matcher(id).matches())
            throw new IllegalArgumentException("Not a Discord id: " + id);
        return id;
    }
}

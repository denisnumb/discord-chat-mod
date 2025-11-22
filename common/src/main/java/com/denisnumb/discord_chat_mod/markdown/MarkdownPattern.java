package com.denisnumb.discord_chat_mod.markdown;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

public class MarkdownPattern{
    public static final Pattern LINK = Pattern.compile("(?<!\\\\)\\[(.+?)\\]\\((https?://\\S+)\\)");
    public static final Pattern UNDERLINED_ITALIC = Pattern.compile("►(?<!\\\\)_(.+?)(?<!\\\\)_►");
    public static final Pattern UNDERLINED = Pattern.compile("(?<!\\\\)►(.+?)(?<!\\\\)►");
    public static final Pattern ITALIC_underline = Pattern.compile("(?<!\\\\)_(.+?)(?<!\\\\)_");
    public static final Pattern BOLD_ITALIC = Pattern.compile("▬(?<!\\\\)\\*(.+?)(?<!\\\\)\\*▬");
    public static final Pattern BOLD = Pattern.compile("(?<!\\\\)▬(.+?)(?<!\\\\)▬");
    public static final Pattern ITALIC_star = Pattern.compile("(?<!\\\\)\\*(.+?)(?<!\\\\)\\*");
    public static final Pattern STRIKETHROUGH = Pattern.compile("(?<!\\\\)~(?<!\\\\)~(.+?)(?<!\\\\)~(?<!\\\\)~");
    public static final Pattern OBFUSCATED = Pattern.compile("(?<!\\\\)\\|(?<!\\\\)\\|(.+?)(?<!\\\\)\\|(?<!\\\\)\\|");
    public static final Pattern URL = Pattern.compile("(https?://\\S+)");
    public static final Pattern DISCORD_MENTION = Pattern.compile("(?<!\\\\)<((?<!\\\\)([@#][!&]?\\d+)|(:.+?:\\d+))(?<!\\\\)>");
    public static final Pattern EMOJI = Pattern.compile(":[a-zA-Z0-9_]{2,}(~([1-9][0-9]*))?:");
    public static final Pattern COLOR_RANGE = Pattern.compile("(?<!\\\\)<([a-zA-Z]+|#[0-9a-fA-F]{3}|#[0-9a-fA-F]{6})(?<!\\\\)>(.+?)(?<!\\\\)<\\1(?<!\\\\)/(?<!\\\\)>");
    public static final Pattern COLOR_SINGLE = Pattern.compile("(?<!\\\\)<([a-zA-Z]+|#[0-9a-fA-F]{3}|#[0-9a-fA-F]{6})(?<!\\\\)/(?<!\\\\)>(\\S+)");
    public static final Pattern COLOR_OPEN = Pattern.compile("(?<!\\\\)<([a-zA-Z]+|#[0-9a-fA-F]{3}|#[0-9a-fA-F]{6})(?<!\\\\)>(.*?)(?=(?:<|$))");

    public static final HashMap<Pattern, MarkdownStyle> withStyle = new LinkedHashMap<>() {{
        put(LINK, MarkdownStyle.LINK);
        put(UNDERLINED_ITALIC, MarkdownStyle.UNDERLINED_ITALIC);
        put(UNDERLINED, MarkdownStyle.UNDERLINED);
        put(ITALIC_underline, MarkdownStyle.ITALIC_underline);
        put(BOLD_ITALIC, MarkdownStyle.BOLD_ITALIC);
        put(BOLD, MarkdownStyle.BOLD);
        put(ITALIC_star, MarkdownStyle.ITALIC_star);
        put(STRIKETHROUGH, MarkdownStyle.STRIKETHROUGH);
        put(OBFUSCATED, MarkdownStyle.OBFUSCATED);
        put(URL, MarkdownStyle.URL);
        put(DISCORD_MENTION, MarkdownStyle.DISCORD_MENTION);
        put(EMOJI, MarkdownStyle.EMOJI);
        put(COLOR_RANGE, MarkdownStyle.COLOR_RANGE);
        put(COLOR_SINGLE, MarkdownStyle.COLOR_SINGLE);
        put(COLOR_OPEN, MarkdownStyle.COLOR_OPEN);
    }};

    public static boolean isStyleExceptAnother(MarkdownStyle style, MarkdownStyle another){
        if (style == MarkdownStyle.COLOR_RANGE)
            return another == MarkdownStyle.COLOR_OPEN || another == MarkdownStyle.COLOR_SINGLE;
        if (style == MarkdownStyle.UNDERLINED_ITALIC)
            return another == MarkdownStyle.UNDERLINED || another == MarkdownStyle.ITALIC_underline;
        if (style == MarkdownStyle.BOLD_ITALIC)
            return another == MarkdownStyle.BOLD || another == MarkdownStyle.ITALIC_star;
        return false;
    }
}

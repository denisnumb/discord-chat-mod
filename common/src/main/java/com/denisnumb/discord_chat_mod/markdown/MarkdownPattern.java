package com.denisnumb.discord_chat_mod.markdown;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class MarkdownPattern{
    private MarkdownPattern() {}

    public static final Pattern ESCAPED = Pattern.compile("\\\\([*_~|@><])");
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
    public static final Pattern GRADIENT_RANGE = Pattern.compile(
            "(?<!\\\\)<((?:[a-zA-Z]+|#[0-9a-fA-F]{3}|#[0-9a-fA-F]{6})(?:;(?:[a-zA-Z]+|#[0-9a-fA-F]{3}|#[0-9a-fA-F]{6})){1,9})(?<!\\\\)>(.+?)(?<!\\\\)</(?<!\\\\)>"
    );
    public static final Pattern GRADIENT_SINGLE = Pattern.compile(
            "(?<!\\\\)<((?:[a-zA-Z]+|#[0-9a-fA-F]{3}|#[0-9a-fA-F]{6})(?:;(?:[a-zA-Z]+|#[0-9a-fA-F]{3}|#[0-9a-fA-F]{6})){1,9})(?<!\\\\)/(?<!\\\\)>(\\S+)"
    );
    public static final Pattern GRADIENT_OPEN = Pattern.compile(
            "(?<!\\\\)<((?:[a-zA-Z]+|#[0-9a-fA-F]{3}|#[0-9a-fA-F]{6})(?:;(?:[a-zA-Z]+|#[0-9a-fA-F]{3}|#[0-9a-fA-F]{6})){1,9})(?<!\\\\)>(.*?)(?=(?:<|$))"
    );

    public static final Map<Pattern, MarkdownStyle> withStyle;

    static {
        Map<Pattern, MarkdownStyle> map = new LinkedHashMap<>();
        map.put(ESCAPED, MarkdownStyle.ESCAPED);
        map.put(LINK, MarkdownStyle.LINK);
        map.put(UNDERLINED_ITALIC, MarkdownStyle.UNDERLINED_ITALIC);
        map.put(UNDERLINED, MarkdownStyle.UNDERLINED);
        map.put(ITALIC_underline, MarkdownStyle.ITALIC_underline);
        map.put(BOLD_ITALIC, MarkdownStyle.BOLD_ITALIC);
        map.put(BOLD, MarkdownStyle.BOLD);
        map.put(ITALIC_star, MarkdownStyle.ITALIC_star);
        map.put(STRIKETHROUGH, MarkdownStyle.STRIKETHROUGH);
        map.put(OBFUSCATED, MarkdownStyle.OBFUSCATED);
        map.put(URL, MarkdownStyle.URL);
        map.put(DISCORD_MENTION, MarkdownStyle.DISCORD_MENTION);
        map.put(EMOJI, MarkdownStyle.EMOJI);
        map.put(GRADIENT_RANGE, MarkdownStyle.GRADIENT_RANGE);
        map.put(GRADIENT_SINGLE, MarkdownStyle.GRADIENT_SINGLE);
        map.put(GRADIENT_OPEN, MarkdownStyle.GRADIENT_OPEN);
        map.put(COLOR_RANGE, MarkdownStyle.COLOR_RANGE);
        map.put(COLOR_SINGLE, MarkdownStyle.COLOR_SINGLE);
        map.put(COLOR_OPEN, MarkdownStyle.COLOR_OPEN);
        withStyle = Collections.unmodifiableMap(map);
    }

    public static boolean isStyleExceptAnother(MarkdownStyle style, MarkdownStyle another){
        if (style == MarkdownStyle.GRADIENT_RANGE){
            return another == MarkdownStyle.GRADIENT_SINGLE
                    || another == MarkdownStyle.GRADIENT_OPEN
                    || another == MarkdownStyle.COLOR_RANGE
                    || another == MarkdownStyle.COLOR_OPEN
                    || another == MarkdownStyle.COLOR_SINGLE;
        }
        if (style == MarkdownStyle.COLOR_RANGE)
            return another == MarkdownStyle.COLOR_OPEN || another == MarkdownStyle.COLOR_SINGLE;
        if (style == MarkdownStyle.UNDERLINED_ITALIC)
            return another == MarkdownStyle.UNDERLINED || another == MarkdownStyle.ITALIC_underline;
        if (style == MarkdownStyle.BOLD_ITALIC)
            return another == MarkdownStyle.BOLD || another == MarkdownStyle.ITALIC_star;
        return false;
    }
}

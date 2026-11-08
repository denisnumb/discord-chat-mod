package com.denisnumb.discord_chat_mod.utils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

/**
 * Converts strings with legacy formatting codes (as used by permission mods for prefixes and suffixes) into components.
 * <p>
 * Supported: {@code &}/{@code §} color and format codes ({@code &a}, {@code &l}, {@code &r}, ...),
 * hex colors {@code &#RRGGBB} and the Bukkit-style {@code &x&R&R&G&G&B&B}.
 */
public final class LegacyTextUtils {
    private LegacyTextUtils() {}

    private static final String CODE_CHARS = "&§";

    public static MutableComponent toComponent(String text) {
        MutableComponent result = Component.empty();
        StringBuilder segment = new StringBuilder();
        Style style = Style.EMPTY;

        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (CODE_CHARS.indexOf(c) < 0 || i + 1 >= text.length()) {
                segment.append(c);
                i++;
                continue;
            }

            Integer hexColor = null;
            int consumed = 0;
            char code = Character.toLowerCase(text.charAt(i + 1));

            if (code == '#' && i + 8 <= text.length() && isHex(text, i + 2, 6)) {
                hexColor = Integer.parseInt(text.substring(i + 2, i + 8), 16);
                consumed = 8;
            } else if (code == 'x' && i + 14 <= text.length() && isBukkitHex(text, i + 2)) {
                StringBuilder hex = new StringBuilder();
                for (int j = 0; j < 6; j++)
                    hex.append(text.charAt(i + 3 + j * 2));
                hexColor = Integer.parseInt(hex.toString(), 16);
                consumed = 14;
            }

            if (hexColor != null) {
                flush(result, segment, style);
                style = Style.EMPTY.withColor(TextColor.fromRgb(hexColor));
                i += consumed;
                continue;
            }

            ChatFormatting formatting = ChatFormatting.getByCode(code);
            if (formatting == null) {
                segment.append(c);
                i++;
                continue;
            }

            flush(result, segment, style);
            style = applyFormatting(style, formatting);
            i += 2;
        }

        flush(result, segment, style);
        return result;
    }

    public static String stripFormatting(String text) {
        return toComponent(text).getString();
    }

    private static Style applyFormatting(Style style, ChatFormatting formatting) {
        if (formatting == ChatFormatting.RESET)
            return Style.EMPTY;
        if (formatting.isColor())
            return Style.EMPTY.withColor(formatting);

        return switch (formatting) {
            case BOLD -> style.withBold(true);
            case ITALIC -> style.withItalic(true);
            case UNDERLINE -> style.withUnderlined(true);
            case STRIKETHROUGH -> style.withStrikethrough(true);
            case OBFUSCATED -> style.withObfuscated(true);
            default -> style;
        };
    }

    private static void flush(MutableComponent result, StringBuilder segment, Style style) {
        if (segment.isEmpty())
            return;
        result.append(Component.literal(segment.toString()).withStyle(style));
        segment.setLength(0);
    }

    private static boolean isHex(String text, int from, int length) {
        for (int i = from; i < from + length; i++)
            if (Character.digit(text.charAt(i), 16) < 0)
                return false;
        return true;
    }

    /**
     * Checks the {@code &R&R&G&G&B&B} part after {@code &x}
     */
    private static boolean isBukkitHex(String text, int from) {
        for (int j = 0; j < 6; j++) {
            if (CODE_CHARS.indexOf(text.charAt(from + j * 2)) < 0)
                return false;
            if (Character.digit(text.charAt(from + j * 2 + 1), 16) < 0)
                return false;
        }
        return true;
    }
}

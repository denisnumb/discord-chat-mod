package com.denisnumb.discord_chat_mod;

import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class ColorUtils {
    public static class Color{
        public static final int TRANSPARENT_IMAGE_TAG_COLOR = -0xffffff;
        public static final int CHANNEL_MENTION_COLOR = 0x6974c9;
        public static final int DISCORD_RED_COLOR = 0xE74C3C;
        public static final int DISCORD_GREEN_COLOR = 0x2ECC71;
        public static final int DISCORD_DEFAULT_COLOR = 0x3b3b41;
        public static int CHAT_LINK_COLOR = 0x00b7ff;
    }

    public static final HashMap<String, Integer> colorNameToInt = new HashMap<>() {{
        put("black", ChatFormatting.BLACK.getColor());
        put("darkblue", ChatFormatting.DARK_BLUE.getColor());
        put("darkgreen", ChatFormatting.DARK_GREEN.getColor());
        put("darkaqua", ChatFormatting.DARK_AQUA.getColor());
        put("darkred", ChatFormatting.DARK_RED.getColor());
        put("darkpurple", ChatFormatting.DARK_PURPLE.getColor());
        put("gold", ChatFormatting.GOLD.getColor());
        put("grey", ChatFormatting.GRAY.getColor());
        put("darkgrey", ChatFormatting.DARK_GRAY.getColor());
        put("blue", ChatFormatting.BLUE.getColor());
        put("green", ChatFormatting.GREEN.getColor());
        put("aqua", ChatFormatting.AQUA.getColor());
        put("red", ChatFormatting.RED.getColor());
        put("lightpurple", ChatFormatting.LIGHT_PURPLE.getColor());
        put("yellow", ChatFormatting.YELLOW.getColor());
        put("white", ChatFormatting.WHITE.getColor());
    }};

    public static @Nullable Integer parseColor(String colorNameOrHex){
        String colorNameOrHexLower = colorNameOrHex.toLowerCase();

        if (colorNameToInt.containsKey(colorNameOrHexLower))
            return colorNameToInt.get(colorNameOrHex);

        try {
            return Integer.parseInt(colorNameOrHexLower.substring(1), 16);
        } catch (NumberFormatException ignored){
            return null;
        }
    }

    public static String getHexColor(@Nullable java.awt.Color color){
        if (color == null)
            return "#ffffff";
        return "#" + Integer.toHexString(color.getRGB()).substring(2);
    }

    public static String getHexColor(int color){
        return getHexColor(new java.awt.Color(color));
    }
}

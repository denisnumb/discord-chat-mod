package com.denisnumb.discord_chat_mod.utils;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.RoleColors;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ColorUtils {
    private ColorUtils() {}

    public static class Color{
        public static final int TRANSPARENT_IMAGE_TAG_COLOR = -0xffffff;
        public static final int CHANNEL_MENTION_COLOR = 0x6974c9;
        public static final int DISCORD_RED_COLOR = 0xE74C3C;
        public static final int DISCORD_GREEN_COLOR = 0x2ECC71;
        public static final int DISCORD_DEFAULT_COLOR = 0x3b3b41;
        public static final int DEFAULT_CHAT_LINK_COLOR = 0x00b7ff;
    }

    public static final Map<String, Integer> colorNameToInt;

    static {
        Map<String, Integer> map = new HashMap<>();
        map.put("black", ChatFormatting.BLACK.getColor());
        map.put("darkblue", ChatFormatting.DARK_BLUE.getColor());
        map.put("darkgreen", ChatFormatting.DARK_GREEN.getColor());
        map.put("darkaqua", ChatFormatting.DARK_AQUA.getColor());
        map.put("darkred", ChatFormatting.DARK_RED.getColor());
        map.put("darkpurple", ChatFormatting.DARK_PURPLE.getColor());
        map.put("gold", ChatFormatting.GOLD.getColor());
        map.put("grey", ChatFormatting.GRAY.getColor());
        map.put("darkgrey", ChatFormatting.DARK_GRAY.getColor());
        map.put("blue", ChatFormatting.BLUE.getColor());
        map.put("green", ChatFormatting.GREEN.getColor());
        map.put("aqua", ChatFormatting.AQUA.getColor());
        map.put("red", ChatFormatting.RED.getColor());
        map.put("lightpurple", ChatFormatting.LIGHT_PURPLE.getColor());
        map.put("yellow", ChatFormatting.YELLOW.getColor());
        map.put("white", ChatFormatting.WHITE.getColor());
        colorNameToInt = Collections.unmodifiableMap(map);
    }

    public static @Nullable Integer parseColor(String colorNameOrHex){
        String colorNameOrHexLower = colorNameOrHex.toLowerCase();

        if (colorNameToInt.containsKey(colorNameOrHexLower))
            return colorNameToInt.get(colorNameOrHex);

        try {
            if (!colorNameOrHexLower.startsWith("#")){
                return null;
            }

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

    public static int[] parseRoleColors(RoleColors colors){
        if (colors.isDefault() || colors.isSolid()) {
            int rgb = colors.getPrimaryRaw() != Role.DEFAULT_COLOR_RAW
                    ? colors.getPrimaryRaw()
                    : java.awt.Color.WHITE.getRGB();
            return new int[] { rgb };
        }

        return colors.isHolographic() || colors.getTertiary() != null
                ? new int[] { colors.getPrimaryRaw(), colors.getSecondaryRaw(), colors.getTertiaryRaw() }
                : new int[] { colors.getPrimaryRaw(), colors.getSecondaryRaw() };
    }

    public static int[] parseGradientColors(String colorList){
        String[] parts = colorList.split(";");
        int[] colors = new int[parts.length];
        for (int i = 0; i < parts.length; i++){
            Integer c = parseColor(parts[i]);
            if (c == null)
                return null;

            colors[i] = c;
        }

        return colors;
    }

    public static int interpolateGradient(int[] gradientColors, int segments, double t) {
        if (t <= 0.0) {
            return gradientColors[0];
        }
        if (t >= 1.0) {
            return gradientColors[gradientColors.length - 1];
        }

        double scaled = t * segments;
        int segmentIndex = Math.min((int) scaled, segments - 1);
        double localT = scaled - segmentIndex;

        java.awt.Color from = new java.awt.Color(gradientColors[segmentIndex]);
        java.awt.Color to = new java.awt.Color(gradientColors[segmentIndex + 1]);

        int r = lerp(from.getRed(), to.getRed(), localT);
        int g = lerp(from.getGreen(), to.getGreen(), localT);
        int b = lerp(from.getBlue(), to.getBlue(), localT);

        return new java.awt.Color(r, g, b).getRGB();
    }

    private static int lerp(int start, int end, double t) {
        return (int) Math.round(start + (end - start) * t);
    }
}

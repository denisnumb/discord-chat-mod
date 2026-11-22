package com.denisnumb.discord_chat_mod.compat.webmap;

import java.util.Arrays;
import java.util.Locale;

public enum WebMapProvider {
    BLUEMAP("de.bluecolored.bluemap.api.BlueMapAPI") {
        @Override
        public String getMapId(String dimensionId, String worldName) {
            if (dimensionId.equals(OVERWORLD))
                return sanitizeBlueMapId(worldName);

            String dimensionName = dimensionId.startsWith(MINECRAFT_NAMESPACE)
                    ? dimensionId.substring(MINECRAFT_NAMESPACE.length())
                    : dimensionId;
            return sanitizeBlueMapId(worldName + "_" + dimensionName);
        }
    },

    SQUAREMAP("xyz.jpenilla.squaremap.api.Squaremap") {
        @Override
        public String getMapId(String dimensionId, String worldName) {
            return dimensionId.replace(':', '_');
        }
    },

    DYNMAP("org.dynmap.DynmapCommonAPI") {
        @Override
        public String getMapId(String dimensionId, String worldName) {
            return switch (dimensionId) {
                case OVERWORLD -> worldName;
                case NETHER -> "DIM-1";
                case END -> "DIM1";
                default -> dimensionId.replace(':', '_');
            };
        }
    };

    private static final String MINECRAFT_NAMESPACE = "minecraft:";
    private static final String OVERWORLD = "minecraft:overworld";
    private static final String NETHER = "minecraft:the_nether";
    private static final String END = "minecraft:the_end";

    private static final WebMapProvider FALLBACK = BLUEMAP;
    private static volatile WebMapProvider detected;
    private final String apiClassName;

    WebMapProvider(String apiClassName) {
        this.apiClassName = apiClassName;
    }

    public abstract String getMapId(String dimensionId, String worldName);

    public static WebMapProvider get() {
        if (detected == null)
            detected = Arrays.stream(values())
                    .filter(WebMapProvider::isInstalled)
                    .findFirst()
                    .orElse(FALLBACK);
        return detected;
    }

    private boolean isInstalled() {
        try {
            Class.forName(apiClassName, false, WebMapProvider.class.getClassLoader());
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static String sanitizeBlueMapId(String id) {
        return id.replaceAll("\\W", "_").toLowerCase(Locale.ROOT);
    }
}

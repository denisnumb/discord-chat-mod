package com.denisnumb.discord_chat_mod.discord.utils;

import java.util.Map;
import java.util.Optional;

public class BluemapMapsRegistry {
    private static final String BLUEMAP_API_CLASS = "de.bluecolored.bluemap.api.BlueMapAPI";

    private static volatile Map<String, String> dimensionToMapId = Map.of();
    private static Boolean bluemapAvailable;

    public static void initialize() {
        if (!isBluemapAvailable()) return;
        BluemapApiHook.install();
    }

    public static Optional<String> getMapId(String dimensionId) {
        return Optional.ofNullable(dimensionToMapId.get(dimensionId));
    }

    static void setMappings(Map<String, String> mappings) {
        dimensionToMapId = Map.copyOf(mappings);
    }

    private static boolean isBluemapAvailable() {
        if (bluemapAvailable == null) {
            try {
                Class.forName(BLUEMAP_API_CLASS, false, BluemapMapsRegistry.class.getClassLoader());
                bluemapAvailable = true;
            } catch (ClassNotFoundException e) {
                bluemapAvailable = false;
            }
        }
        return bluemapAvailable;
    }
}

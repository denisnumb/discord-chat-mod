package com.denisnumb.discord_chat_mod.discord.utils;

import com.mojang.logging.LogUtils;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

class BluemapApiHook {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static Consumer<BlueMapAPI> onEnableListener;
    private static Consumer<BlueMapAPI> onDisableListener;

    static void install() {
        if (onEnableListener != null) {
            BlueMapAPI.getInstance().ifPresent(BluemapApiHook::populate);
            return;
        }
        onEnableListener = BluemapApiHook::populate;
        onDisableListener = api -> BluemapMapsRegistry.setMappings(Map.of());
        BlueMapAPI.onEnable(onEnableListener);
        BlueMapAPI.onDisable(onDisableListener);
        BlueMapAPI.getInstance().ifPresent(BluemapApiHook::populate);
    }

    static void uninstall() {
        if (onEnableListener != null) {
            BlueMapAPI.unregisterListener(onEnableListener);
            onEnableListener = null;
        }
        if (onDisableListener != null) {
            BlueMapAPI.unregisterListener(onDisableListener);
            onDisableListener = null;
        }
        BluemapMapsRegistry.setMappings(Map.of());
    }

    private static void populate(BlueMapAPI api) {
        Map<String, String> resolved = new HashMap<>();
        for (BlueMapMap map : api.getMaps()) {
            String dimension = parseDimension(map.getWorld().getId());
            if (dimension == null) {
                LOGGER.warn("BlueMap map \"{}\" has unrecognized world id format \"{}\", skipping",
                        map.getId(), map.getWorld().getId());
                continue;
            }
            resolved.putIfAbsent(dimension, map.getId());
        }
        BluemapMapsRegistry.setMappings(resolved);
    }

    private static String parseDimension(String worldId) {
        if (worldId == null) return null;
        int hash = worldId.indexOf('#');
        if (hash >= 0 && hash < worldId.length() - 1)
            return worldId.substring(hash + 1);
        return null;
    }
}

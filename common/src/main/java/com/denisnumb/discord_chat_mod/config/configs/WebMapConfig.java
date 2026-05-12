package com.denisnumb.discord_chat_mod.config.configs;

import com.electronwill.nightconfig.core.CommentedConfig;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.denisnumb.discord_chat_mod.config.ConfigComments.*;
import static com.denisnumb.discord_chat_mod.config.ConfigDefaults.*;

public class WebMapConfig {
    public static boolean enableXaeroWaypointParsing;
    public static String webMapUrlTemplate;
    public static volatile Map<String, String> webMapDimensions = Map.of();

    public static CommentedConfig loadWebMapConfig(CommentedConfig commonConfig) {
        CommentedConfig existedWebMapConfig = commonConfig.getOrElse("webMapConfig", commonConfig.createSubConfig());
        CommentedConfig webMapConfig = commonConfig.createSubConfig();

        enableXaeroWaypointParsing = existedWebMapConfig.getOrElse("enableXaeroWaypointParsing", ENABLE_XAERO_WAYPOINT_PARSING_DEFAULT);
        webMapConfig.set("enableXaeroWaypointParsing", enableXaeroWaypointParsing);
        webMapConfig.setComment("enableXaeroWaypointParsing", ENABLE_XAERO_WAYPOINT_PARSING_COMMENT);

        webMapUrlTemplate = existedWebMapConfig.getOrElse("webMapUrlTemplate", WEB_MAP_URL_TEMPLATE_DEFAULT);
        webMapConfig.set("webMapUrlTemplate", webMapUrlTemplate);
        webMapConfig.setComment("webMapUrlTemplate", WEB_MAP_URL_TEMPLATE_COMMENT);

        CommentedConfig dimensions = existedWebMapConfig.get("dimensions");
        if (dimensions == null)
            dimensions = defaultDimensionsConfig(commonConfig);
        webMapConfig.set("dimensions", dimensions);
        webMapConfig.setComment("dimensions", WEB_MAP_DIMENSIONS_COMMENT);
        webMapDimensions = readDimensions(dimensions);

        return webMapConfig;
    }

    private static CommentedConfig defaultDimensionsConfig(CommentedConfig parent) {
        CommentedConfig dims = parent.createSubConfig();
        dims.set("minecraft:overworld", "world");
        dims.set("minecraft:the_nether", "world_the_nether");
        dims.set("minecraft:the_end", "world_the_end");
        return dims;
    }

    private static Map<String, String> readDimensions(CommentedConfig dimensions) {
        Map<String, String> result = new LinkedHashMap<>();
        for (CommentedConfig.Entry entry : dimensions.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String s && !s.isBlank())
                result.put(entry.getKey(), s);
        }
        return Map.copyOf(result);
    }
}

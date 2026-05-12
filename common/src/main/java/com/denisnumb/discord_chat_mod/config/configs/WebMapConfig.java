package com.denisnumb.discord_chat_mod.config.configs;

import com.denisnumb.discord_chat_mod.discord.utils.BluemapMapsRegistry;
import com.electronwill.nightconfig.core.CommentedConfig;

import static com.denisnumb.discord_chat_mod.config.ConfigComments.*;
import static com.denisnumb.discord_chat_mod.config.ConfigDefaults.*;

public class WebMapConfig {
    public static boolean enableXaeroWaypointParsing;
    public static String webMapUrlTemplate;

    public static CommentedConfig loadWebMapConfig(CommentedConfig commonConfig) {
        CommentedConfig existedWebMapConfig = commonConfig.getOrElse("webMapConfig", commonConfig.createSubConfig());
        CommentedConfig webMapConfig = commonConfig.createSubConfig();

        enableXaeroWaypointParsing = existedWebMapConfig.getOrElse("enableXaeroWaypointParsing", ENABLE_XAERO_WAYPOINT_PARSING_DEFAULT);
        webMapConfig.set("enableXaeroWaypointParsing", enableXaeroWaypointParsing);
        webMapConfig.setComment("enableXaeroWaypointParsing", ENABLE_XAERO_WAYPOINT_PARSING_COMMENT);

        webMapUrlTemplate = existedWebMapConfig.getOrElse("webMapUrlTemplate", WEB_MAP_URL_TEMPLATE_DEFAULT);
        webMapConfig.set("webMapUrlTemplate", webMapUrlTemplate);
        webMapConfig.setComment("webMapUrlTemplate", WEB_MAP_URL_TEMPLATE_COMMENT);

        BluemapMapsRegistry.initialize();

        return webMapConfig;
    }
}

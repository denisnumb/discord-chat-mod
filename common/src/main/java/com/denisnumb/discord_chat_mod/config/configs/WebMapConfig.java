package com.denisnumb.discord_chat_mod.config.configs;

import com.denisnumb.discord_chat_mod.config.core.ConfigParameter;
import com.denisnumb.discord_chat_mod.config.core.ConfigSection;
import com.denisnumb.discord_chat_mod.discord.chat.template.StringTemplate;
import com.denisnumb.discord_chat_mod.discord.chat.template.TemplateTypes;
import com.electronwill.nightconfig.core.CommentedConfig;

import static com.denisnumb.discord_chat_mod.config.ConfigComments.*;
import static com.denisnumb.discord_chat_mod.config.ConfigDefaults.*;

public final class WebMapConfig extends ConfigSection {
    private WebMapConfig() {}

    public static final ConfigParameter<Boolean, Boolean> FORMAT_XAERO_WAYPOINTS =
            ConfigParameter.ofBoolean("formatXaeroWaypoints")
                    .defaultValue(FORMAT_XAERO_WAYPOINTS_DEFAULT)
                    .comment(FORMAT_XAERO_WAYPOINTS_COMMENT)
                    .build();

    public static final ConfigParameter<String, StringTemplate<TemplateTypes.WebMapLink>> WEB_MAP_LINK_TEMPLATE =
            ConfigParameter.ofString("webMapLinkTemplate")
                    .defaultValue(WEB_MAP_LINK_TEMPLATE_DEFAULT)
                    .comment(WEB_MAP_LINK_TEMPLATE_COMMENT)
                    .normalize(String::trim)
                    .transform(t -> new StringTemplate<TemplateTypes.WebMapLink>(t))
                    .build();

    public static CommentedConfig load(CommentedConfig commonConfig) {
        CommentedConfig webMapConfig = commonConfig.getOrElse("webMap", commonConfig.createSubConfig());
        ConfigSection.loadAll(WebMapConfig.class, webMapConfig, webMapConfig);
        return webMapConfig;
    }
}

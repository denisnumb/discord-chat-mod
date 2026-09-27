package com.denisnumb.discord_chat_mod.config.configs;

import com.denisnumb.discord_chat_mod.config.core.ConfigParameter;
import com.denisnumb.discord_chat_mod.config.core.ConfigSection;
import com.electronwill.nightconfig.core.CommentedConfig;

import static com.denisnumb.discord_chat_mod.config.ConfigComments.*;
import static com.denisnumb.discord_chat_mod.config.ConfigDefaults.*;

public final class DiscordProxyConfig extends ConfigSection {
    private DiscordProxyConfig() {}

    public static final ConfigParameter<String, String> PROXY_HOSTNAME =
            ConfigParameter.ofString("proxyHostname")
                    .defaultValue(PROXY_HOSTNAME_DEFAULT)
                    .comment(PROXY_HOSTNAME_COMMENT)
                    .build();

    public static final ConfigParameter<Integer, Integer> PROXY_PORT =
            ConfigParameter.ofInt("proxyPort")
                    .defaultValue(PROXY_PORT_DEFAULT)
                    .rangeWithComment(0, 65535)
                    .build();

    public static final ConfigParameter<String, String> PROXY_USER =
            ConfigParameter.ofString("proxyUser")
                    .defaultValue(PROXY_USER_DEFAULT)
                    .comment(PROXY_USER_COMMENT)
                    .build();

    public static final ConfigParameter<String, String> PROXY_PASSWORD =
            ConfigParameter.ofString("proxyPassword")
                    .defaultValue(PROXY_PASSWORD_DEFAULT)
                    .build();

    public static CommentedConfig load(CommentedConfig commonConfig) {
        CommentedConfig discordProxyConfig = commonConfig.getOrElse("discordProxyConfig", commonConfig.createSubConfig());
        ConfigSection.loadAll(DiscordProxyConfig.class, discordProxyConfig, discordProxyConfig);

        return discordProxyConfig;
    }
}

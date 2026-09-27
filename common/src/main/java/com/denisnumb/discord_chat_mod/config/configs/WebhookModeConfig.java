package com.denisnumb.discord_chat_mod.config.configs;

import com.denisnumb.discord_chat_mod.config.core.ConfigParameter;
import com.denisnumb.discord_chat_mod.config.core.ConfigSection;
import com.electronwill.nightconfig.core.CommentedConfig;

import static com.denisnumb.discord_chat_mod.config.ConfigComments.*;
import static com.denisnumb.discord_chat_mod.config.ConfigDefaults.*;

public final class WebhookModeConfig extends ConfigSection {
    private WebhookModeConfig() {}

    public static final ConfigParameter<Boolean, Boolean> ENABLE_WEBHOOK_MODE =
            ConfigParameter.ofBoolean("enableWebhookMode")
                    .defaultValue(ENABLE_WEBHOOK_MODE_DEFAULT)
                    .comment(ENABLE_WEBHOOK_MODE_COMMENT)
                    .build();

    public static final ConfigParameter<String, String> WEBHOOK_SERVER_NAME =
            ConfigParameter.ofString("webhookServerName")
                    .defaultValue(WEBHOOK_SERVER_NAME_DEFAULT)
                    .comment(WEBHOOK_SERVER_NAME_COMMENT)
                    .build();

    public static final ConfigParameter<String, String> WEBHOOK_SERVER_AVATAR_URL =
            ConfigParameter.ofString("webhookServerAvatarUrl")
                    .defaultValue(WEBHOOK_SERVER_AVATAR_URL_DEFAULT)
                    .comment(WEBHOOK_SERVER_AVATAR_URL_COMMENT)
                    .build();

    public static final ConfigParameter<Boolean, Boolean> ENABLE_SET_AVATAR_URL_COMMAND =
            ConfigParameter.ofBoolean("enableSetAvatarUrlCommand")
                    .defaultValue(ENABLE_SET_AVATAR_URL_COMMAND_DEFAULT)
                    .comment(ENABLE_SET_AVATAR_URL_COMMAND_COMMENT)
                    .build();

    public static final ConfigParameter<String, String> WEBHOOK_PLAYER_AVATAR_URL =
            ConfigParameter.ofString("webhookPlayerAvatarUrl")
                    .defaultValue(WEBHOOK_PLAYER_AVATAR_URL_DEFAULT)
                    .comment(WEBHOOK_PLAYER_AVATAR_URL_COMMENT)
                    .build();

    public static final ConfigParameter<String, String> WEBHOOK_PLAYER_DEFAULT_AVATAR_URL =
            ConfigParameter.ofString("webhookPlayerDefaultAvatarUrl")
                    .defaultValue(WEBHOOK_PLAYER_DEFAULT_AVATAR_URL_DEFAULT)
                    .comment(WEBHOOK_PLAYER_DEFAULT_AVATAR_URL_COMMENT)
                    .build();

    public static CommentedConfig load(CommentedConfig commonConfig) {
        CommentedConfig webhookModeConfig = commonConfig.getOrElse("webhookModeConfig", commonConfig.createSubConfig());
        ConfigSection.loadAll(WebhookModeConfig.class, webhookModeConfig, webhookModeConfig);

        return webhookModeConfig;
    }
}

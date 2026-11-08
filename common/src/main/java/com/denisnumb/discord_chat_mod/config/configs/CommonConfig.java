package com.denisnumb.discord_chat_mod.config.configs;

import com.denisnumb.discord_chat_mod.config.core.ConfigParameter;
import com.denisnumb.discord_chat_mod.config.core.ConfigSection;
import com.electronwill.nightconfig.core.CommentedConfig;

import java.util.Locale;

import static com.denisnumb.discord_chat_mod.config.ConfigComments.*;
import static com.denisnumb.discord_chat_mod.config.ConfigDefaults.*;

public final class CommonConfig extends ConfigSection {
    private CommonConfig() {}

    public static final ConfigParameter<String, String> DISCORD_BOT_TOKEN =
            ConfigParameter.ofString("discordBotToken")
                    .defaultValue(DISCORD_BOT_TOKEN_DEFAULT)
                    .comment(DISCORD_BOT_TOKEN_COMMENT)
                    .build();

    public static final ConfigParameter<String, String> MOD_LOCALE =
            ConfigParameter.ofString("modLocale")
                    .defaultValue(MOD_LOCALE_DEFAULT)
                    .comment(MOD_LOCALE_COMMENT)
                    .build();

    public static final ConfigParameter<Integer, Integer> UTC_OFFSET_HOURS =
            ConfigParameter.ofInt("utcOffsetHours")
                    .defaultValue(UTC_OFFSET_HOURS_DEFAULT)
                    .comment(UTC_OFFSET_HOURS_COMMENT)
                    .rangeWithComment(-12, 14)
                    .build();

    public static final ConfigParameter<Boolean, Boolean> ENABLE_BOT_PRESENCE_STATUS =
            ConfigParameter.ofBoolean("enableBotPresenceStatus")
                    .defaultValue(ENABLE_BOT_PRESENCE_STATUS_DEFAULT)
                    .comment(ENABLE_BOT_PRESENCE_STATUS_COMMENT)
                    .build();

    public static final ConfigParameter<Boolean, Boolean> MENTION_BOTS =
            ConfigParameter.ofBoolean("mentionBots")
                    .defaultValue(MENTION_BOTS_DEFAULT)
                    .comment(MENTION_BOTS_COMMENT)
                    .build();

    public static final ConfigParameter<String, String> PERMISSIONS_PROVIDER =
            ConfigParameter.ofString("permissionsProvider")
                    .defaultValue(PERMISSIONS_PROVIDER_DEFAULT)
                    .comment(PERMISSIONS_PROVIDER_COMMENT)
                    .normalize(value -> value.trim().toLowerCase(Locale.ROOT))
                    .validator((value, fallback) -> value.matches("^(auto|luckperms|ftbranks|none)$") ? value : fallback)
                    .build();

    public static void load(CommentedConfig commonConfig) {
        ConfigSection.loadAll(CommonConfig.class, commonConfig, commonConfig);
    }
}

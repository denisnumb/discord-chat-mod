package com.denisnumb.discord_chat_mod.config.configs;

import com.denisnumb.discord_chat_mod.config.core.ConfigParameter;
import com.denisnumb.discord_chat_mod.config.core.ConfigSection;
import com.electronwill.nightconfig.core.CommentedConfig;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static com.denisnumb.discord_chat_mod.config.ConfigComments.*;
import static com.denisnumb.discord_chat_mod.config.ConfigDefaults.*;

/**
 * {@code [api]} section: settings of the public API for other mods.
 */
public final class ApiConfig extends ConfigSection {
    private ApiConfig() {}

    public static final String SECTION_KEY = "api";

    public static final ConfigParameter<Boolean, Boolean> ENABLED =
            ConfigParameter.ofBoolean("enabled")
                    .defaultValue(API_ENABLED_DEFAULT)
                    .comment(API_ENABLED_COMMENT)
                    .build();

    public static final ConfigParameter<List<String>, Set<String>> DISABLED_MODS =
            ConfigParameter.<String>ofList("disabledMods")
                    .defaultValue(API_DISABLED_MODS_DEFAULT)
                    .comment(API_DISABLED_MODS_COMMENT)
                    .transform(mods -> mods.stream()
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(s -> s.toLowerCase(Locale.ROOT))
                            .collect(Collectors.toUnmodifiableSet())
                    )
                    .build();

    public static final ConfigParameter<Integer, Integer> MAX_MESSAGES_PER_MINUTE_PER_MOD =
            ConfigParameter.ofInt("maxMessagesPerMinutePerMod")
                    .defaultValue(API_MAX_MESSAGES_PER_MINUTE_DEFAULT)
                    .comment(API_MAX_MESSAGES_PER_MINUTE_COMMENT)
                    .range(0, 10_000)
                    .build();

    public static final ConfigParameter<Boolean, Boolean> ALLOW_EVERYONE_MENTIONS =
            ConfigParameter.ofBoolean("allowEveryoneMentions")
                    .defaultValue(API_ALLOW_EVERYONE_MENTIONS_DEFAULT)
                    .comment(API_ALLOW_EVERYONE_MENTIONS_COMMENT)
                    .build();

    public static final ConfigParameter<Boolean, Boolean> LOG_API_MESSAGES =
            ConfigParameter.ofBoolean("logApiMessages")
                    .defaultValue(API_LOG_MESSAGES_DEFAULT)
                    .comment(API_LOG_MESSAGES_COMMENT)
                    .build();

    public static CommentedConfig load(CommentedConfig commonConfig) {
        CommentedConfig apiConfig = commonConfig.getOrElse(SECTION_KEY, commonConfig.createSubConfig());
        ConfigSection.loadAll(ApiConfig.class, apiConfig, apiConfig);
        return apiConfig;
    }
}

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

public final class LogsConfig extends ConfigSection {
    private LogsConfig() {}

    public static final ConfigParameter<Boolean, Boolean> LOG_DISCORD_MESSAGES =
            ConfigParameter.ofBoolean("logDiscordMessages")
                    .defaultValue(LOG_DISCORD_MESSAGES_DEFAULT)
                    .comment(LOG_DISCORD_MESSAGES_COMMENT)
                    .migrateFromParent("logDiscordMessages")
                    .build();

    public static final ConfigParameter<Boolean, Boolean> LOG_DISCORD_ERRORS_TO_SERVER_CHAT =
            ConfigParameter.ofBoolean("logDiscordErrorsToServerChat")
                    .defaultValue(LOG_DISCORD_ERRORS_TO_SERVER_CHAT_DEFAULT)
                    .comment(LOG_DISCORD_ERRORS_TO_SERVER_CHAT_COMMENT)
                    .migrateFromParent("logDiscordErrorsToServerChat")
                    .build();

    public static final ConfigParameter<String, String> DISCORD_ERRORS_CHAT_PLAYER_SELECTOR =
            ConfigParameter.ofString("discordErrorsChatPlayerSelector")
                    .defaultValue(DISCORD_ERRORS_CHAT_PLAYER_SELECTOR_DEFAULT)
                    .comment(DISCORD_ERRORS_CHAT_PLAYER_SELECTOR_COMMENT)
                    .migrateFromParent("discordErrorsChatPlayerSelector")
                    .build();

    public static final ConfigParameter<Boolean, Boolean> SERVER_LOGS_TO_DISCORD_ENABLED =
            ConfigParameter.ofBoolean("serverLogsToDiscordEnabled")
                    .defaultValue(SERVER_LOGS_TO_DISCORD_ENABLED_DEFAULT)
                    .comment(SERVER_LOGS_TO_DISCORD_ENABLED_COMMENT)
                    .migrateFromParent("serverLogsToDiscordEnabled")
                    .build();

    public static final ConfigParameter<String, String> SERVER_LOGS_TO_DISCORD_LOGGING_LEVEL =
            ConfigParameter.ofString("serverLogsToDiscordLoggingLevel")
                    .defaultValue(SERVER_LOGS_TO_DISCORD_LOGGING_LEVEL_DEFAULT)
                    .comment(SERVER_LOGS_TO_DISCORD_LOGGING_LEVEL_COMMENT)
                    .validator((value, fallback) -> value.matches("(?i)^(INFO|WARN|ERROR)$") ? value : fallback)
                    .migrateFromParent("serverLogsToDiscordLoggingLevel")
                    .build();

    public static final ConfigParameter<String, String> SERVER_LOGS_PATTERN =
            ConfigParameter.ofString("serverLogsPattern")
                    .defaultValue(SERVER_LOGS_PATTERN_DEFAULT)
                    .comment(SERVER_LOGS_PATTERN_COMMENT)
                    .migrateFromParent("serverLogsPattern")
                    .build();

    public static final ConfigParameter<Boolean, Boolean> COMMAND_LOG_ENABLED =
            ConfigParameter.ofBoolean("commandLogEnabled")
                    .defaultValue(COMMAND_LOG_ENABLED_DEFAULT)
                    .comment(COMMAND_LOG_ENABLED_COMMENT)
                    .migrateFromParent("commandLogEnabled")
                    .build();

    public static final ConfigParameter<Integer, Integer> COMMAND_LOG_MIN_PERMISSION_LEVEL =
            ConfigParameter.ofInt("commandLogMinPermissionLevel")
                    .defaultValue(COMMAND_LOG_MIN_PERMISSION_LEVEL_DEFAULT)
                    .comment(COMMAND_LOG_MIN_PERMISSION_LEVEL_COMMENT)
                    .range(0, 4)
                    .migrateFromParent("commandLogMinPermissionLevel")
                    .build();

    public static final ConfigParameter<List<String>, Set<String>> COMMAND_LOG_IGNORED_COMMANDS =
            ConfigParameter.<String>ofList("commandLogIgnoredCommands")
                    .defaultValue(COMMAND_LOG_IGNORED_COMMANDS_DEFAULT)
                    .comment(COMMAND_LOG_IGNORED_COMMANDS_COMMENT)
                    .migrateFromParent("commandLogIgnoredCommands")
                    .transform(commandsList -> commandsList.stream()
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(s -> s.toLowerCase(Locale.ROOT))
                            .collect(Collectors.toUnmodifiableSet())
                    )
                    .build();


    public static CommentedConfig load(CommentedConfig commonConfig) {
        CommentedConfig logsConfig = commonConfig.getOrElse("logsConfig", commonConfig.createSubConfig());
        ConfigSection.loadAll(LogsConfig.class, logsConfig, logsConfig, commonConfig);

        return logsConfig;
    }
}
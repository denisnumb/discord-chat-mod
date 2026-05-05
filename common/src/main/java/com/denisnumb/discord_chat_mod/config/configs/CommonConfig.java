package com.denisnumb.discord_chat_mod.config.configs;

import com.electronwill.nightconfig.core.CommentedConfig;

import static com.denisnumb.discord_chat_mod.config.ConfigComments.*;
import static com.denisnumb.discord_chat_mod.config.ConfigDefaults.*;

public class CommonConfig {
    public static String discordBotToken;
    public static String serverLogsChannelId;
    public static String serverLogsToDiscordLoggingLevel;
    public static boolean logDiscordMessages;
    public static boolean logDiscordErrorsToServerChat;
    public static String discordErrorsChatPlayerSelector;
    public static String modLocale;
    public static int utcOffsetHours;
    public static boolean enableBotPresenceStatus;

    public static void loadCommonConfig(CommentedConfig commonConfig){
        discordBotToken = commonConfig.getOrElse("discordBotToken", DISCORD_BOT_TOKEN_DEFAULT);
        commonConfig.set("discordBotToken", discordBotToken);
        commonConfig.setComment("discordBotToken", DISCORD_BOT_TOKEN_COMMENT);

        serverLogsChannelId = commonConfig.getOrElse("serverLogsChannelId", SERVER_LOGS_CHANNEL_ID_DEFAULT);
        commonConfig.set("serverLogsChannelId", serverLogsChannelId);
        commonConfig.setComment("serverLogsChannelId", SERVER_LOGS_CHANNEL_ID_COMMENT);

        serverLogsToDiscordLoggingLevel = commonConfig.getOrElse("serverLogsToDiscordLoggingLevel", SERVER_LOGS_TO_DISCORD_LOGGING_LEVEL_DEFAULT);
        if (!serverLogsToDiscordLoggingLevel.matches("(?i)^(INFO|WARN|ERROR)$"))
            serverLogsToDiscordLoggingLevel = SERVER_LOGS_TO_DISCORD_LOGGING_LEVEL_DEFAULT;
        commonConfig.set("serverLogsToDiscordLoggingLevel", serverLogsToDiscordLoggingLevel);
        commonConfig.setComment("serverLogsToDiscordLoggingLevel", SERVER_LOGS_TO_DISCORD_LOGGING_LEVEL_COMMENT);

        logDiscordMessages = commonConfig.getOrElse("logDiscordMessages", LOG_DISCORD_MESSAGES_DEFAULT);
        commonConfig.set("logDiscordMessages", logDiscordMessages);
        commonConfig.setComment("logDiscordMessages", LOG_DISCORD_MESSAGES_COMMENT);

        logDiscordErrorsToServerChat = commonConfig.getOrElse("logDiscordErrorsToServerChat", LOG_DISCORD_ERRORS_TO_SERVER_CHAT_DEFAULT);
        commonConfig.set("logDiscordErrorsToServerChat", logDiscordErrorsToServerChat);
        commonConfig.setComment("logDiscordErrorsToServerChat", LOG_DISCORD_ERRORS_TO_SERVER_CHAT_COMMENT);

        discordErrorsChatPlayerSelector = commonConfig.getOrElse("discordErrorsChatPlayerSelector", DISCORD_ERRORS_CHAT_PLAYER_SELECTOR_DEFAULT);
        commonConfig.set("discordErrorsChatPlayerSelector", discordErrorsChatPlayerSelector);
        commonConfig.setComment("discordErrorsChatPlayerSelector", DISCORD_ERRORS_CHAT_PLAYER_SELECTOR_COMMENT);

        modLocale = commonConfig.getOrElse("modLocale", MOD_LOCALE_DEFAULT);
        commonConfig.set("modLocale", modLocale);
        commonConfig.setComment("modLocale", MOD_LOCALE_COMMENT);

        utcOffsetHours = commonConfig.getOrElse("utcOffsetHours", UTC_OFFSET_HOURS_DEFAULT);
        if (utcOffsetHours < -12) utcOffsetHours = -12;
        if (utcOffsetHours > 14) utcOffsetHours = 14;
        commonConfig.set("utcOffsetHours", utcOffsetHours);
        commonConfig.setComment("utcOffsetHours", UTC_OFFSET_HOURS_COMMENT + String.format("\n Default: %d\n Range: -12 ~ 14", UTC_OFFSET_HOURS_DEFAULT));

        enableBotPresenceStatus = commonConfig.getOrElse("enableBotPresenceStatus", ENABLE_BOT_PRESENCE_STATUS_DEFAULT);
        commonConfig.set("enableBotPresenceStatus", enableBotPresenceStatus);
        commonConfig.setComment("enableBotPresenceStatus", ENABLE_BOT_PRESENCE_STATUS_COMMENT);
    }
}

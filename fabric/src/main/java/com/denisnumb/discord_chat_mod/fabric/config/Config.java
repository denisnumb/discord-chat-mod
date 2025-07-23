package com.denisnumb.discord_chat_mod.fabric.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

import static com.denisnumb.discord_chat_mod.config.ConfigComments.*;

public class Config {
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
    private static final Path COMMON_PATH = CONFIG_DIR.resolve("discord_chat_mod-common.toml");
    private static final Path CLIENT_PATH = CONFIG_DIR.resolve("discord_chat_mod-client.toml");

    public static String discordBotToken;
    public static String discordChannelId;
    public static boolean logDiscordMessages;
    public static boolean enablePinnedStatusMessage;
    public static boolean logDiscordErrorsToServerChat;
    public static String discordErrorsChatPlayerSelector;
    public static String modLocale;

    public static boolean enableWebhookMode;
    public static String webhookServerName;
    public static String webhookServerAvatarUrl;
    public static boolean enableSetAvatarUrlCommand;
    public static String webhookPlayerAvatarUrl;
    public static String webhookPlayerDefaultAvatarUrl;

    public static String proxyHostname;
    public static int proxyPort;
    public static String proxyUser;
    public static String proxyPassword;

    public static boolean duplicateMessages;
    public static String pinnedStatusMessageChannelId;
    public static String deathsChannelId;
    public static String advancementsChannelId;
    public static String serverStartStopChannelId;
    public static String playerJoinLeaveChannelId;
    public static String playerChatMessagesChannelId;
    public static String screenshotsChannelId;
    public static String tellrawChannelId;
    public static String sayChannelId;

    public static boolean emojifulCompatibility;
    public static int maxChatHistory;

    public static void load() {
        loadCommon();

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            loadClient();
        }
    }

    private static void loadCommon() {
        CommentedFileConfig commonConfig = CommentedFileConfig.builder(COMMON_PATH)
                .autosave()
                .preserveInsertionOrder()
                .sync()
                .build();

        commonConfig.load();

        discordBotToken = commonConfig.getOrElse("discordBotToken", "");
        commonConfig.set("discordBotToken", discordBotToken);
        commonConfig.setComment("discordBotToken", DISCORD_BOT_TOKEN_COMMENT);

        discordChannelId = commonConfig.getOrElse("discordChannelId", "");
        commonConfig.set("discordChannelId", discordChannelId);
        commonConfig.setComment("discordChannelId", DISCORD_CHANNEL_ID_COMMENT);

        logDiscordMessages = commonConfig.getOrElse("logDiscordMessages", true);
        commonConfig.set("logDiscordMessages", logDiscordMessages);
        commonConfig.setComment("logDiscordMessages", LOG_DISCORD_MESSAGES_COMMENT);

        enablePinnedStatusMessage = commonConfig.getOrElse("enablePinnedStatusMessage", true);
        commonConfig.set("enablePinnedStatusMessage", enablePinnedStatusMessage);
        commonConfig.setComment("enablePinnedStatusMessage", ENABLE_PINNED_STATUS_MESSAGE_COMMENT);

        logDiscordErrorsToServerChat = commonConfig.getOrElse("logDiscordErrorsToServerChat", true);
        commonConfig.set("logDiscordErrorsToServerChat", logDiscordErrorsToServerChat);
        commonConfig.setComment("logDiscordErrorsToServerChat", LOG_DISCORD_ERRORS_TO_SERVER_CHAT_COMMENT);

        discordErrorsChatPlayerSelector = commonConfig.getOrElse("discordErrorsChatPlayerSelector", "@a");
        commonConfig.set("discordErrorsChatPlayerSelector", discordErrorsChatPlayerSelector);
        commonConfig.setComment("discordErrorsChatPlayerSelector", DISCORD_ERRORS_CHAT_PLAYER_SELECTOR_COMMENT);

        modLocale = commonConfig.getOrElse("modLocale", "en_us");
        commonConfig.set("modLocale", modLocale);
        commonConfig.setComment("modLocale", MOD_LOCALE_COMMENT);

        CommentedConfig existedWebhookModeConfig = commonConfig.getOrElse("webhookModeConfig", commonConfig.createSubConfig());
        CommentedConfig webhookModeConfig = commonConfig.createSubConfig();

        enableWebhookMode = existedWebhookModeConfig.getOrElse("enableWebhookMode", false);
        webhookModeConfig.set("enableWebhookMode", enableWebhookMode);
        webhookModeConfig.setComment("enableWebhookMode", ENABLE_WEBHOOK_MODE_COMMENT);

        webhookServerName = existedWebhookModeConfig.getOrElse("webhookServerName", "Minecraft Server");
        webhookModeConfig.set("webhookServerName", webhookServerName);
        webhookModeConfig.setComment("webhookServerName", WEBHOOK_SERVER_NAME_COMMENT);

        webhookServerAvatarUrl = existedWebhookModeConfig.getOrElse("webhookServerAvatarUrl", "");
        webhookModeConfig.set("webhookServerAvatarUrl", webhookServerAvatarUrl);
        webhookModeConfig.setComment("webhookServerAvatarUrl", WEBHOOK_SERVER_AVATAR_URL_COMMENT);

        enableSetAvatarUrlCommand = existedWebhookModeConfig.getOrElse("enableSetAvatarUrlCommand", true);
        webhookModeConfig.set("enableSetAvatarUrlCommand", enableSetAvatarUrlCommand);
        webhookModeConfig.setComment("enableSetAvatarUrlCommand", ENABLE_SET_AVATAR_URL_COMMAND_COMMENT);

        webhookPlayerAvatarUrl = existedWebhookModeConfig.getOrElse("webhookPlayerAvatarUrl", "https://mc-heads.net/avatar/<name>.png");
        webhookModeConfig.set("webhookPlayerAvatarUrl", webhookPlayerAvatarUrl);
        webhookModeConfig.setComment("webhookPlayerAvatarUrl", WEBHOOK_PLAYER_AVATAR_URL_COMMENT);

        webhookPlayerDefaultAvatarUrl = existedWebhookModeConfig.getOrElse("webhookPlayerDefaultAvatarUrl", "https://mc-heads.net/avatar.png");
        webhookModeConfig.set("webhookPlayerDefaultAvatarUrl", webhookPlayerDefaultAvatarUrl);
        webhookModeConfig.setComment("webhookPlayerDefaultAvatarUrl", WEBHOOK_PLAYER_DEFAULT_AVATAR_URL_COMMENT);

        commonConfig.set("webhookModeConfig", webhookModeConfig);

        CommentedConfig existedDiscordProxyConfig = commonConfig.getOrElse("discordProxyConfig", commonConfig.createSubConfig());
        CommentedConfig discordProxyConfig = commonConfig.createSubConfig();

        proxyHostname = existedDiscordProxyConfig.getOrElse("proxyHostname", "");
        discordProxyConfig.set("proxyHostname", proxyHostname);
        discordProxyConfig.setComment("proxyHostname", PROXY_HOSTNAME_COMMENT);

        proxyPort = existedDiscordProxyConfig.getOrElse("proxyPort", 1234);
        if (proxyPort < 0 || proxyPort > 65535)
            proxyPort = 1234;
        discordProxyConfig.set("proxyPort", proxyPort);
        discordProxyConfig.setComment("proxyPort", " Default: 1234\n Range: 0 ~ 65535");

        proxyUser = existedDiscordProxyConfig.getOrElse("proxyUser", "");
        discordProxyConfig.set("proxyUser", proxyUser);
        discordProxyConfig.setComment("proxyUser", PROXY_USER_COMMENT);

        proxyPassword = existedDiscordProxyConfig.getOrElse("proxyPassword", "");
        discordProxyConfig.set("proxyPassword", proxyPassword);

        commonConfig.set("discordProxyConfig", discordProxyConfig);

        CommentedConfig existedChannelOverrides = commonConfig.getOrElse("channelOverrides", commonConfig.createSubConfig());
        CommentedConfig channelOverrides = commonConfig.createSubConfig();

        duplicateMessages = existedChannelOverrides.getOrElse("duplicateMessages", false);
        channelOverrides.set("duplicateMessages", duplicateMessages);
        channelOverrides.setComment("duplicateMessages", DUPLICATE_MESSAGES_COMMENT);

        pinnedStatusMessageChannelId = existedChannelOverrides.getOrElse("pinnedStatusMessageChannelId", "");
        channelOverrides.set("pinnedStatusMessageChannelId", pinnedStatusMessageChannelId);
        channelOverrides.setComment("pinnedStatusMessageChannelId", PINNED_STATUS_MESSAGE_CHANNEL_ID_COMMENT);

        deathsChannelId = existedChannelOverrides.getOrElse("deathsChannelId", "");
        channelOverrides.set("deathsChannelId", deathsChannelId);
        channelOverrides.setComment("deathsChannelId", DEATHS_CHANNEL_ID_COMMENT);

        advancementsChannelId = existedChannelOverrides.getOrElse("advancementsChannelId", "");
        channelOverrides.set("advancementsChannelId", advancementsChannelId);
        channelOverrides.setComment("advancementsChannelId", ADVANCEMENTS_CHANNEL_ID_COMMENT);

        serverStartStopChannelId = existedChannelOverrides.getOrElse("serverStartStopChannelId", "");
        channelOverrides.set("serverStartStopChannelId", serverStartStopChannelId);
        channelOverrides.setComment("serverStartStopChannelId", SERVER_START_STOP_CHANNEL_ID_COMMENT);

        playerJoinLeaveChannelId = existedChannelOverrides.getOrElse("playerJoinLeaveChannelId", "");
        channelOverrides.set("playerJoinLeaveChannelId", playerJoinLeaveChannelId);
        channelOverrides.setComment("playerJoinLeaveChannelId", PLAYER_JOIN_LEAVE_CHANNEL_ID_COMMENT);

        playerChatMessagesChannelId = existedChannelOverrides.getOrElse("playerChatMessagesChannelId", "");
        channelOverrides.set("playerChatMessagesChannelId", playerChatMessagesChannelId);
        channelOverrides.setComment("playerChatMessagesChannelId", PLAYER_CHAT_MESSAGES_CHANNEL_ID_COMMENT);

        screenshotsChannelId = existedChannelOverrides.getOrElse("screenshotsChannelId", "");
        channelOverrides.set("screenshotsChannelId", screenshotsChannelId);
        channelOverrides.setComment("screenshotsChannelId", SCREENSHOTS_CHANNEL_ID_COMMENT);

        tellrawChannelId = existedChannelOverrides.getOrElse("tellrawChannelId", "");
        channelOverrides.set("tellrawChannelId", tellrawChannelId);
        channelOverrides.setComment("tellrawChannelId", TELLRAW_CHANNEL_ID_COMMENT);

        sayChannelId = existedChannelOverrides.getOrElse("sayChannelId", "");
        channelOverrides.set("sayChannelId", sayChannelId);
        channelOverrides.setComment("sayChannelId", SAY_CHANNEL_ID_COMMENT);

        commonConfig.set("channelOverrides", channelOverrides);

        commonConfig.save();
    }

    private static void loadClient() {
        CommentedFileConfig clientConfig = CommentedFileConfig.builder(CLIENT_PATH)
                .autosave()
                .preserveInsertionOrder()
                .sync()
                .build();
        clientConfig.load();

        emojifulCompatibility = clientConfig.getOrElse("emojifulCompatibility", false);
        clientConfig.set("emojifulCompatibility", emojifulCompatibility);
        clientConfig.setComment("emojifulCompatibility", EMOJIFUL_COMPATIBILITY_COMMENT);

        maxChatHistory = clientConfig.getOrElse("maxChatHistory", 500);
        if (maxChatHistory < 20)
            maxChatHistory = 20;
        clientConfig.set("maxChatHistory", maxChatHistory);
        clientConfig.setComment("maxChatHistory", MAX_CHAT_HISTORY_COMMENT + "\n Default: 500\n Range: > 20");

        clientConfig.save();
    }
}

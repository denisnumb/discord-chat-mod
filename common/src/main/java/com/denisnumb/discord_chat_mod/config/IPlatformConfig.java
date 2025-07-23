package com.denisnumb.discord_chat_mod.config;

public interface IPlatformConfig {
    String discordBotToken();
    String discordChannelId();
    boolean isDiscordMessagesLoggingEnabled();
    boolean isPinnedStatusMessageEnabled();
    boolean isLoggingDiscordErrorsToServerChatEnabled();
    String discordErrorsChatPlayerSelector();
    String modLocale();

    boolean isWebhookModeEnabled();
    String webhookServerName();
    String webhookServerAvatarUrl();
    boolean isSetAvatarUrlCommandEnabled();
    String webhookPlayerAvatarUrl();
    String webhookPlayerDefaultAvatarUrl();

    String proxyHostname();
    int proxyPort();
    String proxyUser();
    String proxyPassword();

    boolean isDuplicateMessagesEnabled();
    String pinnedStatusMessageChannelId();
    String deathsChannelId();
    String advancementsChannelId();
    String serverStartStopChannelId();
    String playerJoinLeaveChannelId();
    String playerChatMessagesChannelId();
    String screenshotsChannelId();
    String tellrawChannelId();
    String sayChannelId();

    boolean isEmojifulCompatibilityEnabled();
    int maxChatHistory();
}

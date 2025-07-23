package com.denisnumb.discord_chat_mod.fabric.config;

import com.denisnumb.discord_chat_mod.config.IPlatformConfig;

public class FabricConfig implements IPlatformConfig {
    @Override
    public String discordBotToken() {
        return Config.discordBotToken;
    }

    @Override
    public String discordChannelId() {
        return Config.discordChannelId;
    }

    @Override
    public boolean isDiscordMessagesLoggingEnabled() {
        return Config.logDiscordMessages;
    }

    @Override
    public boolean isPinnedStatusMessageEnabled() {
        return Config.enablePinnedStatusMessage;
    }

    @Override
    public boolean isLoggingDiscordErrorsToServerChatEnabled() {
        return Config.logDiscordErrorsToServerChat;
    }

    @Override
    public String discordErrorsChatPlayerSelector() {
        return Config.discordErrorsChatPlayerSelector;
    }

    @Override
    public String modLocale() {
        return Config.modLocale;
    }

    @Override
    public boolean isWebhookModeEnabled() {
        return Config.enableWebhookMode;
    }

    @Override
    public String webhookServerName() {
        return Config.webhookServerName;
    }

    @Override
    public String webhookServerAvatarUrl() {
        return Config.webhookServerAvatarUrl;
    }

    @Override
    public boolean isSetAvatarUrlCommandEnabled() {
        return Config.enableSetAvatarUrlCommand;
    }

    @Override
    public String webhookPlayerAvatarUrl() {
        return Config.webhookPlayerAvatarUrl;
    }

    @Override
    public String webhookPlayerDefaultAvatarUrl() {
        return Config.webhookPlayerDefaultAvatarUrl;
    }

    @Override
    public String proxyHostname() {
        return Config.proxyHostname;
    }

    @Override
    public int proxyPort() {
        return Config.proxyPort;
    }

    @Override
    public String proxyUser() {
        return Config.proxyUser;
    }

    @Override
    public String proxyPassword() {
        return Config.proxyPassword;
    }

    @Override
    public boolean isDuplicateMessagesEnabled() {
        return Config.duplicateMessages;
    }

    @Override
    public String pinnedStatusMessageChannelId() {
        return Config.pinnedStatusMessageChannelId;
    }

    @Override
    public String deathsChannelId() {
        return Config.deathsChannelId;
    }

    @Override
    public String advancementsChannelId() {
        return Config.advancementsChannelId;
    }

    @Override
    public String serverStartStopChannelId() {
        return Config.serverStartStopChannelId;
    }

    @Override
    public String playerJoinLeaveChannelId() {
        return Config.playerJoinLeaveChannelId;
    }

    @Override
    public String playerChatMessagesChannelId() {
        return Config.playerChatMessagesChannelId;
    }

    @Override
    public String screenshotsChannelId() {
        return Config.screenshotsChannelId;
    }

    @Override
    public String tellrawChannelId() {
        return Config.tellrawChannelId;
    }

    @Override
    public String sayChannelId() {
        return Config.sayChannelId;
    }

    @Override
    public boolean isEmojifulCompatibilityEnabled() {
        return Config.emojifulCompatibility;
    }

    @Override
    public int maxChatHistory() {
        return Config.maxChatHistory;
    }
}

package com.denisnumb.discord_chat_mod.neoforge.config;

import com.denisnumb.discord_chat_mod.config.IPlatformConfig;

public class NeoForgeConfig implements IPlatformConfig {
    @Override
    public String discordBotToken() {
        return Config.DISCORD_BOT_TOKEN.get();
    }

    @Override
    public String discordChannelId() {
        return Config.DISCORD_CHANNEL_ID.get();
    }

    @Override
    public boolean isDiscordMessagesLoggingEnabled() {
        return Config.LOG_DISCORD_MESSAGES.get();
    }

    @Override
    public boolean isPinnedStatusMessageEnabled() {
        return Config.ENABLE_PINNED_STATUS_MESSAGE.get();
    }

    @Override
    public boolean isLoggingDiscordErrorsToServerChatEnabled() {
        return Config.LOG_DISCORD_ERRORS_TO_SERVER_CHAT.get();
    }

    @Override
    public String discordErrorsChatPlayerSelector() {
        return Config.DISCORD_ERRORS_CHAT_PLAYER_SELECTOR.get();
    }

    @Override
    public String modLocale() {
        return Config.MOD_LOCALE.get();
    }

    @Override
    public boolean isWebhookModeEnabled() {
        return Config.ENABLE_WEBHOOK_MODE.get();
    }

    @Override
    public String webhookServerName() {
        return Config.WEBHOOK_SERVER_NAME.get();
    }

    @Override
    public String webhookServerAvatarUrl() {
        return Config.WEBHOOK_SERVER_AVATAR_URL.get();
    }

    @Override
    public boolean isSetAvatarUrlCommandEnabled() {
        return Config.ENABLE_SET_AVATAR_URL_COMMAND.get();
    }

    @Override
    public String webhookPlayerAvatarUrl() {
        return Config.WEBHOOK_PLAYER_AVATAR_URL.get();
    }

    @Override
    public String webhookPlayerDefaultAvatarUrl() {
        return Config.WEBHOOK_PLAYER_DEFAULT_AVATAR_URL.get();
    }

    @Override
    public String proxyHostname() {
        return Config.PROXY_HOSTNAME.get();
    }

    @Override
    public int proxyPort() {
        return Config.PROXY_PORT.get();
    }

    @Override
    public String proxyUser() {
        return Config.PROXY_USER.get();
    }

    @Override
    public String proxyPassword() {
        return Config.PROXY_PASSWORD.get();
    }

    @Override
    public boolean isDuplicateMessagesEnabled() {
        return Config.DUPLICATE_MESSAGES.get();
    }

    @Override
    public String pinnedStatusMessageChannelId() {
        return Config.PINNED_STATUS_MESSAGE_CHANNEL_ID.get();
    }

    @Override
    public String deathsChannelId() {
        return Config.DEATHS_CHANNEL_ID.get();
    }

    @Override
    public String advancementsChannelId() {
        return Config.ADVANCEMENTS_CHANNEL_ID.get();
    }

    @Override
    public String serverStartStopChannelId() {
        return Config.SERVER_START_STOP_CHANNEL_ID.get();
    }

    @Override
    public String playerJoinLeaveChannelId() {
        return Config.PLAYER_JOIN_LEAVE_CHANNEL_ID.get();
    }

    @Override
    public String playerChatMessagesChannelId() {
        return Config.PLAYER_CHAT_MESSAGES_CHANNEL_ID.get();
    }

    @Override
    public String screenshotsChannelId() {
        return Config.SCREENSHOTS_CHANNEL_ID.get();
    }

    @Override
    public String tellrawChannelId() {
        return Config.TELLRAW_CHANNEL_ID.get();
    }

    @Override
    public String sayChannelId() {
        return Config.SAY_CHANNEL_ID.get();
    }

    @Override
    public boolean isEmojifulCompatibilityEnabled() {
        return Config.EMOJIFUL_COMPATIBILITY.get();
    }

    @Override
    public int maxChatHistory() {
        return Config.MAX_CHAT_HISTORY.get();
    }
}

package com.denisnumb.discord_chat_mod.config;

import com.denisnumb.discord_chat_mod.config.configs.*;
import com.denisnumb.discord_chat_mod.config.configs.DiscordGuildsConfig.DiscordGuildConfig;

import java.util.List;
import java.util.Set;

public class ConfigProviderImpl implements IConfigProvider {
    @Override
    public List<DiscordGuildConfig> discordGuildConfigs() {
        return DiscordGuildsConfig.discordGuildConfigs;
    }

    @Override
    public String discordBotToken() {
        return CommonConfig.discordBotToken;
    }

    @Override
    public String modLocale() {
        return CommonConfig.modLocale;
    }

    @Override
    public int utcOffsetHours() {
        return CommonConfig.utcOffsetHours;
    }

    @Override
    public boolean isBotPresenceStatusEnabled() {
        return CommonConfig.enableBotPresenceStatus;
    }

    @Override
    public boolean mentionBots() {
        return CommonConfig.mentionBots;
    }

    @Override
    public boolean isDiscordMessagesLoggingEnabled() {
        return LogsConfig.logDiscordMessages;
    }

    @Override
    public boolean isLoggingDiscordErrorsToServerChatEnabled() {
        return LogsConfig.logDiscordErrorsToServerChat;
    }

    @Override
    public String discordErrorsChatPlayerSelector() {
        return LogsConfig.discordErrorsChatPlayerSelector;
    }

    @Override
    public boolean isServerLogsToDiscordEnabled() {
        return LogsConfig.serverLogsToDiscordEnabled;
    }

    @Override
    public String serverLogsToDiscordLoggingLevel() {
        return LogsConfig.serverLogsToDiscordLoggingLevel;
    }

    @Override
    public String serverLogsPattern() {
        return LogsConfig.serverLogsPattern;
    }

    @Override
    public boolean isCommandLogEnabled() {
        return LogsConfig.commandLogEnabled;
    }

    @Override
    public int commandLogMinPermissionLevel() {
        return LogsConfig.commandLogMinPermissionLevel;
    }

    @Override
    public Set<String> commandLogIgnoredCommands() {
        return LogsConfig.commandLogIgnoredCommands;
    }

    @Override
    public boolean isWebhookModeEnabled() {
        return WebhookModeConfig.enableWebhookMode;
    }

    @Override
    public String webhookServerName() {
        return WebhookModeConfig.webhookServerName;
    }

    @Override
    public String webhookServerAvatarUrl() {
        return WebhookModeConfig.webhookServerAvatarUrl;
    }

    @Override
    public boolean isSetAvatarUrlCommandEnabled() {
        return WebhookModeConfig.enableSetAvatarUrlCommand;
    }

    @Override
    public String webhookPlayerAvatarUrl() {
        return WebhookModeConfig.webhookPlayerAvatarUrl;
    }

    @Override
    public String webhookPlayerDefaultAvatarUrl() {
        return WebhookModeConfig.webhookPlayerDefaultAvatarUrl;
    }

    @Override
    public String proxyHostname() {
        return DiscordProxyConfig.proxyHostname;
    }

    @Override
    public int proxyPort() {
        return DiscordProxyConfig.proxyPort;
    }

    @Override
    public String proxyUser() {
        return DiscordProxyConfig.proxyUser;
    }

    @Override
    public String proxyPassword() {
        return DiscordProxyConfig.proxyPassword;
    }

    @Override
    public boolean isMinecraftChatCustomizationEnabled() {
        return MinecraftChatStyleConfig.enableMinecraftChatCustomization;
    }

    @Override
    public String minecraftDiscordMessagesStyle() {
        return MinecraftChatStyleConfig.minecraftDiscordMessagesStyle;
    }

    @Override
    public String minecraftPlayerMessageStyle() {
        return MinecraftChatStyleConfig.minecraftPlayerMessageStyle;
    }

    @Override
    public String minecraftPlayerJoinedStyle() {
        return MinecraftChatStyleConfig.minecraftPlayerJoinedStyle;
    }

    @Override
    public String minecraftPlayerLeftStyle() {
        return MinecraftChatStyleConfig.minecraftPlayerLeftStyle;
    }

    @Override
    public String minecraftPlayerDeathNameStyle() {
        return MinecraftChatStyleConfig.minecraftPlayerDeathNameStyle;
    }

    @Override
    public String minecraftPlayerDeathCauseStyle() {
        return MinecraftChatStyleConfig.minecraftPlayerDeathCauseStyle;
    }

    @Override
    public String minecraftPlayerDeathSecondEntityNameStyle() {
        return MinecraftChatStyleConfig.minecraftPlayerDeathSecondEntityNameStyle;
    }

    @Override
    public String minecraftPlayerDeathWeaponStyle() {
        return MinecraftChatStyleConfig.minecraftPlayerDeathWeaponStyle;
    }

    @Override
    public String minecraftPlayerAdvancementTaskStyle() {
        return MinecraftChatStyleConfig.minecraftPlayerAdvancementTaskStyle;
    }

    @Override
    public String minecraftPlayerAdvancementGoalStyle() {
        return MinecraftChatStyleConfig.minecraftPlayerAdvancementGoalStyle;
    }

    @Override
    public String minecraftPlayerAdvancementChallengeStyle() {
        return MinecraftChatStyleConfig.minecraftPlayerAdvancementChallengeStyle;
    }

    @Override
    public String minecraftTeamMessageSentStyle() {
        return MinecraftChatStyleConfig.minecraftTeamMessageSentStyle;
    }

    @Override
    public String minecraftTeamMessageReceivedStyle() {
        return MinecraftChatStyleConfig.minecraftTeamMessageReceivedStyle;
    }

    @Override
    public String minecraftTellMessageSentStyle() {
        return MinecraftChatStyleConfig.minecraftTellMessageSentStyle;
    }

    @Override
    public String minecraftTellMessageReceivedStyle() {
        return MinecraftChatStyleConfig.minecraftTellMessageReceivedStyle;
    }

    @Override
    public String minecraftSayCommandStyle() {
        return MinecraftChatStyleConfig.minecraftSayCommandStyle;
    }

    @Override
    public String minecraftMeCommandStyle() {
        return MinecraftChatStyleConfig.minecraftMeCommandStyle;
    }

    @Override
    public String discordPlayerMessageStyle() {
        return DiscordChatStyleConfig.discordPlayerMessageStyle;
    }

    @Override
    public String discordPlayerMessageWebhookStyle() {
        return DiscordChatStyleConfig.discordPlayerMessageWebhookStyle;
    }

    @Override
    public String discordPlayerJoinedStyle() {
        return DiscordChatStyleConfig.discordPlayerJoinedStyle;
    }

    @Override
    public String discordPlayerLeftStyle() {
        return DiscordChatStyleConfig.discordPlayerLeftStyle;
    }

    @Override
    public String discordPlayerDeathCauseStyle() {
        return DiscordChatStyleConfig.discordPlayerDeathCauseStyle;
    }

    @Override
    public String discordPlayerDeathNameStyle() {
        return DiscordChatStyleConfig.discordPlayerDeathNameStyle;
    }

    @Override
    public String discordPlayerDeathSecondEntityStyle() {
        return DiscordChatStyleConfig.discordPlayerDeathSecondEntityStyle;
    }

    @Override
    public String discordPlayerDeathWeaponStyle() {
        return DiscordChatStyleConfig.discordPlayerDeathWeaponStyle;
    }

    @Override
    public String discordPlayerDeathMessageStyle() {
        return DiscordChatStyleConfig.discordPlayerDeathMessageStyle;
    }

    @Override
    public String discordPlayerAdvancementTaskStyle() {
        return DiscordChatStyleConfig.discordPlayerAdvancementTaskStyle;
    }

    @Override
    public String discordPlayerAdvancementGoalStyle() {
        return DiscordChatStyleConfig.discordPlayerAdvancementGoalStyle;
    }

    @Override
    public String discordPlayerAdvancementChallengeStyle() {
        return DiscordChatStyleConfig.discordPlayerAdvancementChallengeStyle;
    }

    @Override
    public String discordSayCommandStyle() {
        return DiscordChatStyleConfig.discordSayCommandStyle;
    }

    @Override
    public String discordMeCommandStyle() {
        return DiscordChatStyleConfig.discordMeCommandStyle;
    }

    @Override
    public String discordMeCommandWebhookStyle() {
        return DiscordChatStyleConfig.discordMeCommandWebhookStyle;
    }

    @Override
    public String discordTellrawCommandStyle() {
        return DiscordChatStyleConfig.discordTellrawCommandStyle;
    }

    @Override
    public String discordCommandLogStyle() {
        return DiscordChatStyleConfig.discordCommandLogStyle;
    }

    @Override
    public String discordScreenshotMessageStyle() {
        return DiscordChatStyleConfig.discordScreenshotMessageStyle;
    }

    @Override
    public String discordScreenshotMessageWebhookStyle() {
        return DiscordChatStyleConfig.discordScreenshotMessageWebhookStyle;
    }

    @Override
    public String discordServerStartedMessageStyle() {
        return DiscordChatStyleConfig.discordServerStartedMessageStyle;
    }

    @Override
    public String discordLocalServerStartedMessageStyle() {
        return DiscordChatStyleConfig.discordLocalServerStartedMessageStyle;
    }

    @Override
    public String discordServerClosedMessageStyle() {
        return DiscordChatStyleConfig.discordServerClosedMessageStyle;
    }

    @Override
    public String discordPinnedStatusMessageServerUnavailableStyle() {
        return DiscordChatStyleConfig.discordPinnedStatusMessageServerUnavailableStyle;
    }

    @Override
    public String discordPinnedStatusMessageServerAvailableStyle() {
        return DiscordChatStyleConfig.discordPinnedStatusMessageServerAvailableStyle;
    }

    @Override
    public String discordPinnedStatusMessagePlayerListDelimiter() {
        return DiscordChatStyleConfig.discordPinnedStatusMessagePlayerListDelimiter;
    }

    @Override
    public String discordPinnedStatusMessagePlayerListNicknameStyle() {
        return DiscordChatStyleConfig.discordPinnedStatusMessagePlayerListNicknameStyle;
    }

    @Override
    public String discordPinnedStatusMessageStyle() {
        return DiscordChatStyleConfig.discordPinnedStatusMessageStyle;
    }

    @Override
    public String discordGuildForwardedMessageUserNameStyle() {
        return DiscordChatStyleConfig.discordGuildForwardedMessageUserNameStyle;
    }

    @Override
    public boolean isEmojifulCompatibilityEnabled() {
        return ClientConfig.emojifulCompatibility;
    }

    @Override
    public int maxChatHistory() {
        return ClientConfig.maxChatHistory;
    }
}

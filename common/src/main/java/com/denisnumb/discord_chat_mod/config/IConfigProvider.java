package com.denisnumb.discord_chat_mod.config;

import com.denisnumb.discord_chat_mod.config.configs.DiscordGuildsConfig;

import java.util.List;
import java.util.Set;

public interface IConfigProvider {
    List<DiscordGuildsConfig.DiscordGuildConfig> discordGuildConfigs();

    String discordBotToken();
    String serverLogsChannelId();
    String serverLogsToDiscordLoggingLevel();
    String serverLogsPattern();
    boolean isServerLogsCommandsOnly();
    boolean isDiscordMessagesLoggingEnabled();
    boolean isLoggingDiscordErrorsToServerChatEnabled();
    String discordErrorsChatPlayerSelector();
    String modLocale();
    int utcOffsetHours();
    boolean isBotPresenceStatusEnabled();
    boolean mentionBots();

    boolean isSlashCommandsEnabled();
    java.util.List<String> slashCommandAllowedRoles();

    boolean isCommandLogEnabled();
    int commandLogMinPermissionLevel();
    Set<String> commandLogIgnoredCommands();

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

    boolean isMinecraftChatCustomizationEnabled();
    String minecraftDiscordMessagesStyle();
    String minecraftPlayerMessageStyle();
    String minecraftPlayerJoinedStyle();
    String minecraftPlayerLeftStyle();
    String minecraftPlayerDeathNameStyle();
    String minecraftPlayerDeathCauseStyle();
    String minecraftPlayerDeathSecondEntityNameStyle();
    String minecraftPlayerDeathWeaponStyle();
    String minecraftPlayerAdvancementTaskStyle();
    String minecraftPlayerAdvancementGoalStyle();
    String minecraftPlayerAdvancementChallengeStyle();
    String minecraftTeamMessageSentStyle();
    String minecraftTeamMessageReceivedStyle();
    String minecraftTellMessageSentStyle();
    String minecraftTellMessageReceivedStyle();
    String minecraftSayCommandStyle();
    String minecraftMeCommandStyle();

    String discordPlayerMessageStyle();
    String discordPlayerMessageWebhookStyle();
    String discordPlayerJoinedStyle();
    String discordPlayerLeftStyle();
    String discordPlayerDeathCauseStyle();
    String discordPlayerDeathNameStyle();
    String discordPlayerDeathSecondEntityStyle();
    String discordPlayerDeathWeaponStyle();
    String discordPlayerDeathMessageStyle();
    String discordPlayerAdvancementTaskStyle();
    String discordPlayerAdvancementGoalStyle();
    String discordPlayerAdvancementChallengeStyle();
    String discordSayCommandStyle();
    String discordMeCommandStyle();
    String discordMeCommandWebhookStyle();
    String discordTellrawCommandStyle();
    String discordCommandLogStyle();
    String discordScreenshotMessageStyle();
    String discordScreenshotMessageWebhookStyle();
    String discordServerStartedMessageStyle();
    String discordLocalServerStartedMessageStyle();
    String discordServerClosedMessageStyle();
    String discordPinnedStatusMessageServerUnavailableStyle();
    String discordPinnedStatusMessageServerAvailableStyle();
    String discordPinnedStatusMessagePlayerListDelimiter();
    String discordPinnedStatusMessagePlayerListNicknameStyle();
    String discordPinnedStatusMessageStyle();
    String discordGuildForwardedMessageUserNameStyle();

    boolean isEmojifulCompatibilityEnabled();
    int maxChatHistory();
}

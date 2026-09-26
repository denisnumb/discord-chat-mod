package com.denisnumb.discord_chat_mod.config;

import com.denisnumb.discord_chat_mod.chat.template.MessageTemplate;
import com.denisnumb.discord_chat_mod.chat.template.MessageTypes;
import com.denisnumb.discord_chat_mod.config.configs.DiscordGuildsConfig;

import java.util.List;
import java.util.Set;

public interface IConfigProvider {
    List<DiscordGuildsConfig.DiscordGuildConfig> discordGuildConfigs();

    String discordBotToken();
    String modLocale();
    int utcOffsetHours();
    boolean isBotPresenceStatusEnabled();
    boolean mentionBots();

    boolean isDiscordMessagesLoggingEnabled();
    boolean isLoggingDiscordErrorsToServerChatEnabled();
    String discordErrorsChatPlayerSelector();
    boolean isServerLogsToDiscordEnabled();
    String serverLogsToDiscordLoggingLevel();
    String serverLogsPattern();
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
    int[] minecraftChatLinkColors();
    MessageTemplate<MessageTypes.DiscordMessage> minecraftDiscordMessagesStyle();
    MessageTemplate<MessageTypes.PlayerMessage> minecraftPlayerMessageStyle();
    MessageTemplate<MessageTypes.PlayerJoined> minecraftPlayerJoinedStyle();
    MessageTemplate<MessageTypes.PlayerLeft> minecraftPlayerLeftStyle();
    MessageTemplate<MessageTypes.DiedEntity> minecraftPlayerDeathNameStyle();
    MessageTemplate<MessageTypes.DeathCause> minecraftPlayerDeathCauseStyle();
    MessageTemplate<MessageTypes.KillerEntity> minecraftPlayerDeathSecondEntityNameStyle();
    MessageTemplate<MessageTypes.KillerWeapon> minecraftPlayerDeathWeaponStyle();
    MessageTemplate<MessageTypes.AdvancementMessageType> minecraftPlayerAdvancementTaskStyle();
    MessageTemplate<MessageTypes.AdvancementMessageType> minecraftPlayerAdvancementGoalStyle();
    MessageTemplate<MessageTypes.AdvancementMessageType> minecraftPlayerAdvancementChallengeStyle();
    MessageTemplate<MessageTypes.TeamMessage> minecraftTeamMessageSentStyle();
    MessageTemplate<MessageTypes.TeamMessage> minecraftTeamMessageReceivedStyle();
    MessageTemplate<MessageTypes.TellOutgoingMessage> minecraftTellMessageSentStyle();
    MessageTemplate<MessageTypes.TellIncomingMessage> minecraftTellMessageReceivedStyle();
    MessageTemplate<MessageTypes.PlayerMessage> minecraftSayCommandStyle();
    MessageTemplate<MessageTypes.PlayerMessage> minecraftMeCommandStyle();

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
    String discordImageMessageStyle();
    String discordImageMessageWebhookStyle();
    String discordServerStartedMessageStyle();
    String discordLocalServerStartedMessageStyle();
    String discordServerClosedMessageStyle();
    String discordPinnedStatusMessageServerUnavailableStyle();
    String discordPinnedStatusMessageServerAvailableStyle();
    String discordPinnedStatusMessagePlayerListDelimiter();
    String discordPinnedStatusMessagePlayerListNicknameStyle();
    String discordPinnedStatusMessageStyle();
    String discordGuildForwardedMessageWebhookUsernameStyle();
    String discordGuildForwardedMessageStyle();

    boolean isEmojifulCompatibilityEnabled();
    int maxChatHistory();
    int maxImageCacheSize();
    int imageLoadTimeoutMs();
    boolean isAttachImageButtonEnabled();
    boolean isClipboardImagePasteEnabled();
}

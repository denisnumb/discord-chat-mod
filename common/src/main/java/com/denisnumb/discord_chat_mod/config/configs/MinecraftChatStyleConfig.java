package com.denisnumb.discord_chat_mod.config.configs;

import com.denisnumb.discord_chat_mod.chat.template.MessageTemplate;
import com.denisnumb.discord_chat_mod.chat.template.MessageTypes;
import com.denisnumb.discord_chat_mod.utils.ColorUtils;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.Objects;

import static com.denisnumb.discord_chat_mod.config.ConfigComments.*;
import static com.denisnumb.discord_chat_mod.config.ConfigDefaults.*;
import static com.denisnumb.discord_chat_mod.utils.ColorUtils.parseGradientColors;

public final class MinecraftChatStyleConfig {
    private MinecraftChatStyleConfig() {}
    private static final Logger LOGGER = LogUtils.getLogger();

    public static boolean enableMinecraftChatCustomization;
    public static int[] minecraftChatLinkColors;
    public static MessageTemplate<MessageTypes.DiscordMessage> minecraftDiscordMessagesStyle;
    public static MessageTemplate<MessageTypes.PlayerMessage> minecraftPlayerMessageStyle;
    public static MessageTemplate<MessageTypes.PlayerJoined> minecraftPlayerJoinedStyle;
    public static MessageTemplate<MessageTypes.PlayerLeft> minecraftPlayerLeftStyle;
    public static MessageTemplate<MessageTypes.DiedEntity> minecraftPlayerDeathNameStyle;
    public static MessageTemplate<MessageTypes.DeathCause> minecraftPlayerDeathCauseStyle;
    public static MessageTemplate<MessageTypes.KillerEntity> minecraftPlayerDeathSecondEntityNameStyle;
    public static MessageTemplate<MessageTypes.KillerWeapon> minecraftPlayerDeathWeaponStyle;
    public static MessageTemplate<MessageTypes.AdvancementMessageType> minecraftPlayerAdvancementTaskStyle;
    public static MessageTemplate<MessageTypes.AdvancementMessageType> minecraftPlayerAdvancementGoalStyle;
    public static MessageTemplate<MessageTypes.AdvancementMessageType> minecraftPlayerAdvancementChallengeStyle;
    public static MessageTemplate<MessageTypes.TeamMessage> minecraftTeamMessageSentStyle;
    public static MessageTemplate<MessageTypes.TeamMessage> minecraftTeamMessageReceivedStyle;
    public static MessageTemplate<MessageTypes.TellOutgoingMessage> minecraftTellMessageSentStyle;
    public static MessageTemplate<MessageTypes.TellIncomingMessage> minecraftTellMessageReceivedStyle;
    public static MessageTemplate<MessageTypes.PlayerMessage> minecraftSayCommandStyle;
    public static MessageTemplate<MessageTypes.PlayerMessage> minecraftMeCommandStyle;

    public static CommentedConfig loadMinecraftChatStyleConfig(CommentedConfig commonConfig){
        CommentedConfig existedMinecraftChatStyle = commonConfig.getOrElse("minecraftChatStyle", commonConfig.createSubConfig());
        CommentedConfig minecraftChatStyle = commonConfig.createSubConfig();

        enableMinecraftChatCustomization = existedMinecraftChatStyle.getOrElse("enableMinecraftChatCustomization", ENABLE_MINECRAFT_CHAT_CUSTOMIZATION_DEFAULT);
        minecraftChatStyle.set("enableMinecraftChatCustomization", enableMinecraftChatCustomization);
        minecraftChatStyle.setComment("enableMinecraftChatCustomization", ENABLE_MINECRAFT_CHAT_CUSTOMIZATION_COMMENT);

        String rawColor = existedMinecraftChatStyle.getOrElse("minecraftChatLinkColor", MINECRAFT_CHAT_LINK_COLOR_DEFAULT);
        minecraftChatLinkColors = Objects.requireNonNullElse(parseGradientColors(rawColor), new int[] { ColorUtils.Color.DEFAULT_CHAT_LINK_COLOR });
        minecraftChatStyle.set("minecraftChatLinkColor", rawColor);
        minecraftChatStyle.setComment("minecraftChatLinkColor", MINECRAFT_CHAT_LINK_COLOR_COMMENT);

        minecraftDiscordMessagesStyle = new MessageTemplate<>(
                existedMinecraftChatStyle.getOrElse("minecraftDiscordMessagesStyle", MINECRAFT_DISCORD_MESSAGES_STYLE_DEFAULT),
                new MessageTypes.DiscordMessage()
        );
        minecraftChatStyle.set("minecraftDiscordMessagesStyle", minecraftDiscordMessagesStyle.getRawTemplate());
        minecraftChatStyle.setComment("minecraftDiscordMessagesStyle", MINECRAFT_DISCORD_MESSAGES_STYLE_COMMENT);

        minecraftPlayerMessageStyle = new MessageTemplate<>(
                existedMinecraftChatStyle.getOrElse("minecraftPlayerMessageStyle", MINECRAFT_PLAYER_MESSAGE_STYLE_DEFAULT),
                new MessageTypes.PlayerMessage()
        );
        minecraftChatStyle.set("minecraftPlayerMessageStyle", minecraftPlayerMessageStyle.getRawTemplate());
        minecraftChatStyle.setComment("minecraftPlayerMessageStyle", MINECRAFT_PLAYER_MESSAGE_STYLE_COMMENT);

        minecraftPlayerJoinedStyle = new MessageTemplate<>(
                existedMinecraftChatStyle.getOrElse("minecraftPlayerJoinedStyle", MINECRAFT_PLAYER_JOINED_STYLE_DEFAULT),
                new MessageTypes.PlayerJoined()
        );
        minecraftChatStyle.set("minecraftPlayerJoinedStyle", minecraftPlayerJoinedStyle.getRawTemplate());
        minecraftChatStyle.setComment("minecraftPlayerJoinedStyle", MINECRAFT_PLAYER_JOINED_STYLE_COMMENT);

        minecraftPlayerLeftStyle = new MessageTemplate<>(
                existedMinecraftChatStyle.getOrElse("minecraftPlayerLeftStyle", MINECRAFT_PLAYER_LEFT_STYLE_DEFAULT),
                new MessageTypes.PlayerLeft()
        );
        minecraftChatStyle.set("minecraftPlayerLeftStyle", minecraftPlayerLeftStyle.getRawTemplate());
        minecraftChatStyle.setComment("minecraftPlayerLeftStyle", MINECRAFT_PLAYER_LEFT_STYLE_COMMENT);

        minecraftPlayerDeathCauseStyle = new MessageTemplate<>(
                existedMinecraftChatStyle.getOrElse("minecraftPlayerDeathCauseStyle", MINECRAFT_PLAYER_DEATH_CAUSE_STYLE_DEFAULT),
                new MessageTypes.DeathCause()
        );
        minecraftChatStyle.set("minecraftPlayerDeathCauseStyle", minecraftPlayerDeathCauseStyle.getRawTemplate());
        minecraftChatStyle.setComment("minecraftPlayerDeathCauseStyle", MINECRAFT_PLAYER_DEATH_STYLE_COMMENT);

        minecraftPlayerDeathNameStyle = new MessageTemplate<>(
                existedMinecraftChatStyle.getOrElse("minecraftPlayerDeathNameStyle", MINECRAFT_PLAYER_DEATH_NAME_STYLE_DEFAULT),
                new MessageTypes.DiedEntity()
        );
        minecraftChatStyle.set("minecraftPlayerDeathNameStyle", minecraftPlayerDeathNameStyle.getRawTemplate());

        minecraftPlayerDeathSecondEntityNameStyle = new MessageTemplate<>(
                existedMinecraftChatStyle.getOrElse("minecraftPlayerDeathSecondEntityNameStyle", MINECRAFT_PLAYER_DEATH_SECOND_ENTITY_STYLE_DEFAULT),
                new MessageTypes.KillerEntity()
        );
        minecraftChatStyle.set("minecraftPlayerDeathSecondEntityNameStyle", minecraftPlayerDeathSecondEntityNameStyle.getRawTemplate());

        minecraftPlayerDeathWeaponStyle = new MessageTemplate<>(
                existedMinecraftChatStyle.getOrElse("minecraftPlayerDeathWeaponStyle", MINECRAFT_PLAYER_DEATH_WEAPON_STYLE_DEFAULT),
                new MessageTypes.KillerWeapon()
        );
        minecraftChatStyle.set("minecraftPlayerDeathWeaponStyle", minecraftPlayerDeathWeaponStyle.getRawTemplate());

        minecraftPlayerAdvancementTaskStyle = new MessageTemplate<>(
                existedMinecraftChatStyle.getOrElse("minecraftPlayerAdvancementTaskStyle", MINECRAFT_PLAYER_ADVANCEMENT_TASK_STYLE_DEFAULT),
                new MessageTypes.AdvancementTask()
        );
        minecraftChatStyle.set("minecraftPlayerAdvancementTaskStyle", minecraftPlayerAdvancementTaskStyle.getRawTemplate());
        minecraftChatStyle.setComment("minecraftPlayerAdvancementTaskStyle", MINECRAFT_PLAYER_ADVANCEMENT_TASK_STYLE_COMMENT);

        minecraftPlayerAdvancementGoalStyle = new MessageTemplate<>(
                existedMinecraftChatStyle.getOrElse("minecraftPlayerAdvancementGoalStyle", MINECRAFT_PLAYER_ADVANCEMENT_GOAL_STYLE_DEFAULT),
                new MessageTypes.AdvancementGoal()
        );
        minecraftChatStyle.set("minecraftPlayerAdvancementGoalStyle", minecraftPlayerAdvancementGoalStyle.getRawTemplate());
        minecraftChatStyle.setComment("minecraftPlayerAdvancementGoalStyle", MINECRAFT_PLAYER_ADVANCEMENT_GOAL_STYLE_COMMENT);

        minecraftPlayerAdvancementChallengeStyle = new MessageTemplate<>(
                existedMinecraftChatStyle.getOrElse("minecraftPlayerAdvancementChallengeStyle", MINECRAFT_PLAYER_ADVANCEMENT_CHALLENGE_STYLE_DEFAULT),
                new MessageTypes.AdvancementChallenge()
        );
        minecraftChatStyle.set("minecraftPlayerAdvancementChallengeStyle", minecraftPlayerAdvancementChallengeStyle.getRawTemplate());
        minecraftChatStyle.setComment("minecraftPlayerAdvancementChallengeStyle", MINECRAFT_PLAYER_ADVANCEMENT_CHALLENGE_STYLE_COMMENT);

        minecraftTeamMessageSentStyle = new MessageTemplate<>(
                existedMinecraftChatStyle.getOrElse("minecraftTeamMessageSentStyle", MINECRAFT_TEAM_MESSAGE_SENT_STYLE_DEFAULT),
                new MessageTypes.TeamMessage()
        );
        minecraftChatStyle.set("minecraftTeamMessageSentStyle", minecraftTeamMessageSentStyle.getRawTemplate());
        minecraftChatStyle.setComment("minecraftTeamMessageSentStyle", MINECRAFT_TEAM_MESSAGE_SENT_STYLE_COMMENT);

        minecraftTeamMessageReceivedStyle = new MessageTemplate<>(
                existedMinecraftChatStyle.getOrElse("minecraftTeamMessageReceivedStyle", MINECRAFT_TEAM_MESSAGE_RECEIVED_STYLE_DEFAULT),
                new MessageTypes.TeamMessage()
        );
        minecraftChatStyle.set("minecraftTeamMessageReceivedStyle", minecraftTeamMessageReceivedStyle.getRawTemplate());
        minecraftChatStyle.setComment("minecraftTeamMessageReceivedStyle", MINECRAFT_TEAM_MESSAGE_RECEIVED_STYLE_COMMENT);

        minecraftTellMessageSentStyle = new MessageTemplate<>(
                existedMinecraftChatStyle.getOrElse("minecraftTellMessageSentStyle", MINECRAFT_TELL_MESSAGE_SENT_STYLE_DEFAULT),
                new MessageTypes.TellOutgoingMessage()
        );
        minecraftChatStyle.set("minecraftTellMessageSentStyle", minecraftTellMessageSentStyle.getRawTemplate());
        minecraftChatStyle.setComment("minecraftTellMessageSentStyle", MINECRAFT_TELL_MESSAGE_SENT_STYLE_COMMENT);

        minecraftTellMessageReceivedStyle = new MessageTemplate<>(
                existedMinecraftChatStyle.getOrElse("minecraftTellMessageReceivedStyle", MINECRAFT_TELL_MESSAGE_RECEIVED_STYLE_DEFAULT),
                new MessageTypes.TellIncomingMessage()
        );
        minecraftChatStyle.set("minecraftTellMessageReceivedStyle", minecraftTellMessageReceivedStyle.getRawTemplate());
        minecraftChatStyle.setComment("minecraftTellMessageReceivedStyle", MINECRAFT_TELL_MESSAGE_RECEIVED_STYLE_COMMENT);

        minecraftSayCommandStyle = new MessageTemplate<>(
                existedMinecraftChatStyle.getOrElse("minecraftSayCommandStyle", MINECRAFT_SAY_COMMAND_STYLE_DEFAULT),
                new MessageTypes.PlayerMessage()
        );
        minecraftChatStyle.set("minecraftSayCommandStyle", minecraftSayCommandStyle.getRawTemplate());
        minecraftChatStyle.setComment("minecraftSayCommandStyle", MINECRAFT_SAY_COMMAND_STYLE_COMMENT);

        minecraftMeCommandStyle = new MessageTemplate<>(
                existedMinecraftChatStyle.getOrElse("minecraftMeCommandStyle", MINECRAFT_ME_COMMAND_STYLE_DEFAULT),
                new MessageTypes.PlayerMessage()
        );
        minecraftChatStyle.set("minecraftMeCommandStyle", minecraftMeCommandStyle.getRawTemplate());
        minecraftChatStyle.setComment("minecraftMeCommandStyle", MINECRAFT_ME_COMMAND_STYLE_COMMENT);

        updateTellMessageExistedDefaultStyle(minecraftChatStyle);

        return minecraftChatStyle;
    }

    /**
     * Migration method for configs generated with version 2.8.0 or less
     * @since 2.9.0
     */
    private static void updateTellMessageExistedDefaultStyle(CommentedConfig minecraftChatStyle) {
        String oldSentStyle = "<grey>*{commands.message.display.outgoing} {receiver}: {message}*<grey/>";
        String oldReceivedStyle = "<grey>*{sender} {commands.message.display.incoming}: {message}*<grey/>";

        if (minecraftTellMessageSentStyle.getRawTemplate().equals(oldSentStyle)) {
            minecraftTellMessageSentStyle = new MessageTemplate<>(MINECRAFT_TELL_MESSAGE_SENT_STYLE_DEFAULT, new MessageTypes.TellOutgoingMessage());
            minecraftChatStyle.set("minecraftTellMessageSentStyle", minecraftTellMessageSentStyle.getRawTemplate());
            LOGGER.info("[minecraftChatStyle] Updating the default style for messages sent via the /tell command.");
        }

        if (minecraftTellMessageReceivedStyle.getRawTemplate().equals(oldReceivedStyle)) {
            minecraftTellMessageReceivedStyle = new MessageTemplate<>(MINECRAFT_TELL_MESSAGE_RECEIVED_STYLE_DEFAULT, new MessageTypes.TellIncomingMessage());
            minecraftChatStyle.set("minecraftTellMessageReceivedStyle", minecraftTellMessageReceivedStyle.getRawTemplate());
            LOGGER.info("[minecraftChatStyle] Updating the default style for messages received via the /tell command.");
        }
    }
}

package com.denisnumb.discord_chat_mod.config.configs;

import com.denisnumb.discord_chat_mod.chat.template.MessageTemplate;
import com.denisnumb.discord_chat_mod.chat.template.MessageTypes;
import com.denisnumb.discord_chat_mod.config.core.ConfigParameter;
import com.denisnumb.discord_chat_mod.config.core.ConfigSection;
import com.denisnumb.discord_chat_mod.utils.ColorUtils;
import com.electronwill.nightconfig.core.CommentedConfig;

import java.util.Objects;

import static com.denisnumb.discord_chat_mod.config.ConfigComments.*;
import static com.denisnumb.discord_chat_mod.config.ConfigDefaults.*;
import static com.denisnumb.discord_chat_mod.utils.ColorUtils.parseGradientColors;

public final class MinecraftChatStyleConfig extends ConfigSection {
    private MinecraftChatStyleConfig() {}

    public static final ConfigParameter<Boolean, Boolean> ENABLE_MINECRAFT_CHAT_CUSTOMIZATION =
            ConfigParameter.ofBoolean("enableMinecraftChatCustomization")
                    .defaultValue(ENABLE_MINECRAFT_CHAT_CUSTOMIZATION_DEFAULT)
                    .comment(ENABLE_MINECRAFT_CHAT_CUSTOMIZATION_COMMENT)
                    .build();

    public static final ConfigParameter<String, int[]> CHAT_LINK_COLORS =
            ConfigParameter.ofString("minecraftChatLinkColor")
                    .defaultValue(MINECRAFT_CHAT_LINK_COLOR_DEFAULT)
                    .comment(MINECRAFT_CHAT_LINK_COLOR_COMMENT)
                    .transform(colors -> Objects.requireNonNullElse(parseGradientColors(colors), new int[] { ColorUtils.Color.DEFAULT_CHAT_LINK_COLOR }))
                    .build();

    public static final ConfigParameter<String, MessageTemplate<MessageTypes.DiscordMessage>> DISCORD_MESSAGE_TEMPLATE =
            ConfigParameter.ofString("minecraftDiscordMessagesStyle")
                    .defaultValue(MINECRAFT_DISCORD_MESSAGES_STYLE_DEFAULT)
                    .comment(MINECRAFT_DISCORD_MESSAGES_STYLE_COMMENT)
                    .transform(template -> new MessageTemplate<>(template, new MessageTypes.DiscordMessage()))
                    .build();

    public static final ConfigParameter<String, MessageTemplate<MessageTypes.PlayerMessage>> PLAYER_MESSAGE_TEMPLATE =
            ConfigParameter.ofString("minecraftPlayerMessageStyle")
                    .defaultValue(MINECRAFT_PLAYER_MESSAGE_STYLE_DEFAULT)
                    .comment(MINECRAFT_PLAYER_MESSAGE_STYLE_COMMENT)
                    .transform(template -> new MessageTemplate<>(template, new MessageTypes.PlayerMessage()))
                    .build();

    public static final ConfigParameter<String, MessageTemplate<MessageTypes.PlayerJoined>> PLAYER_JOINED_TEMPLATE =
            ConfigParameter.ofString("minecraftPlayerJoinedStyle")
                    .defaultValue(MINECRAFT_PLAYER_JOINED_STYLE_DEFAULT)
                    .comment(MINECRAFT_PLAYER_JOINED_STYLE_COMMENT)
                    .transform(template -> new MessageTemplate<>(template, new MessageTypes.PlayerJoined()))
                    .build();

    public static final ConfigParameter<String, MessageTemplate<MessageTypes.PlayerLeft>> PLAYER_LEFT_TEMPLATE =
            ConfigParameter.ofString("minecraftPlayerLeftStyle")
                    .defaultValue(MINECRAFT_PLAYER_LEFT_STYLE_DEFAULT)
                    .comment(MINECRAFT_PLAYER_LEFT_STYLE_COMMENT)
                    .transform(template -> new MessageTemplate<>(template, new MessageTypes.PlayerLeft()))
                    .build();

    public static final ConfigParameter<String, MessageTemplate<MessageTypes.DeathCause>> DEATH_CAUSE_TEMPLATE =
            ConfigParameter.ofString("minecraftPlayerDeathCauseStyle")
                    .defaultValue(MINECRAFT_PLAYER_DEATH_CAUSE_STYLE_DEFAULT)
                    .comment(MINECRAFT_PLAYER_DEATH_STYLE_COMMENT)
                    .transform(template -> new MessageTemplate<>(template, new MessageTypes.DeathCause()))
                    .build();

    public static final ConfigParameter<String, MessageTemplate<MessageTypes.DiedEntity>> PLAYER_DEATH_NAME_TEMPLATE =
            ConfigParameter.ofString("minecraftPlayerDeathNameStyle")
                    .defaultValue(MINECRAFT_PLAYER_DEATH_NAME_STYLE_DEFAULT)
                    .transform(template -> new MessageTemplate<>(template, new MessageTypes.DiedEntity()))
                    .build();

    public static final ConfigParameter<String, MessageTemplate<MessageTypes.KillerEntity>> KILLER_ENTITY_TEMPLATE =
            ConfigParameter.ofString("minecraftPlayerDeathSecondEntityNameStyle")
                    .defaultValue(MINECRAFT_PLAYER_DEATH_SECOND_ENTITY_STYLE_DEFAULT)
                    .transform(template -> new MessageTemplate<>(template, new MessageTypes.KillerEntity()))
                    .build();

    public static final ConfigParameter<String, MessageTemplate<MessageTypes.KillerWeapon>> KILLER_WEAPON_TEMPLATE =
            ConfigParameter.ofString("minecraftPlayerDeathWeaponStyle")
                    .defaultValue(MINECRAFT_PLAYER_DEATH_WEAPON_STYLE_DEFAULT)
                    .transform(template -> new MessageTemplate<>(template, new MessageTypes.KillerWeapon()))
                    .build();

    public static final ConfigParameter<String, MessageTemplate<MessageTypes.AdvancementMessageType>> ADVANCEMENT_TASK_TEMPLATE =
            ConfigParameter.ofString("minecraftPlayerAdvancementTaskStyle")
                    .defaultValue(MINECRAFT_PLAYER_ADVANCEMENT_TASK_STYLE_DEFAULT)
                    .comment(MINECRAFT_PLAYER_ADVANCEMENT_TASK_STYLE_COMMENT)
                    .transform(template -> new MessageTemplate<MessageTypes.AdvancementMessageType>(template, new MessageTypes.AdvancementTask()))
                    .build();

    public static final ConfigParameter<String, MessageTemplate<MessageTypes.AdvancementMessageType>> ADVANCEMENT_GOAL_TEMPLATE =
            ConfigParameter.ofString("minecraftPlayerAdvancementGoalStyle")
                    .defaultValue(MINECRAFT_PLAYER_ADVANCEMENT_GOAL_STYLE_DEFAULT)
                    .comment(MINECRAFT_PLAYER_ADVANCEMENT_GOAL_STYLE_COMMENT)
                    .transform(template -> new MessageTemplate<MessageTypes.AdvancementMessageType>(template, new MessageTypes.AdvancementGoal()))
                    .build();

    public static final ConfigParameter<String, MessageTemplate<MessageTypes.AdvancementMessageType>> ADVANCEMENT_CHALLENGE_TEMPLATE =
            ConfigParameter.ofString("minecraftPlayerAdvancementChallengeStyle")
                    .defaultValue(MINECRAFT_PLAYER_ADVANCEMENT_CHALLENGE_STYLE_DEFAULT)
                    .comment(MINECRAFT_PLAYER_ADVANCEMENT_CHALLENGE_STYLE_COMMENT)
                    .transform(template -> new MessageTemplate<MessageTypes.AdvancementMessageType>(template, new MessageTypes.AdvancementChallenge()))
                    .build();

    public static final ConfigParameter<String, MessageTemplate<MessageTypes.TeamMessage>> TEAM_MESSAGE_OUTGOING_TEMPLATE =
            ConfigParameter.ofString("minecraftTeamMessageSentStyle")
                    .defaultValue(MINECRAFT_TEAM_MESSAGE_SENT_STYLE_DEFAULT)
                    .comment(MINECRAFT_TEAM_MESSAGE_SENT_STYLE_COMMENT)
                    .transform(template -> new MessageTemplate<>(template, new MessageTypes.TeamMessage()))
                    .build();

    public static final ConfigParameter<String, MessageTemplate<MessageTypes.TeamMessage>> TEAM_MESSAGE_INCOMING_TEMPLATE =
            ConfigParameter.ofString("minecraftTeamMessageReceivedStyle")
                    .defaultValue(MINECRAFT_TEAM_MESSAGE_RECEIVED_STYLE_DEFAULT)
                    .comment(MINECRAFT_TEAM_MESSAGE_RECEIVED_STYLE_COMMENT)
                    .transform(template -> new MessageTemplate<>(template, new MessageTypes.TeamMessage()))
                    .build();

    public static final ConfigParameter<String, MessageTemplate<MessageTypes.TellOutgoingMessage>> TELL_MESSAGE_OUTGOING_TEMPLATE =
            ConfigParameter.ofString("minecraftTellMessageSentStyle")
                    .defaultValue(MINECRAFT_TELL_MESSAGE_SENT_STYLE_DEFAULT)
                    .comment(MINECRAFT_TELL_MESSAGE_SENT_STYLE_COMMENT)
                    .migrateDefaultValue("<grey>*{commands.message.display.outgoing} {receiver}: {message}*<grey/>")
                    .transform(template -> new MessageTemplate<>(template, new MessageTypes.TellOutgoingMessage()))
                    .build();

    public static final ConfigParameter<String, MessageTemplate<MessageTypes.TellIncomingMessage>> TELL_MESSAGE_INCOMING_TEMPLATE =
            ConfigParameter.ofString("minecraftTellMessageReceivedStyle")
                    .defaultValue(MINECRAFT_TELL_MESSAGE_RECEIVED_STYLE_DEFAULT)
                    .comment(MINECRAFT_TELL_MESSAGE_RECEIVED_STYLE_COMMENT)
                    .migrateDefaultValue("<grey>*{sender} {commands.message.display.incoming}: {message}*<grey/>")
                    .transform(template -> new MessageTemplate<>(template, new MessageTypes.TellIncomingMessage()))
                    .build();

    public static final ConfigParameter<String, MessageTemplate<MessageTypes.PlayerMessage>> SAY_COMMAND_TEMPLATE =
            ConfigParameter.ofString("minecraftSayCommandStyle")
                    .defaultValue(MINECRAFT_SAY_COMMAND_STYLE_DEFAULT)
                    .comment(MINECRAFT_SAY_COMMAND_STYLE_COMMENT)
                    .transform(template -> new MessageTemplate<>(template, new MessageTypes.PlayerMessage()))
                    .build();

    public static final ConfigParameter<String, MessageTemplate<MessageTypes.PlayerMessage>> ME_COMMAND_TEMPLATE =
            ConfigParameter.ofString("minecraftMeCommandStyle")
                    .defaultValue(MINECRAFT_ME_COMMAND_STYLE_DEFAULT)
                    .comment(MINECRAFT_ME_COMMAND_STYLE_COMMENT)
                    .transform(template -> new MessageTemplate<>(template, new MessageTypes.PlayerMessage()))
                    .build();

    public static CommentedConfig load(CommentedConfig commonConfig) {
        CommentedConfig minecraftChatStyleConfig = commonConfig.getOrElse("minecraftChatStyle", commonConfig.createSubConfig());
        ConfigSection.loadAll(MinecraftChatStyleConfig.class, minecraftChatStyleConfig, minecraftChatStyleConfig);

        return minecraftChatStyleConfig;
    }
}
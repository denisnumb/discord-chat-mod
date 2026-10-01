package com.denisnumb.discord_chat_mod.config.configs;

import com.denisnumb.discord_chat_mod.chat.template.TemplateParameter;
import com.denisnumb.discord_chat_mod.config.core.ConfigParameter;
import com.denisnumb.discord_chat_mod.config.core.ConfigParameterBuilder;
import com.denisnumb.discord_chat_mod.config.core.ConfigSection;
import com.denisnumb.discord_chat_mod.discord.chat.template.JsonTemplate;
import com.denisnumb.discord_chat_mod.discord.chat.template.StringTemplate;
import com.denisnumb.discord_chat_mod.discord.chat.template.TemplateType;
import com.denisnumb.discord_chat_mod.discord.chat.template.TemplateTypes;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import static com.denisnumb.discord_chat_mod.config.ConfigComments.*;
import static com.denisnumb.discord_chat_mod.config.ConfigDefaults.*;

public final class DiscordChatStyleConfig extends ConfigSection {
    private DiscordChatStyleConfig() {}

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();

    public static final ConfigParameter<String, JsonTemplate<TemplateTypes.PlayerMessage>> PLAYER_MESSAGE_TEMPLATE =
            jsonTemplate("discordPlayerMessageStyle",
                    DISCORD_PLAYER_MESSAGE_STYLE_DEFAULT,
                    DISCORD_PLAYER_MESSAGE_STYLE_COMMENT,
                    new TemplateTypes.PlayerMessage()
            ).build();

    public static final ConfigParameter<String, JsonTemplate<TemplateTypes.PlayerMessage>> PLAYER_MESSAGE_WEBHOOK_TEMPLATE =
            jsonTemplate("discordPlayerMessageWebhookStyle",
                    DISCORD_PLAYER_MESSAGE_WEBHOOK_STYLE_DEFAULT,
                    DISCORD_PLAYER_MESSAGE_WEBHOOK_STYLE_COMMENT,
                    new TemplateTypes.PlayerMessage()
            ).build();

    public static final ConfigParameter<String, JsonTemplate<TemplateTypes.PlayerJoined>> PLAYER_JOINED_TEMPLATE =
            jsonTemplate("discordPlayerJoinedStyle",
                    DISCORD_PLAYER_JOINED_STYLE_DEFAULT,
                    DISCORD_PLAYER_JOINED_STYLE_COMMENT,
                    new TemplateTypes.PlayerJoined()
            ).build();

    public static final ConfigParameter<String, JsonTemplate<TemplateTypes.PlayerLeft>> PLAYER_LEFT_TEMPLATE =
            jsonTemplate("discordPlayerLeftStyle",
                    DISCORD_PLAYER_LEFT_STYLE_DEFAULT,
                    DISCORD_PLAYER_LEFT_STYLE_COMMENT,
                    new TemplateTypes.PlayerLeft()
            ).build();

    public static final ConfigParameter<String, StringTemplate<TemplateTypes.DeathCause>> PLAYER_DEATH_CAUSE_TEMPLATE =
            ConfigParameter.ofString("discordPlayerDeathCauseStyle")
                    .defaultValue(DISCORD_PLAYER_DEATH_CAUSE_STYLE_DEFAULT)
                    .comment(DISCORD_PLAYER_DEATH_CAUSE_STYLE_COMMENT)
                    .transform(t -> new StringTemplate<TemplateTypes.DeathCause>(t))
                    .build();

    public static final ConfigParameter<String, StringTemplate<TemplateTypes.DiedEntity>> PLAYER_DEATH_NAME_TEMPLATE =
            ConfigParameter.ofString("discordPlayerDeathNameStyle")
                    .defaultValue(DISCORD_PLAYER_DEATH_NAME_STYLE_DEFAULT)
                    .transform(t -> new StringTemplate<TemplateTypes.DiedEntity>(t))
                    .build();

    public static final ConfigParameter<String, StringTemplate<TemplateTypes.KillerEntity>> KILLER_ENTITY_TEMPLATE =
            ConfigParameter.ofString("discordPlayerDeathSecondEntityStyle")
                    .defaultValue(DISCORD_PLAYER_DEATH_SECOND_ENTITY_STYLE_DEFAULT)
                    .transform(t -> new StringTemplate<TemplateTypes.KillerEntity>(t))
                    .build();

    public static final ConfigParameter<String, StringTemplate<TemplateTypes.KillerWeapon>> KILLER_WEAPON_TEMPLATE =
            ConfigParameter.ofString("discordPlayerDeathWeaponStyle")
                    .defaultValue(DISCORD_PLAYER_DEATH_WEAPON_STYLE_DEFAULT)
                    .transform(t -> new StringTemplate<TemplateTypes.KillerWeapon>(t))
                    .build();

    public static final ConfigParameter<String, JsonTemplate<TemplateTypes.DeathMessage>> DEATH_MESSAGE_TEMPLATE =
            jsonTemplate("discordPlayerDeathMessageStyle",
                    DISCORD_PLAYER_DEATH_MESSAGE_STYLE_DEFAULT,
                    DISCORD_PLAYER_DEATH_MESSAGE_STYLE_COMMENT,
                    new TemplateTypes.DeathMessage()
            ).build();

    public static final ConfigParameter<String, JsonTemplate<TemplateTypes.AdvancementTask>> ADVANCEMENT_TASK_TEMPLATE =
            jsonTemplate("discordPlayerAdvancementTaskStyle",
                    DISCORD_PLAYER_ADVANCEMENT_TASK_STYLE_DEFAULT,
                    DISCORD_PLAYER_ADVANCEMENT_TASK_STYLE_COMMENT,
                    new TemplateTypes.AdvancementTask()
            ).build();

    public static final ConfigParameter<String, JsonTemplate<TemplateTypes.AdvancementGoal>> ADVANCEMENT_GOAL_TEMPLATE =
            jsonTemplate("discordPlayerAdvancementGoalStyle",
                    DISCORD_PLAYER_ADVANCEMENT_GOAL_STYLE_DEFAULT,
                    DISCORD_PLAYER_ADVANCEMENT_GOAL_STYLE_COMMENT,
                    new TemplateTypes.AdvancementGoal()
            ).build();

    public static final ConfigParameter<String, JsonTemplate<TemplateTypes.AdvancementChallenge>> ADVANCEMENT_CHALLENGE_TEMPLATE =
            jsonTemplate("discordPlayerAdvancementChallengeStyle",
                    DISCORD_PLAYER_ADVANCEMENT_CHALLENGE_STYLE_DEFAULT,
                    DISCORD_PLAYER_ADVANCEMENT_CHALLENGE_STYLE_COMMENT,
                    new TemplateTypes.AdvancementChallenge()
            ).build();

    public static final ConfigParameter<String, JsonTemplate<TemplateTypes.PlayerMessage>> SAY_COMMAND_TEMPLATE =
            jsonTemplate("discordSayCommandStyle",
                    DISCORD_SAY_COMMAND_STYLE_DEFAULT,
                    DISCORD_SAY_COMMAND_STYLE_COMMENT,
                    new TemplateTypes.PlayerMessage()
            ).build();

    public static final ConfigParameter<String, JsonTemplate<TemplateTypes.PlayerMessage>> ME_COMMAND_TEMPLATE =
            jsonTemplate("discordMeCommandStyle",
                    DISCORD_ME_COMMAND_STYLE_DEFAULT,
                    DISCORD_ME_COMMAND_STYLE_COMMENT,
                    new TemplateTypes.PlayerMessage()
            ).build();

    public static final ConfigParameter<String, JsonTemplate<TemplateTypes.PlayerMessage>> ME_COMMAND_WEBHOOK_TEMPLATE =
            jsonTemplate("discordMeCommandWebhookStyle",
                    DISCORD_ME_COMMAND_WEBHOOK_STYLE_DEFAULT,
                    DISCORD_ME_COMMAND_WEBHOOK_STYLE_COMMENT,
                    new TemplateTypes.PlayerMessage()
            ).build();

    public static final ConfigParameter<String, JsonTemplate<TemplateTypes.TellrawMessage>> TELLRAW_COMMAND_TEMPLATE =
            jsonTemplate("discordTellrawCommandStyle",
                    DISCORD_TELLRAW_COMMAND_STYLE_DEFAULT,
                    DISCORD_TELLRAW_COMMAND_STYLE_COMMENT,
                    new TemplateTypes.TellrawMessage()
            ).build();

    public static final ConfigParameter<String, JsonTemplate<TemplateTypes.CommandLog>> COMMAND_LOG_TEMPLATE =
            jsonTemplate("discordCommandLogStyle",
                    DISCORD_COMMAND_LOG_STYLE_DEFAULT,
                    DISCORD_COMMAND_LOG_STYLE_COMMENT,
                    new TemplateTypes.CommandLog()
            ).build();

    public static final ConfigParameter<String, JsonTemplate<TemplateTypes.ImageMessage>> IMAGE_MESSAGE_TEMPLATE =
            jsonTemplate("discordImageMessageStyle",
                    DISCORD_IMAGE_MESSAGE_STYLE_DEFAULT,
                    DISCORD_IMAGE_MESSAGE_STYLE_COMMENT,
                    new TemplateTypes.ImageMessage()
            ).migrateFrom("discordScreenshotMessageStyle",
                            value -> value.replace("{screenshot_url}", TemplateParameter.IMAGE_URL.getPlaceholder())
            ).build();

    public static final ConfigParameter<String, JsonTemplate<TemplateTypes.ImageMessage>> IMAGE_MESSAGE_WEBHOOK_TEMPLATE =
            jsonTemplate("discordImageMessageWebhookStyle",
                    DISCORD_IMAGE_MESSAGE_WEBHOOK_STYLE_DEFAULT,
                    DISCORD_IMAGE_MESSAGE_WEBHOOK_STYLE_COMMENT,
                    new TemplateTypes.ImageMessage()
            ).migrateFrom("discordScreenshotMessageWebhookStyle",
                            value -> value.replace("{screenshot_url}", TemplateParameter.IMAGE_URL.getPlaceholder())
            ).build();

    public static final ConfigParameter<String, JsonTemplate<TemplateTypes.ServerStarted>> SERVER_STARTED_MESSAGE_TEMPLATE =
            jsonTemplate("discordServerStartedMessageStyle",
                    DISCORD_SERVER_STARTED_MESSAGE_STYLE_DEFAULT,
                    DISCORD_SERVER_STARTED_MESSAGE_STYLE_COMMENT,
                    new TemplateTypes.ServerStarted()
            ).build();

    public static final ConfigParameter<String, JsonTemplate<TemplateTypes.LocalServerStated>> LOCAL_SERVER_STARTED_MESSAGE_TEMPLATE =
            jsonTemplate("discordLocalServerStartedMessageStyle",
                    DISCORD_LOCAL_SERVER_STARTED_MESSAGE_STYLE_DEFAULT,
                    DISCORD_LOCAL_SERVER_STARTED_MESSAGE_STYLE_COMMENT,
                    new TemplateTypes.LocalServerStated()
            ).build();

    public static final ConfigParameter<String, JsonTemplate<TemplateTypes.ServerClosed>> SERVER_CLOSED_MESSAGE_TEMPLATE =
            jsonTemplate("discordServerClosedMessageStyle",
                    DISCORD_SERVER_CLOSED_MESSAGE_STYLE_DEFAULT,
                    DISCORD_SERVER_CLOSED_MESSAGE_STYLE_COMMENT,
                    new TemplateTypes.ServerClosed()
            ).build();

    public static final ConfigParameter<String, JsonTemplate<TemplateTypes.PinnedStatusUnavailable>> PINNED_STATUS_MESSAGE_SERVER_UNAVAILABLE_TEMPLATE =
            jsonTemplate("discordPinnedStatusMessageServerUnavailableStyle",
                    DISCORD_PINNED_STATUS_MESSAGE_SERVER_UNAVAILABLE_STYLE_DEFAULT,
                    DISCORD_PINNED_STATUS_MESSAGE_SERVER_UNAVAILABLE_STYLE_COMMENT,
                    new TemplateTypes.PinnedStatusUnavailable()
            ).build();

    public static final ConfigParameter<String, JsonTemplate<TemplateTypes.PinnedStatusAvailable>> PINNED_STATUS_MESSAGE_SERVER_AVAILABLE_TEMPLATE =
            jsonTemplate("discordPinnedStatusMessageServerAvailableStyle",
                    DISCORD_PINNED_STATUS_MESSAGE_SERVER_AVAILABLE_STYLE_DEFAULT,
                    DISCORD_PINNED_STATUS_MESSAGE_SERVER_AVAILABLE_STYLE_COMMENT,
                    new TemplateTypes.PinnedStatusAvailable()
            ).build();

    public static final ConfigParameter<String, String> PINNED_STATUS_MESSAGE_PLAYER_LIST_DELIMITER =
            ConfigParameter.ofString("discordPinnedStatusMessagePlayerListDelimiter")
                    .defaultValue(DISCORD_PINNED_STATUS_MESSAGE_PLAYER_LIST_DELIMITER_DEFAULT)
                    .comment(DISCORD_PINNED_STATUS_MESSAGE_PLAYER_LIST_DELIMITER_COMMENT)
                    .build();

    public static final ConfigParameter<String, StringTemplate<TemplateTypes.PlayerListNickname>> PINNED_STATUS_MESSAGE_PLAYER_LIST_NICKNAME_TEMPLATE =
            ConfigParameter.ofString("discordPinnedStatusMessagePlayerListNicknameStyle")
                    .defaultValue(DISCORD_PINNED_STATUS_MESSAGE_PLAYER_LIST_NICKNAME_STYLE_DEFAULT)
                    .comment(DISCORD_PINNED_STATUS_MESSAGE_PLAYER_LIST_NICKNAME_STYLE_COMMENT)
                    .transform(t -> new StringTemplate<TemplateTypes.PlayerListNickname>(t))
                    .build();

    public static final ConfigParameter<String, JsonTemplate<TemplateTypes.PinnedStatusOnlinePlayers>> PINNED_STATUS_MESSAGE_TEMPLATE =
            jsonTemplate("discordPinnedStatusMessageStyle",
                    DISCORD_PINNED_STATUS_MESSAGE_STYLE_DEFAULT,
                    DISCORD_PINNED_STATUS_MESSAGE_STYLE_COMMENT,
                    new TemplateTypes.PinnedStatusOnlinePlayers()
            ).build();

    public static final ConfigParameter<String, StringTemplate<TemplateTypes.GuildForwardedMessageWebhookUsername>> GUILD_FORWARDED_MESSAGE_WEBHOOK_USERNAME_TEMPLATE =
            ConfigParameter.ofString("discordGuildForwardedMessageWebhookUsernameStyle")
                    .defaultValue(DISCORD_GUILD_FORWARDED_MESSAGE_WEBHOOK_USERNAME_STYLE_DEFAULT)
                    .comment(DISCORD_GUILD_FORWARDED_MESSAGE_WEBHOOK_USERNAME_STYLE_COMMENT)
                    .migrateFrom("discordGuildForwardedMessageUserNameStyle")
                    .transform(t -> new StringTemplate<TemplateTypes.GuildForwardedMessageWebhookUsername>(t))
                    .build();

    public static final ConfigParameter<String, JsonTemplate<TemplateTypes.GuildForwardedMessage>> GUILD_FORWARDED_MESSAGE_TEMPLATE =
            jsonTemplate("discordGuildForwardedMessageStyle",
                    DISCORD_GUILD_FORWARDED_MESSAGE_STYLE_DEFAULT,
                    DISCORD_GUILD_FORWARDED_MESSAGE_STYLE_COMMENT,
                    new TemplateTypes.GuildForwardedMessage()
            ).build();

    private static <T extends TemplateType> ConfigParameterBuilder<String, JsonTemplate<T>> jsonTemplate(
            String key,
            String defaultValue,
            String comment,
            T templateType
    ) {
        return ConfigParameter.ofString(key)
                .defaultValue(defaultValue)
                .comment(comment)
                .normalize(value -> value.replace("\r", ""))
                .validator(DiscordChatStyleConfig::validateJsonValue)
                .transform(t -> new JsonTemplate<>(t, templateType));
    }

    private static String validateJsonValue(String jsonValue, String defaultValue) {
        if (jsonValue.isBlank())
            return defaultValue;

        try {
            JsonObject jsonObject = GSON.fromJson(jsonValue, JsonObject.class);

            if (!jsonObject.has("content") && !jsonObject.has("embed"))
                throw new JsonSyntaxException("Json should contains \"content\" or \"embed\" keys");

            return jsonValue;
        } catch (JsonSyntaxException e) {
            LOGGER.warn("Error on parsing discord message style json: {}", e.getMessage());
            LOGGER.warn(jsonValue);
            LOGGER.warn("Default style will be used");
            return defaultValue;
        }
    }

    public static CommentedConfig load(CommentedConfig commonConfig) {
        CommentedConfig discordChatStyleConfig = commonConfig.getOrElse("discordChatStyle", commonConfig.createSubConfig());
        ConfigSection.loadAll(DiscordChatStyleConfig.class, discordChatStyleConfig, discordChatStyleConfig);

        return discordChatStyleConfig;
    }
}
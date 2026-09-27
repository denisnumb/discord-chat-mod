package com.denisnumb.discord_chat_mod.config.configs;

import com.denisnumb.discord_chat_mod.chat.template.TemplateParameter;
import com.denisnumb.discord_chat_mod.config.core.ConfigParameter;
import com.denisnumb.discord_chat_mod.config.core.ConfigParameterBuilder;
import com.denisnumb.discord_chat_mod.config.core.ConfigSection;
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

    public static final ConfigParameter<String, String> PLAYER_MESSAGE_TEMPLATE =
            jsonMessage("discordPlayerMessageStyle", DISCORD_PLAYER_MESSAGE_STYLE_DEFAULT, DISCORD_PLAYER_MESSAGE_STYLE_COMMENT)
                    .build();

    public static final ConfigParameter<String, String> PLAYER_MESSAGE_WEBHOOK_TEMPLATE =
            jsonMessage("discordPlayerMessageWebhookStyle", DISCORD_PLAYER_MESSAGE_WEBHOOK_STYLE_DEFAULT, DISCORD_PLAYER_MESSAGE_WEBHOOK_STYLE_COMMENT)
                    .build();

    public static final ConfigParameter<String, String> PLAYER_JOINED_TEMPLATE =
            jsonMessage("discordPlayerJoinedStyle", DISCORD_PLAYER_JOINED_STYLE_DEFAULT, DISCORD_PLAYER_JOINED_STYLE_COMMENT)
                    .build();

    public static final ConfigParameter<String, String> PLAYER_LEFT_TEMPLATE =
            jsonMessage("discordPlayerLeftStyle", DISCORD_PLAYER_LEFT_STYLE_DEFAULT, DISCORD_PLAYER_LEFT_STYLE_COMMENT)
                    .build();

    public static final ConfigParameter<String, String> PLAYER_DEATH_CAUSE_TEMPLATE =
            ConfigParameter.ofString("discordPlayerDeathCauseStyle")
                    .defaultValue(DISCORD_PLAYER_DEATH_CAUSE_STYLE_DEFAULT)
                    .comment(DISCORD_PLAYER_DEATH_CAUSE_STYLE_COMMENT)
                    .build();

    public static final ConfigParameter<String, String> PLAYER_DEATH_NAME_TEMPLATE =
            ConfigParameter.ofString("discordPlayerDeathNameStyle")
                    .defaultValue(DISCORD_PLAYER_DEATH_NAME_STYLE_DEFAULT)
                    .build();

    public static final ConfigParameter<String, String> KILLER_ENTITY_TEMPLATE =
            ConfigParameter.ofString("discordPlayerDeathSecondEntityStyle")
                    .defaultValue(DISCORD_PLAYER_DEATH_SECOND_ENTITY_STYLE_DEFAULT)
                    .build();

    public static final ConfigParameter<String, String> KILLER_WEAPON_TEMPLATE =
            ConfigParameter.ofString("discordPlayerDeathWeaponStyle")
                    .defaultValue(DISCORD_PLAYER_DEATH_WEAPON_STYLE_DEFAULT)
                    .build();

    public static final ConfigParameter<String, String> DEATH_MESSAGE_TEMPLATE =
            jsonMessage("discordPlayerDeathMessageStyle", DISCORD_PLAYER_DEATH_MESSAGE_STYLE_DEFAULT, DISCORD_PLAYER_DEATH_MESSAGE_STYLE_COMMENT)
                    .build();

    public static final ConfigParameter<String, String> ADVANCEMENT_TASK_TEMPLATE =
            jsonMessage("discordPlayerAdvancementTaskStyle", DISCORD_PLAYER_ADVANCEMENT_TASK_STYLE_DEFAULT, DISCORD_PLAYER_ADVANCEMENT_TASK_STYLE_COMMENT)
                    .build();

    public static final ConfigParameter<String, String> ADVANCEMENT_GOAL_TEMPLATE =
            jsonMessage("discordPlayerAdvancementGoalStyle", DISCORD_PLAYER_ADVANCEMENT_GOAL_STYLE_DEFAULT, DISCORD_PLAYER_ADVANCEMENT_GOAL_STYLE_COMMENT)
                    .build();

    public static final ConfigParameter<String, String> ADVANCEMENT_CHALLENGE_TEMPLATE =
            jsonMessage("discordPlayerAdvancementChallengeStyle", DISCORD_PLAYER_ADVANCEMENT_CHALLENGE_STYLE_DEFAULT, DISCORD_PLAYER_ADVANCEMENT_CHALLENGE_STYLE_COMMENT)
                    .build();

    public static final ConfigParameter<String, String> SAY_COMMAND_TEMPLATE =
            jsonMessage("discordSayCommandStyle", DISCORD_SAY_COMMAND_STYLE_DEFAULT, DISCORD_SAY_COMMAND_STYLE_COMMENT)
                    .build();

    public static final ConfigParameter<String, String> ME_COMMAND_TEMPLATE =
            jsonMessage("discordMeCommandStyle", DISCORD_ME_COMMAND_STYLE_DEFAULT, DISCORD_ME_COMMAND_STYLE_COMMENT)
                    .build();

    public static final ConfigParameter<String, String> ME_COMMAND_WEBHOOK_TEMPLATE =
            jsonMessage("discordMeCommandWebhookStyle", DISCORD_ME_COMMAND_WEBHOOK_STYLE_DEFAULT, DISCORD_ME_COMMAND_WEBHOOK_STYLE_COMMENT)
                    .build();

    public static final ConfigParameter<String, String> TELLRAW_COMMAND_TEMPLATE =
            jsonMessage("discordTellrawCommandStyle", DISCORD_TELLRAW_COMMAND_STYLE_DEFAULT, DISCORD_TELLRAW_COMMAND_STYLE_COMMENT)
                    .build();

    public static final ConfigParameter<String, String> COMMAND_LOG_TEMPLATE =
            jsonMessage("discordCommandLogStyle", DISCORD_COMMAND_LOG_STYLE_DEFAULT, DISCORD_COMMAND_LOG_STYLE_COMMENT)
                    .build();

    public static final ConfigParameter<String, String> IMAGE_MESSAGE_TEMPLATE =
            jsonMessage("discordImageMessageStyle", DISCORD_IMAGE_MESSAGE_STYLE_DEFAULT, DISCORD_IMAGE_MESSAGE_STYLE_COMMENT)
                    .migrateFrom("discordScreenshotMessageStyle",
                            value -> value.replace("{screenshot_url}", TemplateParameter.IMAGE_URL.getPlaceholder()))
                    .build();

    public static final ConfigParameter<String, String> IMAGE_MESSAGE_WEBHOOK_TEMPLATE =
            jsonMessage("discordImageMessageWebhookStyle", DISCORD_IMAGE_MESSAGE_WEBHOOK_STYLE_DEFAULT, DISCORD_IMAGE_MESSAGE_WEBHOOK_STYLE_COMMENT)
                    .migrateFrom("discordScreenshotMessageWebhookStyle",
                            value -> value.replace("{screenshot_url}", TemplateParameter.IMAGE_URL.getPlaceholder()))
                    .build();

    public static final ConfigParameter<String, String> SERVER_STARTED_MESSAGE_TEMPLATE =
            jsonMessage("discordServerStartedMessageStyle", DISCORD_SERVER_STARTED_MESSAGE_STYLE_DEFAULT, DISCORD_SERVER_STARTED_MESSAGE_STYLE_COMMENT)
                    .build();

    public static final ConfigParameter<String, String> LOCAL_SERVER_STARTED_MESSAGE_TEMPLATE =
            jsonMessage("discordLocalServerStartedMessageStyle", DISCORD_LOCAL_SERVER_STARTED_MESSAGE_STYLE_DEFAULT, DISCORD_LOCAL_SERVER_STARTED_MESSAGE_STYLE_COMMENT)
                    .build();

    public static final ConfigParameter<String, String> SERVER_CLOSED_MESSAGE_TEMPLATE =
            jsonMessage("discordServerClosedMessageStyle", DISCORD_SERVER_CLOSED_MESSAGE_STYLE_DEFAULT, DISCORD_SERVER_CLOSED_MESSAGE_STYLE_COMMENT)
                    .build();

    public static final ConfigParameter<String, String> PINNED_STATUS_MESSAGE_SERVER_UNAVAILABLE_TEMPLATE =
            jsonMessage("discordPinnedStatusMessageServerUnavailableStyle", DISCORD_PINNED_STATUS_MESSAGE_SERVER_UNAVAILABLE_STYLE_DEFAULT, DISCORD_PINNED_STATUS_MESSAGE_SERVER_UNAVAILABLE_STYLE_COMMENT)
                    .build();

    public static final ConfigParameter<String, String> PINNED_STATUS_MESSAGE_SERVER_AVAILABLE_TEMPLATE =
            jsonMessage("discordPinnedStatusMessageServerAvailableStyle", DISCORD_PINNED_STATUS_MESSAGE_SERVER_AVAILABLE_STYLE_DEFAULT, DISCORD_PINNED_STATUS_MESSAGE_SERVER_AVAILABLE_STYLE_COMMENT)
                    .build();

    public static final ConfigParameter<String, String> PINNED_STATUS_MESSAGE_PLAYER_LIST_DELIMITER =
            ConfigParameter.ofString("discordPinnedStatusMessagePlayerListDelimiter")
                    .defaultValue(DISCORD_PINNED_STATUS_MESSAGE_PLAYER_LIST_DELIMITER_DEFAULT)
                    .comment(DISCORD_PINNED_STATUS_MESSAGE_PLAYER_LIST_DELIMITER_COMMENT)
                    .build();

    public static final ConfigParameter<String, String> PINNED_STATUS_MESSAGE_PLAYER_LIST_NICKNAME_TEMPLATE =
            ConfigParameter.ofString("discordPinnedStatusMessagePlayerListNicknameStyle")
                    .defaultValue(DISCORD_PINNED_STATUS_MESSAGE_PLAYER_LIST_NICKNAME_STYLE_DEFAULT)
                    .comment(DISCORD_PINNED_STATUS_MESSAGE_PLAYER_LIST_NICKNAME_STYLE_COMMENT)
                    .build();

    public static final ConfigParameter<String, String> PINNED_STATUS_MESSAGE_TEMPLATE =
            jsonMessage("discordPinnedStatusMessageStyle", DISCORD_PINNED_STATUS_MESSAGE_STYLE_DEFAULT, DISCORD_PINNED_STATUS_MESSAGE_STYLE_COMMENT)
                    .build();

    public static final ConfigParameter<String, String> GUILD_FORWARDED_MESSAGE_WEBHOOK_USERNAME_TEMPLATE =
            ConfigParameter.ofString("discordGuildForwardedMessageWebhookUsernameStyle")
                    .defaultValue(DISCORD_GUILD_FORWARDED_MESSAGE_WEBHOOK_USERNAME_STYLE_DEFAULT)
                    .comment(DISCORD_GUILD_FORWARDED_MESSAGE_WEBHOOK_USERNAME_STYLE_COMMENT)
                    .migrateFrom("discordGuildForwardedMessageUserNameStyle")
                    .build();

    public static final ConfigParameter<String, String> GUILD_FORWARDED_MESSAGE_TEMPLATE =
            jsonMessage("discordGuildForwardedMessageStyle", DISCORD_GUILD_FORWARDED_MESSAGE_STYLE_DEFAULT, DISCORD_GUILD_FORWARDED_MESSAGE_STYLE_COMMENT)
                    .build();

    private static ConfigParameterBuilder<String, String> jsonMessage(String key, String defaultValue, String comment) {
        return ConfigParameter.ofString(key)
                .defaultValue(defaultValue)
                .comment(comment)
                .normalize(value -> value.replace("\r", ""))
                .validator(DiscordChatStyleConfig::validateJsonValue);
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
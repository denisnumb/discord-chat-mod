package com.denisnumb.discord_chat_mod.discord.chat;

import com.denisnumb.discord_chat_mod.chat.template.TemplateParameter;
import com.denisnumb.discord_chat_mod.chat.template.TemplatePlaceholder;
import com.denisnumb.discord_chat_mod.config.configs.CommonConfig;
import com.denisnumb.discord_chat_mod.config.configs.DiscordChatStyleConfig;
import com.denisnumb.discord_chat_mod.discord.model.MessageType;
import com.denisnumb.discord_chat_mod.utils.ColorUtils;
import com.denisnumb.discord_chat_mod.utils.DeathMessageUtils;
import com.denisnumb.discord_chat_mod.locale.DiscordLocaleProvider;
import com.denisnumb.discord_chat_mod.utils.JavaUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.slf4j.Logger;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import static com.denisnumb.discord_chat_mod.chat.template.TemplateParameter.*;
import static com.denisnumb.discord_chat_mod.chat.template.TemplateParameter.Translatable.*;
import static com.denisnumb.discord_chat_mod.utils.JavaUtils.mergeMaps;

public final class DiscordMessageFormatter {
    private DiscordMessageFormatter() {}
    private static final Gson GSON = new Gson();
    private static final Logger LOGGER = LogUtils.getLogger();

    private static Optional<MessageEmbed> parseEmbedJson(JsonObject jsonTemplate){
        if (!jsonTemplate.has("embed"))
            return Optional.empty();

        JsonObject embed = jsonTemplate.getAsJsonObject("embed");

        if (embed.has("color") && embed.getAsJsonPrimitive("color").isString()){
            Integer parsedColor = ColorUtils.parseColor(embed.get("color").getAsString());
            if (parsedColor == null)
                embed.remove("color");
            else
                embed.addProperty("color", parsedColor);
        }

        return Optional.of(EmbedBuilder.fromData(DataObject.fromJson(GSON.toJson(embed))).build());
    }

    private static String escapeSpecialCharacters(String text){
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private static DiscordMessageComponents parseDiscordConfigTemplate(String jsonTemplate, Map<TemplatePlaceholder, String> parameterMap) {
        for (Map.Entry<TemplatePlaceholder, String> param : parameterMap.entrySet())
            jsonTemplate = jsonTemplate.replace(param.getKey().getPlaceholder(), escapeSpecialCharacters(param.getValue()));

        JsonObject parsedTemplate = GSON.fromJson(jsonTemplate, JsonObject.class);

        Optional<String> content = parsedTemplate.has("content")
                ? Optional.of(parsedTemplate.get("content").getAsString())
                : Optional.empty();

        Optional<MessageEmbed> embed = parseEmbedJson(parsedTemplate);

        return new DiscordMessageComponents(content, embed);
    }

    public static String formatDeathMessageComponents(DeathMessageUtils.DeathMessageComponents components){
        String playerTemplate = DiscordChatStyleConfig.PLAYER_DEATH_NAME_TEMPLATE.get().replace(PLAYER.getPlaceholder(), getTranslatedComponent(components.diedEntity()));
        String killerTemplate = DiscordChatStyleConfig.KILLER_ENTITY_TEMPLATE.get().replace(SECOND_ENTITY.getPlaceholder(),
                components.killerEntity() == null ? "" : getTranslatedComponent(components.killerEntity())
        );
        String weaponTemplate = DiscordChatStyleConfig.KILLER_WEAPON_TEMPLATE.get().replace(ITEM.getPlaceholder(),
                components.killerWeapon() == null ? "" : components.killerWeapon().getString()
        );

        return String.format(
                DiscordChatStyleConfig.PLAYER_DEATH_CAUSE_TEMPLATE.get()
                        .replace(DEATH_CAUSE.getPlaceholder(), DiscordLocaleProvider.getTranslate(components.deathCauseLocaleKey())),
                playerTemplate,
                killerTemplate,
                weaponTemplate
        );
    }

    private static String getTranslatedComponent(Component component){
        return component.getContents() instanceof TranslatableContents tc
                ? DiscordLocaleProvider.getTranslate(tc.getKey())
                : component.getString();
    }


    public static Optional<DiscordMessageComponents> getDiscordMessageComponents(MessageType messageType, Map<TemplatePlaceholder, String> parameterMap){
        OffsetDateTime now = JavaUtils.getDateTimeWithUtcOffset(CommonConfig.UTC_OFFSET_HOURS.get());

        parameterMap = mergeMaps(
                parameterMap,
                Map.of(TIMESTAMP, String.valueOf(now.toEpochSecond())),
                Map.of(DATETIME, now.format(DateTimeFormatter.ISO_INSTANT))
        );

        try{
            DiscordMessageComponents result = switch (messageType){
                case PINNED_STATUS_AVAILABLE ->  parseDiscordConfigTemplate(
                        setConfigTemplateTranslatableParameters(DiscordChatStyleConfig.PINNED_STATUS_MESSAGE_SERVER_AVAILABLE_TEMPLATE.get(), SERVER_AVAILABLE),
                        parameterMap
                );
                case PINNED_STATUS_UNAVAILABLE ->  parseDiscordConfigTemplate(
                        setConfigTemplateTranslatableParameters(DiscordChatStyleConfig.PINNED_STATUS_MESSAGE_SERVER_UNAVAILABLE_TEMPLATE.get(), SERVER_UNAVAILABLE),
                        parameterMap
                );
                case PINNED_STATUS_PLAYERS ->  parseDiscordConfigTemplate(DiscordChatStyleConfig.PINNED_STATUS_MESSAGE_TEMPLATE.get(), parameterMap);
                case SERVER_START -> parseDiscordConfigTemplate(
                        setConfigTemplateTranslatableParameters(DiscordChatStyleConfig.SERVER_STARTED_MESSAGE_TEMPLATE.get(), SERVER_STARTED),
                        parameterMap
                );
                case LOCAL_SERVER_START -> parseDiscordConfigTemplate(DiscordChatStyleConfig.LOCAL_SERVER_STARTED_MESSAGE_TEMPLATE.get(), parameterMap);
                case SERVER_STOP -> parseDiscordConfigTemplate(
                        setConfigTemplateTranslatableParameters(DiscordChatStyleConfig.SERVER_CLOSED_MESSAGE_TEMPLATE.get(), SERVER_CLOSED),
                        parameterMap
                );
                case CHAT -> parseDiscordConfigTemplate(DiscordChatStyleConfig.PLAYER_MESSAGE_TEMPLATE.get(), parameterMap);
                case CHAT_WEBHOOK -> parseDiscordConfigTemplate(DiscordChatStyleConfig.PLAYER_MESSAGE_WEBHOOK_TEMPLATE.get(), parameterMap);
                case IMAGE -> parseDiscordConfigTemplate(DiscordChatStyleConfig.IMAGE_MESSAGE_TEMPLATE.get(), parameterMap);
                case IMAGE_WEBHOOK -> parseDiscordConfigTemplate(DiscordChatStyleConfig.IMAGE_MESSAGE_WEBHOOK_TEMPLATE.get(), parameterMap);
                case LEFT -> parseDiscordConfigTemplate(
                        setConfigTemplateTranslatableParameters(DiscordChatStyleConfig.PLAYER_LEFT_TEMPLATE.get(), PLAYER_LEFT),
                        parameterMap
                );
                case JOIN -> parseDiscordConfigTemplate(
                        setConfigTemplateTranslatableParameters(DiscordChatStyleConfig.PLAYER_JOINED_TEMPLATE.get(), PLAYER_JOINED),
                        parameterMap
                );
                case DEATH, PET_DEATH -> parseDiscordConfigTemplate(DiscordChatStyleConfig.DEATH_MESSAGE_TEMPLATE.get(), parameterMap);
                case ADVANCEMENT_GOAL -> parseDiscordConfigTemplate(
                        setConfigTemplateTranslatableParameters(DiscordChatStyleConfig.ADVANCEMENT_GOAL_TEMPLATE.get(), ADVANCEMENT_GOAL),
                        parameterMap
                );
                case ADVANCEMENT_TASK -> parseDiscordConfigTemplate(
                        setConfigTemplateTranslatableParameters(DiscordChatStyleConfig.ADVANCEMENT_TASK_TEMPLATE.get(), ADVANCEMENT_TASK),
                        parameterMap
                );
                case ADVANCEMENT_CHALLENGE -> parseDiscordConfigTemplate(
                        setConfigTemplateTranslatableParameters(DiscordChatStyleConfig.ADVANCEMENT_CHALLENGE_TEMPLATE.get(), ADVANCEMENT_CHALLENGE),
                        parameterMap
                );
                case SAY_COMMAND -> parseDiscordConfigTemplate(DiscordChatStyleConfig.SAY_COMMAND_TEMPLATE.get(), parameterMap);
                case ME_COMMAND -> parseDiscordConfigTemplate(DiscordChatStyleConfig.ME_COMMAND_TEMPLATE.get(), parameterMap);
                case ME_COMMAND_WEBHOOK -> parseDiscordConfigTemplate(DiscordChatStyleConfig.ME_COMMAND_WEBHOOK_TEMPLATE.get(), parameterMap);
                case TELLRAW_COMMAND -> parseDiscordConfigTemplate(DiscordChatStyleConfig.TELLRAW_COMMAND_TEMPLATE.get(), parameterMap);
                case COMMAND_LOG -> parseDiscordConfigTemplate(DiscordChatStyleConfig.COMMAND_LOG_TEMPLATE.get(), parameterMap);
                case GUILD_FORWARDED_MESSAGE -> parseDiscordConfigTemplate(DiscordChatStyleConfig.GUILD_FORWARDED_MESSAGE_TEMPLATE.get(), parameterMap);
            };

            return Optional.of(result);
        } catch (Exception e){
            LOGGER.error("Error parsing discord message style for message type [{}]", messageType, e);
        }

        return Optional.empty();
    }

    private static String setConfigTemplateTranslatableParameters(String configTemplate, TemplateParameter.Translatable... translatableParameters) {
        for (TemplateParameter.Translatable param : translatableParameters)
            configTemplate = configTemplate.replace(param.getPlaceholder(), clearTranslatedString(DiscordLocaleProvider.getTranslate(param.unwrapBraces())));

        return configTemplate;
    }

    private static String clearTranslatedString(String text) {
        return text.replaceAll("%s|:|«|»", "").trim();
    }
}

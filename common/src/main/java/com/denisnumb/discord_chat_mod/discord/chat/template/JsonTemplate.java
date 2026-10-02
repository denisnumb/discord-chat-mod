package com.denisnumb.discord_chat_mod.discord.chat.template;

import com.denisnumb.discord_chat_mod.chat.template.TemplateParameter;
import com.denisnumb.discord_chat_mod.chat.template.TemplatePlaceholder;
import com.denisnumb.discord_chat_mod.discord.chat.model.DiscordMessageBody;
import com.denisnumb.discord_chat_mod.locale.DiscordLocaleProvider;
import com.denisnumb.discord_chat_mod.utils.ColorUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.data.DataObject;

import java.util.*;

public final class JsonTemplate<T extends TemplateType> {
    private static final Gson GSON = new Gson();
    private final String rawJsonTemplate;
    private final Set<TemplatePlaceholder> presentPlaceholders;
    private final TemplateParameter.Translatable translatableParam;

    public JsonTemplate(String rawJsonTemplate, T type) {
        this.rawJsonTemplate = rawJsonTemplate;
        this.presentPlaceholders = scanPresentPlaceholders(rawJsonTemplate, type.availableParameters());
        this.translatableParam = type.translatable()
                .filter(presentPlaceholders::contains)
                .orElse(null);
    }

    public String getRawTemplate() {
        return this.rawJsonTemplate;
    }

    public DiscordMessageBody applyParameters(TemplateType.ParamsBuilder<? super T> paramsBuilder) {
        Map<TemplatePlaceholder, String> params = paramsBuilder.build();

        String jsonTemplate = translatableParam != null && !params.containsKey(translatableParam)
                ? setConfigTemplateTranslatableParameters(rawJsonTemplate, translatableParam)
                : rawJsonTemplate;

        for (TemplatePlaceholder param : presentPlaceholders) {
            if (params.containsKey(param)){
                jsonTemplate = jsonTemplate.replace(param.getPlaceholder(), escapeSpecialCharacters(params.get(param)));
            }
        }


        JsonObject parsedTemplate = GSON.fromJson(jsonTemplate, JsonObject.class);

        Optional<String> content = parsedTemplate.has("content")
                ? Optional.of(parsedTemplate.get("content").getAsString())
                : Optional.empty();

        List<MessageEmbed> embeds = new ArrayList<>(parseEmbedJson(parsedTemplate).stream().toList());

        return new DiscordMessageBody(content, embeds);
    }

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

    private static String setConfigTemplateTranslatableParameters(String configTemplate, TemplateParameter.Translatable... translatableParameters) {
        for (TemplateParameter.Translatable param : translatableParameters)
            configTemplate = configTemplate.replace(param.getPlaceholder(), clearTranslatedString(DiscordLocaleProvider.getTranslate(param.translationKey())));

        return configTemplate;
    }

    private static String clearTranslatedString(String text) {
        return text.replaceAll("%s|:|«|»", "").trim();
    }

    private static String escapeSpecialCharacters(String text){
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private static Set<TemplatePlaceholder> scanPresentPlaceholders(String rawTemplate, Set<TemplatePlaceholder> availablePlaceholders) {
        Set<TemplatePlaceholder> result = new HashSet<>();
        for (TemplatePlaceholder placeholder : availablePlaceholders)
            if (rawTemplate.contains(placeholder.getPlaceholder()))
                result.add(placeholder);

        return result;
    }
}

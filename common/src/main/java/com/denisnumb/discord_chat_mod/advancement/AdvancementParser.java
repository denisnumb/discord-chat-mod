package com.denisnumb.discord_chat_mod.advancement;

import com.denisnumb.discord_chat_mod.locale.ServerLocaleProvider;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.server;

public class AdvancementParser {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(AdvancementDisplayComponent.class, new AdvancementDisplayComponentDeserializer())
            .create();

    @Nullable
    public static JsonObject getAdvancementFileAsJsonObject(Identifier resourceLocation)
    {
        try {
            Optional<Resource> resource = server.getResourceManager().getResource(resourceLocation);
            InputStreamReader reader = new InputStreamReader(resource.get().open(), StandardCharsets.UTF_8);
            return GSON.fromJson(reader, JsonObject.class);
        } catch (Exception e) {
            LOGGER.error("AdvancementFileNotFound: {}", resourceLocation);
            e.printStackTrace();
            return null;
        }
    }


    public static String getTranslatedAdvancementTitle(JsonObject advancementJson, String defaultValue){
        return getTranslatedAdvancementField("title", advancementJson, defaultValue);
    }

    public static String getTranslatedAdvancementDescription(JsonObject advancementJson, String defaultValue){
        return getTranslatedAdvancementField("description", advancementJson, defaultValue);
    }

    private static String getTranslatedAdvancementField(String fieldKey, JsonObject advancementJson, String defaultValue){
        AdvancementDisplayComponent component = parseAdvancementField(advancementJson, fieldKey);
        if (component == null)
            return defaultValue;

        String translatedValue = translateAndFormatRecursively(component);
        return (translatedValue == null || translatedValue.split("%s", -1).length-1 > 0)
                ? defaultValue
                : translatedValue;
    }

    @Nullable
    private static String translateAndFormatRecursively(AdvancementDisplayComponent component){
        String text = component.text != null
                ? component.text
                : ServerLocaleProvider.getTranslate(component.translate);

        if (text.equals(component.translate))
            return null;
        text = component.applyStyles(text);

        if (component.with != null && !component.with.isEmpty()) {
            Object[] formattedArgs = component.with.stream()
                    .map(AdvancementParser::translateAndFormatRecursively)
                    .filter(Objects::nonNull)
                    .toArray();

            if (formattedArgs.length == text.split("%s", -1).length-1) {
                return String.format(text, formattedArgs);
            }
        }

        return text;
    }

    @Nullable
    private static AdvancementDisplayComponent parseAdvancementField(JsonObject advancementJson, String fieldKey){
        if (advancementJson.has("display")) {
            JsonObject display = advancementJson.getAsJsonObject("display");
            if (display.has(fieldKey))
                return GSON.fromJson(display.get(fieldKey), AdvancementDisplayComponent.class);
        }
        return null;
    }
}

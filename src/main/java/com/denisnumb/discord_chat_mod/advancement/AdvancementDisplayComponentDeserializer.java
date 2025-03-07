package com.denisnumb.discord_chat_mod.advancement;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class AdvancementDisplayComponentDeserializer implements JsonDeserializer<AdvancementDisplayComponent> {
    private static final Type advancementDisplayComponentListType = new TypeToken<List<AdvancementDisplayComponent>>(){}.getType();

    @Override
    public AdvancementDisplayComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        AdvancementDisplayComponent component = new AdvancementDisplayComponent();

        if (json.isJsonPrimitive()) {
            component.text = json.getAsString();
        } else if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            if (obj.has("text")) component.text = obj.get("text").getAsString();
            if (obj.has("translate")) component.translate = obj.get("translate").getAsString();
            if (obj.has("bold")) component.bold = obj.get("bold").getAsBoolean();
            if (obj.has("italic")) component.italic = obj.get("italic").getAsBoolean();
            if (obj.has("strikethrough")) component.strikethrough = obj.get("strikethrough").getAsBoolean();
            if (obj.has("underlined")) component.underlined = obj.get("underlined").getAsBoolean();
            if (obj.has("obfuscated")) component.obfuscated = obj.get("obfuscated").getAsBoolean();
            if (obj.has("with")) component.with = context.deserialize(obj.get("with"), advancementDisplayComponentListType);
        }

        return component;
    }
}
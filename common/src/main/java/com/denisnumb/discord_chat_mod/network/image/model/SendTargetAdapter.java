package com.denisnumb.discord_chat_mod.network.image.model;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.List;

public class SendTargetAdapter implements JsonSerializer<SendTarget>, JsonDeserializer<SendTarget> {
    @Override
    public JsonElement serialize(SendTarget src, Type type, JsonSerializationContext ctx) {
        JsonObject obj = new JsonObject();
        if (src instanceof SendTarget.All) {
            obj.addProperty("type", "ALL");
        } else if (src instanceof SendTarget.Players players) {
            obj.addProperty("type", "PLAYERS");
            obj.add("nicknames", ctx.serialize(players.nicknames()));
        } else if (src instanceof SendTarget.Team team) {
            obj.addProperty("type", "TEAM");
            obj.addProperty("teamName", team.teamName());
        }
        return obj;
    }

    @Override
    public SendTarget deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        String kind = obj.get("type").getAsString();
        return switch (kind) {
            case "ALL" -> SendTarget.all();
            case "PLAYERS" -> SendTarget.players(
                ctx.deserialize(obj.get("nicknames"), new TypeToken<@NotNull List<String>>(){}.getType())
            );
            case "TEAM" -> SendTarget.team(obj.get("teamName").getAsString());
            default -> throw new JsonParseException("Unknown SendTarget type: " + kind);
        };
    }
}
package com.denisnumb.discord_chat_mod.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public final class MojangApiUtils {
    private MojangApiUtils() {}

    private static final String PROFILE_BY_NAME_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final Pattern PLAYER_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{1,16}$");
    private static final int TIMEOUT_MS = 5000;

    public record MojangProfile(UUID uuid, String rawId, String name) {}

    public static boolean isValidPlayerName(String name) {
        return name != null && PLAYER_NAME_PATTERN.matcher(name).matches();
    }

    public static Optional<MojangProfile> getProfileByName(String playerName) {
        if (!isValidPlayerName(playerName))
            return Optional.empty();

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URI(PROFILE_BY_NAME_URL + playerName).toURL().openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                return Optional.empty();

            try (InputStream in = connection.getInputStream()) {
                JsonObject json = JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
                String rawId = json.get("id").getAsString();
                String name = json.get("name").getAsString();
                return Optional.of(new MojangProfile(parseUndashedUuid(rawId), rawId, name));
            }
        } catch (Exception ignored) {
            return Optional.empty();
        } finally {
            if (connection != null)
                connection.disconnect();
        }
    }

    private static UUID parseUndashedUuid(String rawId) {
        String dashed = rawId.replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{12})",
                "$1-$2-$3-$4-$5"
        );
        return UUID.fromString(dashed);
    }
}

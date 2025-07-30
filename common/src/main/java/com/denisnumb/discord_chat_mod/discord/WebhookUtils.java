package com.denisnumb.discord_chat_mod.discord;

import com.denisnumb.discord_chat_mod.commands.set_avatar.AvatarUrlStorage;
import com.denisnumb.discord_chat_mod.config.IPlatformConfig;
import com.denisnumb.discord_chat_mod.config.PlatformConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.mojang.logging.LogUtils;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import static com.denisnumb.discord_chat_mod.chat_images.ImageUtils.isImageUrl;
import static com.denisnumb.discord_chat_mod.chat_images.ImageUtils.getMimeType;

public class WebhookUtils {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    private static ExecutorService EXECUTOR;

    public static class WebhookPayload {
        String username;
        @SerializedName("avatar_url")
        String avatarUrl;
        List<WebhookEmbed> embeds;
        String content;

        private record WebhookEmbed(String title, String description, int color) {}

        public WebhookPayload(String content){
            this.content = content;
        }

        public WebhookPayload(MessageEmbed embed){
            this.embeds = List.of(new WebhookEmbed(embed.getTitle(), embed.getDescription(), embed.getColorRaw()));
        }

        public WebhookPayload setUsername(String username){
            this.username = username;
            return this;
        }

        public WebhookPayload setAvatarUrl(String avatarUrl){
            this.avatarUrl = avatarUrl;
            return this;
        }
    }

    public static void initWebhookSendExecutor(){
        EXECUTOR = Executors.newSingleThreadExecutor();
    }

    public static void stopWebhookSendExecutor(){
        EXECUTOR.shutdown();
        EXECUTOR = null;
    }

    public static void sendWebhook(String webhookUrl, Supplier<WebhookPayload> payloadSupplier) {
        EXECUTOR.submit(() -> sendDiscordWebhook(webhookUrl, GSON.toJson(payloadSupplier.get())));
    }

    public static Future<Optional<String>> sendWebhookWithImage(String webhookUrl, WebhookPayload payload, byte[] imageBytes, String fileName) {
        return EXECUTOR.submit(() -> {
            try {
                String boundary = UUID.randomUUID().toString();
                HttpURLConnection connection = getHttpURLConnection(webhookUrl, "multipart/form-data; boundary=" + boundary);

                try (DataOutputStream out = new DataOutputStream(connection.getOutputStream())) {
                    String payloadJson = GSON.toJson(payload);

                    out.writeBytes("--" + boundary + "\r\n");
                    out.writeBytes("Content-Disposition: form-data; name=\"payload_json\"\r\n");
                    out.writeBytes("Content-Type: application/json\r\n\r\n");
                    out.write(payloadJson.getBytes(StandardCharsets.UTF_8));
                    out.writeBytes("\r\n");

                    out.writeBytes("--" + boundary + "\r\n");
                    out.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n");
                    out.writeBytes("Content-Type: application/octet-stream\r\n\r\n");
                    out.write(imageBytes);
                    out.writeBytes("\r\n");

                    out.writeBytes("--" + boundary + "--\r\n");
                    out.flush();
                }

                handleResponseCode(connection);
                Optional<String> imageUrl = getSentImageUrl(connection);
                connection.disconnect();

                return imageUrl;
            } catch (Exception e) {
                LOGGER.error("SendWebhookError: " + e.getMessage());
                e.printStackTrace();
            }

            return Optional.empty();
        });
    }

    public static String getWebhookServerName(){
        String configValue = PlatformConfig.getConfig().webhookServerName();
        return configValue.isBlank() ? null : configValue.replaceAll("(?i)discord", "DC");
    }

    public static String getPlayerAvatarUrl(Player player){
        IPlatformConfig config = PlatformConfig.getConfig();

        if (config.isSetAvatarUrlCommandEnabled()){
            String customAvatarUrl = AvatarUrlStorage.getUrl(player);
            if (customAvatarUrl != null)
                return customAvatarUrl;
        }

        String avatarUrlTemplate = config.webhookPlayerAvatarUrl();
        String defaultAvatarUrl = config.webhookPlayerDefaultAvatarUrl();
        String playerName = player.getName().getString();

        avatarUrlTemplate = avatarUrlTemplate.replace("<name>", playerName);

        if (avatarUrlTemplate.contains("<uuid>")){
            Optional<String> optionalUUID = getUUIDFromMojangAPI(playerName);
            if (optionalUUID.isPresent())
                avatarUrlTemplate = avatarUrlTemplate.replace("<uuid>", optionalUUID.get());
        }

        if (isImageUrl(getMimeType(avatarUrlTemplate)))
            return avatarUrlTemplate;

        return isImageUrl(getMimeType(defaultAvatarUrl))
                ? defaultAvatarUrl
                : "https://mc-heads.net/avatar.png";
    }

    public static Optional<String> getUUIDFromMojangAPI(String username) {
        try {
            URL url = new URI("https://api.mojang.com/users/profiles/minecraft/" + username).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);
            in.close();

            JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
            return Optional.of(json.get("id").getAsString());
        } catch (Exception ignored){}

        return Optional.empty();
    }

    private static void sendDiscordWebhook(String webhookUrl, String jsonPayload){
        try {
            HttpURLConnection connection = getHttpURLConnection(webhookUrl, "application/json");

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            handleResponseCode(connection);
            connection.disconnect();
        } catch (Exception e) {
            LOGGER.error("SendWebhookError: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Optional<String> getSentImageUrl(HttpURLConnection connection){
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            JsonObject responseJson = JsonParser.parseString(response.toString()).getAsJsonObject();
            JsonArray attachments = responseJson.getAsJsonArray("attachments");

            if (attachments != null && attachments.size() > 0) {
                JsonObject attachment = attachments.get(0).getAsJsonObject();
                return Optional.of(attachment.get("url").getAsString());
            }
        } catch (Exception ignored){}

        return Optional.empty();
    }

    private static void handleResponseCode(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        if (responseCode != 204 && responseCode != 200) {
            LOGGER.error("Webhook response code: " + responseCode);
            try (InputStream err = connection.getErrorStream()) {
                if (err != null) {
                    String response = new String(err.readAllBytes(), StandardCharsets.UTF_8);
                    LOGGER.error("DiscordWebhookError: " + response);
                }
            }
        }
    }

    private static HttpURLConnection getHttpURLConnection(String webhookUrl, String contentType) throws URISyntaxException, IOException {
        URL url = new URI(webhookUrl).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", contentType);
        connection.setDoOutput(true);

        return connection;
    }
}

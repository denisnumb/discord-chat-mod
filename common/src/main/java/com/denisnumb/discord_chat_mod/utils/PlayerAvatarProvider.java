package com.denisnumb.discord_chat_mod.utils;

import com.denisnumb.discord_chat_mod.commands.set_avatar.AvatarUrlStorage;
import com.denisnumb.discord_chat_mod.config.configs.WebhookModeConfig;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.properties.Property;
import net.minecraft.world.entity.player.Player;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Optional;

import static com.denisnumb.discord_chat_mod.chat_images.utils.ImageUtils.getMimeType;
import static com.denisnumb.discord_chat_mod.chat_images.utils.ImageUtils.isImageUrl;

public final class PlayerAvatarProvider {
    private PlayerAvatarProvider() {}

    public static String getPlayerAvatarUrl(Player player){
        if (WebhookModeConfig.ENABLE_SET_AVATAR_URL_COMMAND.get()){
            String customAvatarUrl = AvatarUrlStorage.getUrl(player);
            if (customAvatarUrl != null)
                return customAvatarUrl;
        }

        String avatarUrlTemplate = WebhookModeConfig.WEBHOOK_PLAYER_AVATAR_URL.get();
        String defaultAvatarUrl = WebhookModeConfig.WEBHOOK_PLAYER_DEFAULT_AVATAR_URL.get();
        String playerName = player.getName().getString();

        avatarUrlTemplate = avatarUrlTemplate.replace("<name>", playerName);

        if (avatarUrlTemplate.contains("<uuid>")){
            Optional<String> optionalUUID = MojangApiUtils.getProfileByName(playerName).map(MojangApiUtils.MojangProfile::rawId);
            if (optionalUUID.isPresent())
                avatarUrlTemplate = avatarUrlTemplate.replace("<uuid>", optionalUUID.get());
        }

        if (avatarUrlTemplate.contains("<texture>")){
            Optional<String> optionalTexture = getPlayerTextureHash(player);
            if (optionalTexture.isPresent())
                avatarUrlTemplate = avatarUrlTemplate.replace("<texture>", optionalTexture.get());
        }

        if (isImageUrl(getMimeType(avatarUrlTemplate)))
            return avatarUrlTemplate;

        return isImageUrl(getMimeType(defaultAvatarUrl))
                ? defaultAvatarUrl
                : "https://mc-heads.net/avatar/steve_head_png";
    }

    private static Optional<String> getPlayerTextureHash(Player player) {
        try {
            Collection<Property> textures = player.getGameProfile().properties().get("textures");
            if (textures.isEmpty())
                return Optional.empty();

            String encoded = textures.iterator().next().value();
            String decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
            JsonObject json = JsonParser.parseString(decoded).getAsJsonObject();
            if (!json.has("textures"))
                return Optional.empty();

            JsonObject texturesObject = json.getAsJsonObject("textures");
            if (!texturesObject.has("SKIN"))
                return Optional.empty();

            String skinUrl = texturesObject.getAsJsonObject("SKIN").get("url").getAsString();
            int slashIndex = skinUrl.lastIndexOf('/');
            if (slashIndex < 0 || slashIndex == skinUrl.length() - 1)
                return Optional.empty();

            return Optional.of(skinUrl.substring(slashIndex + 1));
        } catch (Exception ignored) {}

        return Optional.empty();
    }
}

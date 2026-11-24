package com.denisnumb.discord_chat_mod.api;

import com.denisnumb.discord_chat_mod.api.spi.ApiProvider;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Entry point of the Discord &amp; Chat Images public API.
 *
 * <p>The API is server-side only and contains no Minecraft or JDA types, so the same API artifact works on every
 * Minecraft version and mod loader supported by the mod. Compile against it with {@code compileOnly} and check that
 * the mod is loaded ({@link #MOD_ID}) before touching any API class.</p>
 *
 * <pre>{@code
 * if (FabricLoader.getInstance().isModLoaded(DiscordChatApi.MOD_ID))   // or ModList.get().isLoaded(...)
 *     MyDiscordIntegration.init();                                    // a separate class that uses the API
 *
 * // MyDiscordIntegration
 * DiscordChatApi.whenAvailable(api -> {
 *     ModHandle discord = api.forMod("mymod");
 *     ChannelKey moderation = discord.registerChannel("moderation", ChannelFallback.DISABLED, "Moderation log of MyMod");
 *     ...
 * });
 * }</pre>
 *
 * @since 1.0.0
 */
public interface DiscordChatApi {
    /**
     * Mod id of Discord &amp; Chat Images. A compile-time constant, safe to use when the mod is not installed.
     */
    String MOD_ID = "discord_chat_mod";

    /**
     * @return {@code true} if the implementation is installed (the mod has finished its own initialization)
     */
    static boolean isAvailable() {
        return ApiProvider.get() != null;
    }

    /**
     * @return the API instance, or empty if the mod has not been initialized yet
     */
    static Optional<DiscordChatApi> get() {
        return Optional.ofNullable(ApiProvider.get());
    }

    /**
     * Runs {@code callback} as soon as the API is available: immediately if it already is, otherwise right after the
     * mod finishes its initialization. Use it to avoid depending on mod loading order.
     */
    static void whenAvailable(Consumer<DiscordChatApi> callback) {
        ApiProvider.whenAvailable(callback);
    }

    /**
     * @return semantic version of the API implemented by the installed mod (for example {@code "1.0.0"}).
     * Methods and classes are annotated with {@code @since} in their documentation.
     */
    default String apiVersion() {
        return ApiVersion.VALUE;
    }

    /**
     * Returns the handle all calls of a mod go through. Every channel registered through the handle is
     * namespaced with {@code modId}, every message is attributed to it (logs, per-mod rate limit, admin block list).
     *
     * @param modId id of the calling mod: lowercase letters, digits, {@code _} and {@code -}, 2-64 characters,
     *              starting with a letter
     * @throws IllegalArgumentException if {@code modId} is invalid
     */
    ModHandle forMod(String modId);
}

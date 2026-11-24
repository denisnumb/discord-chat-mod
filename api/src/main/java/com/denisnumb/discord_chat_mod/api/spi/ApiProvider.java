package com.denisnumb.discord_chat_mod.api.spi;

import com.denisnumb.discord_chat_mod.api.DiscordChatApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Holder of the API implementation.
 *
 * <p><b>Internal.</b> Only Discord &amp; Chat Images calls {@link #install}. Other mods use the static methods of {@link DiscordChatApi}.</p>
 */
public final class ApiProvider {
    private ApiProvider() {}

    private static final Object LOCK = new Object();
    private static final List<Consumer<DiscordChatApi>> PENDING = new ArrayList<>();
    private static volatile DiscordChatApi instance;

    public static DiscordChatApi get() {
        return instance;
    }

    public static void whenAvailable(Consumer<DiscordChatApi> callback) {
        Objects.requireNonNull(callback, "callback");

        DiscordChatApi api;
        synchronized (LOCK) {
            api = instance;
            if (api == null) {
                PENDING.add(callback);
                return;
            }
        }

        callback.accept(api);
    }

    /**
     * Installs the implementation and runs the callbacks queued by {@link #whenAvailable}. A failing callback does not
     * prevent the others from running: its exception is passed to {@code errorHandler}.
     *
     * @throws IllegalStateException if an implementation is already installed
     */
    public static void install(DiscordChatApi implementation, BiConsumer<Consumer<DiscordChatApi>, RuntimeException> errorHandler) {
        Objects.requireNonNull(implementation, "implementation");

        List<Consumer<DiscordChatApi>> callbacks;
        synchronized (LOCK) {
            if (instance != null)
                throw new IllegalStateException("Discord Chat API implementation is already installed");

            instance = implementation;
            callbacks = new ArrayList<>(PENDING);
            PENDING.clear();
        }

        for (Consumer<DiscordChatApi> callback : callbacks) {
            try {
                callback.accept(implementation);
            } catch (RuntimeException e) {
                errorHandler.accept(callback, e);
            }
        }
    }
}

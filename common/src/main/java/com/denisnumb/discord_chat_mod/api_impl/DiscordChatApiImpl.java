package com.denisnumb.discord_chat_mod.api_impl;

import com.denisnumb.discord_chat_mod.DiscordChatMod;
import com.denisnumb.discord_chat_mod.api.ConnectionState;
import com.denisnumb.discord_chat_mod.api.DiscordChatApi;
import com.denisnumb.discord_chat_mod.api.ModHandle;
import com.denisnumb.discord_chat_mod.api.Registration;
import com.denisnumb.discord_chat_mod.api.spi.ApiProvider;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public final class DiscordChatApiImpl implements DiscordChatApi {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Pattern MOD_ID_PATTERN = Pattern.compile("[a-z][a-z0-9_-]{1,63}");
    private static final DiscordChatApiImpl INSTANCE = new DiscordChatApiImpl();
    private static final List<Consumer<ConnectionState>> CONNECTION_LISTENERS = new CopyOnWriteArrayList<>();
    private static final AtomicBoolean CONNECTED = new AtomicBoolean(false);

    private final Map<String, ModHandleImpl> handles = new ConcurrentHashMap<>();

    private DiscordChatApiImpl() {}

    public static void install() {
        ApiProvider.install(INSTANCE, (callback, error) ->
                LOGGER.error("[api] A Discord Chat API callback of another mod ({}) failed", callback.getClass().getName(), error));
        LOGGER.info("[api] Discord Chat API {} is available", INSTANCE.apiVersion());
    }

    @Override
    public ModHandle forMod(String modId) {
        Objects.requireNonNull(modId, "modId");
        if (!MOD_ID_PATTERN.matcher(modId).matches())
            throw new IllegalArgumentException("Invalid mod id \"" + modId + "\": expected [a-z][a-z0-9_-]{1,63}");
        if (modId.equals(DiscordChatApi.MOD_ID))
            throw new IllegalArgumentException("Use the id of your own mod");

        return handles.computeIfAbsent(modId, ModHandleImpl::new);
    }

    public static void onDiscordConnected() {
        if (CONNECTED.compareAndSet(false, true))
            fireConnectionChanged(ConnectionState.CONNECTED);
    }

    public static void onDiscordDisconnecting() {
        if (CONNECTED.compareAndSet(true, false))
            fireConnectionChanged(ConnectionState.DISCONNECTED);
    }

    static boolean isConnected() {
        return CONNECTED.get() && DiscordChatMod.isDiscordConnected();
    }

    static Registration addConnectionListener(String modId, Consumer<ConnectionState> listener) {
        Objects.requireNonNull(listener, "listener");

        Consumer<ConnectionState> wrapped = state -> {
            try {
                listener.accept(state);
            } catch (RuntimeException e) {
                LOGGER.error("[api] Connection listener of mod \"{}\" failed", modId, e);
            }
        };
        CONNECTION_LISTENERS.add(wrapped);

        AtomicBoolean registered = new AtomicBoolean(true);
        return () -> {
            if (registered.compareAndSet(true, false))
                CONNECTION_LISTENERS.remove(wrapped);
        };
    }

    private static void fireConnectionChanged(ConnectionState state) {
        for (Consumer<ConnectionState> listener : CONNECTION_LISTENERS)
            listener.accept(state);
    }
}

package com.denisnumb.discord_chat_mod.api_impl;

import com.denisnumb.discord_chat_mod.api.ChannelFallback;
import com.denisnumb.discord_chat_mod.api.ChannelKey;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ApiRegistry {
    private ApiRegistry() {}

    private static final Map<ChannelKey, ChannelDefinition> CHANNELS = new ConcurrentHashMap<>();

    public record ChannelDefinition(ChannelKey key, ChannelFallback fallback, String description) {
        public String configComment() {
            String fallbackText = fallback == ChannelFallback.DEFAULT_CHANNEL
                    ? "the default channel"
                    : "disabled";
            return " " + description.strip() + " (mod: " + key.namespace() + "). If empty: " + fallbackText + ".";
        }
    }

    public static void registerChannel(ChannelDefinition definition) {
        CHANNELS.put(definition.key(), definition);
    }

    public static ChannelDefinition getChannel(ChannelKey key) {
        return CHANNELS.get(key);
    }

    public static Collection<ChannelDefinition> getChannels() {
        return List.copyOf(CHANNELS.values());
    }
}

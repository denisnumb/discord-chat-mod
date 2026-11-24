package com.denisnumb.discord_chat_mod.api;

/**
 * Identifier of a Discord channel slot: one of the mod's {@linkplain Channels built-in channels} or a channel
 * registered by another mod with {@link ModHandle#registerChannel}. The actual Discord channel is chosen per guild by
 * the server administrator.
 *
 * @since 1.0.0
 */
public final class ChannelKey {
    private final String namespace;
    private final String name;

    private ChannelKey(String namespace, String name) {
        this.namespace = namespace;
        this.name = name;
    }

    /**
     * Creates a key, for example to send to a channel registered by another mod. Sending to a key that nobody has
     * registered completes with {@link SendStatus#UNKNOWN_CHANNEL}.
     *
     * @throws IllegalArgumentException if the namespace or the name is invalid
     */
    public static ChannelKey of(String namespace, String name) {
        return new ChannelKey(Names.namespace(namespace), Names.name(name));
    }

    public String namespace() {
        return namespace;
    }

    public String name() {
        return name;
    }

    /**
     * @return {@code true} for the mod's own channels from {@link Channels}
     */
    public boolean isBuiltin() {
        return DiscordChatApi.MOD_ID.equals(namespace);
    }

    /**
     * @return {@code "<namespace>:<name>"}, the key used in the config
     */
    @Override
    public String toString() {
        return namespace + ":" + name;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof ChannelKey other && namespace.equals(other.namespace) && name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return 31 * namespace.hashCode() + name.hashCode();
    }
}

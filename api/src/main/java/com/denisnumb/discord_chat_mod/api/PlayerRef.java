package com.denisnumb.discord_chat_mod.api;

import java.util.Objects;
import java.util.UUID;

/**
 * A Minecraft player, online or offline.
 *
 * @since 1.0.0
 */
public final class PlayerRef {
    private final UUID uuid;
    private final String name;

    private PlayerRef(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    /**
     * @param uuid the player's UUID
     * @param name the player's name, used when the player is offline (online players use their current display name)
     */
    public static PlayerRef of(UUID uuid, String name) {
        Objects.requireNonNull(uuid, "uuid");
        Objects.requireNonNull(name, "name");
        if (name.isBlank())
            throw new IllegalArgumentException("Player name is blank");
        return new PlayerRef(uuid, name);
    }

    public UUID uuid() {
        return uuid;
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof PlayerRef other && uuid.equals(other.uuid) && name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode() * 31 + name.hashCode();
    }

    @Override
    public String toString() {
        return name + " (" + uuid + ")";
    }
}

package com.denisnumb.discord_chat_mod.api;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A link between a Minecraft account and a Discord user.
 *
 * @since 1.0.0
 */
public final class LinkedAccount {
    private final UUID playerUuid;
    private final String playerName;
    private final String discordUserId;
    private final Instant linkedAt;

    private LinkedAccount(UUID playerUuid, String playerName, String discordUserId, Instant linkedAt) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.discordUserId = discordUserId;
        this.linkedAt = linkedAt;
    }

    /**
     * Used by the implementation.
     */
    public static LinkedAccount of(UUID playerUuid, String playerName, String discordUserId, Instant linkedAt) {
        return new LinkedAccount(
                Objects.requireNonNull(playerUuid, "playerUuid"),
                Objects.requireNonNull(playerName, "playerName"),
                Objects.requireNonNull(discordUserId, "discordUserId"),
                Objects.requireNonNull(linkedAt, "linkedAt")
        );
    }

    public UUID playerUuid() {
        return playerUuid;
    }

    /**
     * @return the player's name when they last joined
     */
    public String playerName() {
        return playerName;
    }

    public String discordUserId() {
        return discordUserId;
    }

    public Instant linkedAt() {
        return linkedAt;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof LinkedAccount other
                && playerUuid.equals(other.playerUuid)
                && discordUserId.equals(other.discordUserId);
    }

    @Override
    public int hashCode() {
        return playerUuid.hashCode() * 31 + discordUserId.hashCode();
    }

    @Override
    public String toString() {
        return "LinkedAccount{" + playerName + " (" + playerUuid + ") <-> " + discordUserId + "}";
    }
}

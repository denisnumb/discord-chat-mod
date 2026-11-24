package com.denisnumb.discord_chat_mod.api;

import java.util.Objects;
import java.util.Optional;

/**
 * Result of sending a message to one channel of one guild.
 *
 * @since 1.0.0
 */
public final class Delivery {
    private final String guildId;
    private final String channelId;
    private final String messageId;
    private final SendStatus status;
    private final String error;

    private Delivery(String guildId, String channelId, String messageId, SendStatus status, String error) {
        this.guildId = Objects.requireNonNull(guildId, "guildId");
        this.channelId = channelId;
        this.messageId = messageId;
        this.status = Objects.requireNonNull(status, "status");
        this.error = error;
    }

    /**
     * Creates a successful delivery. Used by the implementation.
     */
    public static Delivery sent(String guildId, String channelId, String messageId) {
        return new Delivery(guildId, Objects.requireNonNull(channelId), Objects.requireNonNull(messageId), SendStatus.SENT, null);
    }

    /**
     * Creates a delivery to a channel that is disabled in the guild. Used by the implementation.
     */
    public static Delivery disabled(String guildId) {
        return new Delivery(guildId, null, null, SendStatus.CHANNEL_DISABLED, null);
    }

    /**
     * Creates a failed delivery. Used by the implementation.
     */
    public static Delivery failed(String guildId, String channelId, String error) {
        return new Delivery(guildId, channelId, null, SendStatus.FAILED, error);
    }

    public String guildId() {
        return guildId;
    }

    /**
     * @return the Discord channel (or thread) id, empty if the channel is disabled
     */
    public Optional<String> channelId() {
        return Optional.ofNullable(channelId);
    }

    /**
     * @return id of the sent message
     */
    public Optional<String> messageId() {
        return Optional.ofNullable(messageId);
    }

    /**
     * @return {@link SendStatus#SENT}, {@link SendStatus#CHANNEL_DISABLED} or {@link SendStatus#FAILED}
     */
    public SendStatus status() {
        return status;
    }

    /**
     * @return reason of a failure
     */
    public Optional<String> error() {
        return Optional.ofNullable(error);
    }

    @Override
    public String toString() {
        return "Delivery{guild=" + guildId + ", channel=" + channelId + ", message=" + messageId + ", status=" + status
                + (error == null ? "" : ", error=" + error) + "}";
    }
}

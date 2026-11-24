package com.denisnumb.discord_chat_mod.api;

/**
 * Outcome of sending a message: of the whole {@link SendResult} or of one {@link Delivery}.
 *
 * @since 1.0.0
 */
public enum SendStatus {
    /**
     * Sent to every channel it was routed to.
     */
    SENT,
    /**
     * Sent to some channels, failed in others (see the deliveries). Only used for {@link SendResult#status()}.
     */
    PARTIALLY_SENT,
    /**
     * The channel is disabled in every guild (or in this guild, for a delivery).
     */
    CHANNEL_DISABLED,
    /**
     * No guild is configured or none could be initialized.
     */
    NO_GUILDS,
    /**
     * The bot is not connected to Discord. Nothing was sent; the message is not queued.
     */
    NOT_CONNECTED,
    /**
     * Nobody has registered this channel key.
     */
    UNKNOWN_CHANNEL,
    /**
     * The administrator disabled the API or this mod in the {@code [api]} config section.
     */
    BLOCKED_BY_ADMIN,
    /**
     * The mod exceeded the per-mod message limit set by the administrator. Nothing was sent.
     */
    RATE_LIMITED,
    /**
     * The {@link Sender#player} sender is vanished. Nothing was sent.
     */
    SENDER_HIDDEN,
    /**
     * Discord rejected the message or an error occurred (see {@link Delivery#error()}).
     */
    FAILED
}

package com.denisnumb.discord_chat_mod.api;

/**
 * Where messages of a mod channel go while the administrator has not set a channel id for it.
 *
 * @since 1.0.0
 */
public enum ChannelFallback {
    /**
     * To the guild's {@code defaultChannelId}. Suitable for public announcements.
     */
    DEFAULT_CHANNEL,
    /**
     * Nowhere: the channel is disabled until configured. Suitable for staff-only information (moderation logs etc.).
     */
    DISABLED
}

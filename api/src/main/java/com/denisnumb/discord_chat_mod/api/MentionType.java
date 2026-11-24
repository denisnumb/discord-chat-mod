package com.denisnumb.discord_chat_mod.api;

/**
 * Kinds of Discord mentions that may notify (ping) their targets.
 *
 * @since 1.0.0
 */
public enum MentionType {
    /**
     * {@code <@userId>}
     */
    USERS,
    /**
     * {@code <@&roleId>}
     */
    ROLES,
    /**
     * {@code @everyone} and {@code @here}. The administrator can forbid them for all API messages.
     */
    EVERYONE
}

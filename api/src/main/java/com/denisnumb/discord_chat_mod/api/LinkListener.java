package com.denisnumb.discord_chat_mod.api;

/**
 * Listener of account link changes, see {@link LinkedAccounts#addListener}.
 *
 * @since 1.0.0
 */
public interface LinkListener {
    /**
     * A Minecraft account was linked to a Discord user.
     */
    default void onLinked(LinkedAccount link) {}

    /**
     * A link was removed.
     */
    default void onUnlinked(LinkedAccount link, UnlinkReason reason) {}
}

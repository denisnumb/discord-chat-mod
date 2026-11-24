package com.denisnumb.discord_chat_mod.api;

/**
 * A listener subscription.
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface Registration {
    /**
     * Removes the listener. Calling it more than once has no effect.
     */
    void unregister();
}

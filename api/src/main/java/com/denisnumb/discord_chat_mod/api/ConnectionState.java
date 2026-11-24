package com.denisnumb.discord_chat_mod.api;

/**
 * Discord connection state of the bot.
 *
 * @since 1.0.0
 */
public enum ConnectionState {
    /**
     * The bot is connected and the guilds are initialized: messages can be sent.
     */
    CONNECTED,
    /**
     * The bot is disconnecting or disconnected (server stop, config reload, connection error).
     */
    DISCONNECTED
}

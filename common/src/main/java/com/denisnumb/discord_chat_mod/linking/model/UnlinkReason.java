package com.denisnumb.discord_chat_mod.linking.model;

public enum UnlinkReason {
    /**
     * /discord unlink in the game
     */
    PLAYER,
    /**
     * /unlink in Discord
     */
    DISCORD_USER,
    /**
     * Unlinked by an administrator
     */
    ADMIN,
    /**
     * The member left the primary guild
     */
    GUILD_LEFT,
    /**
     * The Minecraft account was linked to another Discord user (the Minecraft account itself stays linked)
     */
    REPLACED
}

package com.denisnumb.discord_chat_mod.api;

/**
 * Why a link was removed.
 *
 * @since 1.0.0
 */
public enum UnlinkReason {
    /**
     * The player used {@code /discord unlink} in the game.
     */
    PLAYER,
    /**
     * The user used {@code /unlink} in Discord.
     */
    DISCORD_USER,
    /**
     * An administrator removed the link.
     */
    ADMIN,
    /**
     * The Discord user left the guild.
     */
    GUILD_LEFT,
    /**
     * The Minecraft account was linked to another Discord user.
     */
    REPLACED,
    /**
     * Any other reason. Handle it to stay compatible with future versions of the mod.
     */
    OTHER
}

package com.denisnumb.discord_chat_mod.api;

/**
 * Built-in channels of the mod. They follow the mod's own routing: a channel without an override in
 * {@code channelOverrides} is the guild's default channel, {@code "-1"} disables it, and {@code duplicateMessages}
 * also copies the message to the default channel.
 *
 * @since 1.0.0
 */
public final class Channels {
    private Channels() {
    }

    /**
     * The guild's {@code defaultChannelId}, the channel bridged with the game chat.
     */
    public static final ChannelKey DEFAULT = builtin("default");
    /**
     * Player chat messages ({@code playerChatMessagesChannelId}).
     */
    public static final ChannelKey CHAT = builtin("chat");
    /**
     * Player join/leave messages ({@code playerJoinLeaveChannelId}).
     */
    public static final ChannelKey JOIN_LEAVE = builtin("join_leave");
    /**
     * Death messages ({@code deathsChannelId}).
     */
    public static final ChannelKey DEATHS = builtin("deaths");
    /**
     * Advancement messages ({@code advancementsChannelId}).
     */
    public static final ChannelKey ADVANCEMENTS = builtin("advancements");
    /**
     * Server start/stop messages ({@code serverStartStopChannelId}).
     */
    public static final ChannelKey SERVER_START_STOP = builtin("server_start_stop");
    /**
     * Log of player commands ({@code commandLogChannelId}). Usually visible to staff only.
     */
    public static final ChannelKey COMMAND_LOG = builtin("command_log");
    /**
     * Server console log channel ({@code serverLogsChannelId}). Disabled if not set. Usually visible to staff only.
     */
    public static final ChannelKey SERVER_LOGS = builtin("server_logs");

    private static ChannelKey builtin(String name) {
        return ChannelKey.of(DiscordChatApi.MOD_ID, name);
    }
}

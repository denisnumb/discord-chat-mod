package com.denisnumb.discord_chat_mod.api;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * API operations on behalf of one mod. Obtained with {@link DiscordChatApi#forMod(String)}; thread-safe.
 *
 * @since 1.0.0
 */
public interface ModHandle {
    /**
     * @return id of the mod this handle belongs to
     */
    String modId();

    /**
     * @return {@code true} if the bot is connected to Discord and messages can be sent right now. The state changes at
     * runtime: Discord connects after the server has started and reconnects when the config is reloaded.
     */
    boolean isConnected();

    /**
     * Subscribes to connection state changes. The listener is called from the thread that connects or disconnects
     * the bot, never from the server thread: do not touch the world from it.
     */
    Registration addConnectionListener(Consumer<ConnectionState> listener);

    /**
     * Registers a Discord channel of this mod. The server administrator sets the channel id for every guild in
     * {@code [guilds.modChannels]} of {@code discord_chat_mod-common.toml}; the key ({@code "<modId>:<name>"}) is added
     * there automatically with {@code description} as a comment.
     *
     * <p>Registering the same name again returns the same key and updates the fallback and the description.
     * Registration does not require a Discord connection and can be done at any time, preferably during mod
     * initialization so that the key appears in the config before the administrator edits it.</p>
     *
     * <p>Unlike built-in channels, mod channels are never duplicated to the default channel by
     * {@code duplicateMessages}: a staff-only channel cannot leak into the public chat.</p>
     *
     * @param name        channel name: lowercase letters, digits, {@code _} and {@code -}, 1-64 characters
     * @param fallback    where messages go while the administrator has not set a channel id
     * @param description shown to the administrator in the config
     * @throws IllegalArgumentException if the name is invalid
     */
    ChannelKey registerChannel(String name, ChannelFallback fallback, String description);

    /**
     * Cheap check that does not contact Discord: whether the bot is connected and {@code channel} is enabled in the
     * config of at least one guild. Use it to avoid preparing expensive messages for nothing. It does not verify that
     * the configured channel exists or that the bot may write there.
     */
    boolean isChannelEnabled(ChannelKey channel);

    /**
     * Sends a message to {@code channel} in every configured guild.
     *
     * <p>Never blocks and may be called from any thread. The future completes on a Discord thread: to touch the world
     * from the callback, hop to the server thread first. Expected outcomes (channel disabled, not connected, rate
     * limited...) complete the future normally, see {@link SendResult#status()}.</p>
     */
    CompletableFuture<SendResult> send(ChannelKey channel, DiscordMessage message);

    /**
     * @return access to Minecraft ↔ Discord account linking
     */
    LinkedAccounts accounts();
}

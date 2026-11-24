package com.denisnumb.discord_chat_mod.api;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Who the message appears to be from.
 *
 * <p>In webhook mode ({@code enableWebhookMode}) the name and avatar of the webhook message are set accordingly.</p>
 *
 * <p>The text content of {@link #player} and {@link #custom} senders is formatted like chat messages of players, with
 * the styles set by the server administrator: {@code discordPlayerMessageWebhookStyle} in webhook mode,
 * {@code discordPlayerMessageStyle} otherwise ({@code {player}} is the sender name, {@code {message}} the text). Embeds
 * and files are added as is; a message without text is not formatted. The text of the {@link #server} sender is sent
 * as is.</p>
 *
 * @since 1.0.0
 */
public final class Sender {
    /**
     * Sender kind.
     */
    public enum Kind {
        /**
         * The server: the same name and avatar as the mod's own server messages.
         */
        SERVER,
        /**
         * A player: their display name and the avatar configured for players.
         */
        PLAYER,
        /**
         * An arbitrary name and avatar.
         */
        CUSTOM
    }

    private static final Sender SERVER = new Sender(Kind.SERVER, null, null, null);

    private final Kind kind;
    private final PlayerRef player;
    private final String name;
    private final String avatarUrl;

    private Sender(Kind kind, PlayerRef player, String name, String avatarUrl) {
        this.kind = kind;
        this.player = player;
        this.name = name;
        this.avatarUrl = avatarUrl;
    }

    /**
     * The default sender. The text is sent as is, without a chat style.
     */
    public static Sender server() {
        return SERVER;
    }

    /**
     * The message appears to be from the player, like their chat messages. Works for offline players too.
     * Messages from a player who is currently vanished (Melius Vanish, Vanishmod) are not sent:
     * {@link SendStatus#SENDER_HIDDEN}.
     */
    public static Sender player(UUID uuid, String name) {
        return player(PlayerRef.of(uuid, name));
    }

    /**
     * @see #player(UUID, String)
     */
    public static Sender player(PlayerRef player) {
        return new Sender(Kind.PLAYER, Objects.requireNonNull(player, "player"), null, null);
    }

    /**
     * An arbitrary sender, for example the name of a mod feature ("Auction House"). Discord forbids some words in
     * webhook names, they are replaced. The name is cut to 80 characters.
     *
     * @param name      displayed name
     * @param avatarUrl avatar image URL, or {@code null} for the webhook's default avatar
     */
    public static Sender custom(String name, String avatarUrl) {
        Objects.requireNonNull(name, "name");
        if (name.isBlank())
            throw new IllegalArgumentException("Sender name is blank");
        return new Sender(Kind.CUSTOM, null, name.strip(), avatarUrl);
    }

    public Kind kind() {
        return kind;
    }

    /**
     * @return the player for {@link Kind#PLAYER}
     */
    public Optional<PlayerRef> player() {
        return Optional.ofNullable(player);
    }

    /**
     * @return the name for {@link Kind#CUSTOM}
     */
    public Optional<String> name() {
        return Optional.ofNullable(name);
    }

    /**
     * @return the avatar URL for {@link Kind#CUSTOM}
     */
    public Optional<String> avatarUrl() {
        return Optional.ofNullable(avatarUrl);
    }
}

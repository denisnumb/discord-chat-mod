package com.denisnumb.discord_chat_mod.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * An immutable message to send with {@link ModHandle#send}: text content, embeds and files, the sender and allowed mentions.
 *
 * <pre>{@code
 * DiscordMessage.text("**Steve** has been banned: griefing");
 *
 * DiscordMessage.builder()
 *     .sender(Sender.player(uuid, "Steve"))
 *     .content("sold a diamond for 64$")
 *     .embed(Embed.builder().title("Lot #42").field("Price", "64$", true).build())
 *     .build();
 * }</pre>
 *
 * @since 1.0.0
 */
public final class DiscordMessage {
    public static final int MAX_CONTENT_LENGTH = 2000;
    public static final int MAX_EMBEDS = 10;
    public static final int MAX_FILES = 10;

    private final String content;
    private final List<Embed> embeds;
    private final List<Attachment> files;
    private final Sender sender;
    private final AllowedMentions allowedMentions;

    private DiscordMessage(Builder b) {
        this.content = b.content;
        this.embeds = Collections.unmodifiableList(new ArrayList<>(b.embeds));
        this.files = Collections.unmodifiableList(new ArrayList<>(b.files));
        this.sender = b.sender;
        this.allowedMentions = b.allowedMentions;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Shortcut for a plain text message from the server.
     *
     * @param content Discord markdown, up to {@value #MAX_CONTENT_LENGTH} characters
     */
    public static DiscordMessage text(String content) {
        return builder().content(content).build();
    }

    /**
     * @return text content, Discord markdown
     */
    public Optional<String> content() {
        return Optional.ofNullable(content);
    }

    public List<Embed> embeds() {
        return embeds;
    }

    public List<Attachment> files() {
        return files;
    }

    public Sender sender() {
        return sender;
    }

    public AllowedMentions allowedMentions() {
        return allowedMentions;
    }

    /**
     * Builder of {@link DiscordMessage}.
     */
    public static final class Builder {
        private String content;
        private final List<Embed> embeds = new ArrayList<>();
        private final List<Attachment> files = new ArrayList<>();
        private Sender sender = Sender.server();
        private AllowedMentions allowedMentions = AllowedMentions.none();

        private Builder() {
        }

        /**
         * Text content, Discord markdown. Use {@link DiscordText#escapeMarkdown} for text written by players.
         */
        public Builder content(String content) {
            Objects.requireNonNull(content, "content");
            if (content.length() > MAX_CONTENT_LENGTH)
                throw new IllegalArgumentException("Content is longer than " + MAX_CONTENT_LENGTH + " characters");
            this.content = content;
            return this;
        }

        /**
         * Adds an embed.
         */
        public Builder embed(Embed embed) {
            Objects.requireNonNull(embed, "embed");
            if (embeds.size() >= MAX_EMBEDS)
                throw new IllegalArgumentException("A message cannot have more than " + MAX_EMBEDS + " embeds");
            embeds.add(embed);
            return this;
        }

        /**
         * Attaches a file.
         */
        public Builder file(Attachment file) {
            Objects.requireNonNull(file, "file");
            if (files.size() >= MAX_FILES)
                throw new IllegalArgumentException("A message cannot have more than " + MAX_FILES + " files");
            files.add(file);
            return this;
        }

        /**
         * @see Attachment#of(String, byte[])
         */
        public Builder file(String fileName, byte[] data) {
            return file(Attachment.of(fileName, data));
        }

        /**
         * Default: {@link Sender#server()}.
         */
        public Builder sender(Sender sender) {
            this.sender = Objects.requireNonNull(sender, "sender");
            return this;
        }

        /**
         * Default: {@link AllowedMentions#none()}.
         */
        public Builder allowedMentions(AllowedMentions allowedMentions) {
            this.allowedMentions = Objects.requireNonNull(allowedMentions, "allowedMentions");
            return this;
        }

        /**
         * @throws IllegalArgumentException if the message has neither content, nor embeds, nor files, or if its
         *                                  embeds exceed {@link Embed#MAX_TOTAL_LENGTH}
         */
        public DiscordMessage build() {
            if ((content == null || content.isBlank()) && embeds.isEmpty() && files.isEmpty())
                throw new IllegalArgumentException("Message is empty");

            int embedLength = embeds.stream().mapToInt(Embed::textLength).sum();
            if (embedLength > Embed.MAX_TOTAL_LENGTH)
                throw new IllegalArgumentException("Embeds text is longer than " + Embed.MAX_TOTAL_LENGTH + " characters");

            return new DiscordMessage(this);
        }
    }
}

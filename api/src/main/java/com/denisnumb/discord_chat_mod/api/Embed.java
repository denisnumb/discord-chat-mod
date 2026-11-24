package com.denisnumb.discord_chat_mod.api;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A Discord embed. Limits are checked by {@link Builder#build()} so that an invalid embed fails in the calling code,
 * not later on a Discord thread.
 *
 * @since 1.0.0
 */
public final class Embed {
    public static final int MAX_TITLE_LENGTH = 256;
    public static final int MAX_DESCRIPTION_LENGTH = 4096;
    public static final int MAX_FIELDS = 25;
    public static final int MAX_FIELD_NAME_LENGTH = 256;
    public static final int MAX_FIELD_VALUE_LENGTH = 1024;
    public static final int MAX_FOOTER_LENGTH = 2048;
    public static final int MAX_AUTHOR_LENGTH = 256;
    /**
     * Limit of the total text length of all embeds of one message.
     */
    public static final int MAX_TOTAL_LENGTH = 6000;

    /**
     * An embed field.
     */
    public static final class Field {
        private final String name;
        private final String value;
        private final boolean inline;

        private Field(String name, String value, boolean inline) {
            this.name = name;
            this.value = value;
            this.inline = inline;
        }

        public String name() {
            return name;
        }

        public String value() {
            return value;
        }

        public boolean inline() {
            return inline;
        }
    }

    private final String title;
    private final String url;
    private final String description;
    private final Integer color;
    private final String authorName;
    private final String authorUrl;
    private final String authorIconUrl;
    private final String footerText;
    private final String footerIconUrl;
    private final String thumbnailUrl;
    private final String imageUrl;
    private final Instant timestamp;
    private final List<Field> fields;

    private Embed(Builder b) {
        this.title = b.title;
        this.url = b.url;
        this.description = b.description;
        this.color = b.color;
        this.authorName = b.authorName;
        this.authorUrl = b.authorUrl;
        this.authorIconUrl = b.authorIconUrl;
        this.footerText = b.footerText;
        this.footerIconUrl = b.footerIconUrl;
        this.thumbnailUrl = b.thumbnailUrl;
        this.imageUrl = b.imageUrl;
        this.timestamp = b.timestamp;
        this.fields = Collections.unmodifiableList(new ArrayList<>(b.fields));
    }

    public static Builder builder() {
        return new Builder();
    }

    public Optional<String> title() {
        return Optional.ofNullable(title);
    }

    /**
     * @return URL the title links to
     */
    public Optional<String> url() {
        return Optional.ofNullable(url);
    }

    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    /**
     * @return RGB color, {@code 0xRRGGBB}
     */
    public Optional<Integer> color() {
        return Optional.ofNullable(color);
    }

    public Optional<String> authorName() {
        return Optional.ofNullable(authorName);
    }

    public Optional<String> authorUrl() {
        return Optional.ofNullable(authorUrl);
    }

    public Optional<String> authorIconUrl() {
        return Optional.ofNullable(authorIconUrl);
    }

    public Optional<String> footerText() {
        return Optional.ofNullable(footerText);
    }

    public Optional<String> footerIconUrl() {
        return Optional.ofNullable(footerIconUrl);
    }

    public Optional<String> thumbnailUrl() {
        return Optional.ofNullable(thumbnailUrl);
    }

    public Optional<String> imageUrl() {
        return Optional.ofNullable(imageUrl);
    }

    public Optional<Instant> timestamp() {
        return Optional.ofNullable(timestamp);
    }

    public List<Field> fields() {
        return fields;
    }

    /**
     * @return length of all texts of the embed, counted against {@link #MAX_TOTAL_LENGTH}
     */
    public int textLength() {
        int length = len(title) + len(description) + len(authorName) + len(footerText);
        for (Field field : fields)
            length += field.name.length() + field.value.length();
        return length;
    }

    private static int len(String s) {
        return s == null ? 0 : s.length();
    }

    /**
     * Builder of {@link Embed}. URLs must start with {@code https://}, {@code http://} or {@code attachment://}.
     */
    public static final class Builder {
        private String title;
        private String url;
        private String description;
        private Integer color;
        private String authorName;
        private String authorUrl;
        private String authorIconUrl;
        private String footerText;
        private String footerIconUrl;
        private String thumbnailUrl;
        private String imageUrl;
        private Instant timestamp;
        private final List<Field> fields = new ArrayList<>();

        private Builder() {
        }

        public Builder title(String title) {
            this.title = text(title, MAX_TITLE_LENGTH, "Title");
            return this;
        }

        /**
         * @param title title text
         * @param url   URL the title links to
         */
        public Builder title(String title, String url) {
            title(title);
            this.url = url(url);
            return this;
        }

        /**
         * @param description Discord markdown
         */
        public Builder description(String description) {
            this.description = text(description, MAX_DESCRIPTION_LENGTH, "Description");
            return this;
        }

        /**
         * @param rgb {@code 0xRRGGBB}
         */
        public Builder color(int rgb) {
            if (rgb < 0 || rgb > 0xFFFFFF)
                throw new IllegalArgumentException("Color must be in range 0x000000..0xFFFFFF");
            this.color = rgb;
            return this;
        }

        public Builder author(String name) {
            return author(name, null, null);
        }

        /**
         * @param name    author text
         * @param url     URL the author text links to, or {@code null}
         * @param iconUrl small icon before the author text, or {@code null}
         */
        public Builder author(String name, String url, String iconUrl) {
            this.authorName = text(name, MAX_AUTHOR_LENGTH, "Author name");
            this.authorUrl = url == null ? null : url(url);
            this.authorIconUrl = iconUrl == null ? null : url(iconUrl);
            return this;
        }

        public Builder footer(String text) {
            return footer(text, null);
        }

        /**
         * @param iconUrl small icon before the footer text, or {@code null}
         */
        public Builder footer(String text, String iconUrl) {
            this.footerText = text(text, MAX_FOOTER_LENGTH, "Footer");
            this.footerIconUrl = iconUrl == null ? null : url(iconUrl);
            return this;
        }

        public Builder thumbnail(String url) {
            this.thumbnailUrl = url(url);
            return this;
        }

        public Builder image(String url) {
            this.imageUrl = url(url);
            return this;
        }

        /**
         * @param timestamp shown in the footer in the reader's local time
         */
        public Builder timestamp(Instant timestamp) {
            this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
            return this;
        }

        /**
         * @param name   field name
         * @param value  field value, Discord markdown
         * @param inline whether the field may be placed next to other inline fields
         */
        public Builder field(String name, String value, boolean inline) {
            if (fields.size() >= MAX_FIELDS)
                throw new IllegalArgumentException("An embed cannot have more than " + MAX_FIELDS + " fields");
            fields.add(new Field(
                    text(name, MAX_FIELD_NAME_LENGTH, "Field name"),
                    text(value, MAX_FIELD_VALUE_LENGTH, "Field value"),
                    inline
            ));
            return this;
        }

        /**
         * @throws IllegalArgumentException if the embed is empty or longer than {@link #MAX_TOTAL_LENGTH}
         */
        public Embed build() {
            Embed embed = new Embed(this);
            if (embed.textLength() == 0 && thumbnailUrl == null && imageUrl == null)
                throw new IllegalArgumentException("Embed is empty");
            if (embed.textLength() > MAX_TOTAL_LENGTH)
                throw new IllegalArgumentException("Embed text is longer than " + MAX_TOTAL_LENGTH + " characters");
            return embed;
        }

        private static String text(String value, int maxLength, String what) {
            Objects.requireNonNull(value, what);
            if (value.isBlank())
                throw new IllegalArgumentException(what + " is blank");
            if (value.length() > maxLength)
                throw new IllegalArgumentException(what + " is longer than " + maxLength + " characters");
            return value;
        }

        private static String url(String value) {
            Objects.requireNonNull(value, "url");
            if (!value.startsWith("https://") && !value.startsWith("http://") && !value.startsWith("attachment://"))
                throw new IllegalArgumentException("URL must start with https://, http:// or attachment://: " + value);
            return value;
        }
    }
}

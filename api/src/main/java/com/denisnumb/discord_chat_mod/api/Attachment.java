package com.denisnumb.discord_chat_mod.api;

import java.util.Objects;

/**
 * A file attached to a message. Embeds can reference it as {@code attachment://<fileName>}.
 *
 * @since 1.0.0
 */
public final class Attachment {
    private final String fileName;
    private final byte[] data;
    private final boolean spoiler;

    private Attachment(String fileName, byte[] data, boolean spoiler) {
        this.fileName = fileName;
        this.data = data;
        this.spoiler = spoiler;
    }

    /**
     * @param fileName file name with extension, for example {@code "map.png"}
     * @param data     file content; copied. The upload limit depends on the guild's boost level
     */
    public static Attachment of(String fileName, byte[] data) {
        return of(fileName, data, false);
    }

    /**
     * @param spoiler whether the file is hidden behind a spoiler
     */
    public static Attachment of(String fileName, byte[] data, boolean spoiler) {
        Objects.requireNonNull(fileName, "fileName");
        Objects.requireNonNull(data, "data");
        if (fileName.isBlank())
            throw new IllegalArgumentException("File name is blank");
        if (data.length == 0)
            throw new IllegalArgumentException("File is empty: " + fileName);
        return new Attachment(fileName.strip(), data.clone(), spoiler);
    }

    public String fileName() {
        return fileName;
    }

    /**
     * @return a copy of the file content
     */
    public byte[] data() {
        return data.clone();
    }

    public int size() {
        return data.length;
    }

    public boolean spoiler() {
        return spoiler;
    }
}

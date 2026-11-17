package com.denisnumb.discord_chat_mod.discord.chat.model;

import net.dv8tion.jda.api.utils.FileUpload;

public record DiscordAttachment(String fileName, byte[] data, boolean spoiler) {
    public DiscordAttachment(String fileName, byte[] data) {
        this(fileName, data, false);
    }

    public FileUpload toFileUpload() {
        FileUpload upload = FileUpload.fromData(data, fileName);
        return spoiler ? upload.asSpoiler() : upload;
    }
}

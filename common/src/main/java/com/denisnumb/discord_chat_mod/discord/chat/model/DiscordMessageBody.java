package com.denisnumb.discord_chat_mod.discord.chat.model;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record DiscordMessageBody(Optional<String> content, List<MessageEmbed> embeds, List<DiscordAttachment> attachments) {
    public DiscordMessageBody {
        embeds = List.copyOf(embeds);
        attachments = List.copyOf(attachments);
    }

    public DiscordMessageBody(Optional<String> content, List<MessageEmbed> embeds) {
        this(content, embeds, List.of());
    }

    public boolean hasContentAndEmbeds() {
        return content.isPresent() && !embeds.isEmpty();
    }

    public boolean hasOnlyContent() {
        return content.isPresent() && embeds.isEmpty();
    }

    public @Nullable String getContent() {
        return content.orElse(null);
    }

    public DiscordMessageBody withAdditionalEmbeds(List<MessageEmbed> extra) {
        int free = Math.max(0, Message.MAX_EMBED_COUNT - embeds.size());
        if (free == 0 || extra.isEmpty()) {
            return this;
        }

        List<MessageEmbed> merged = new ArrayList<>(embeds);
        extra.stream().limit(free).forEach(merged::add);

        return new DiscordMessageBody(content, merged, attachments);
    }

    public DiscordMessageBody withAttachment(@Nullable DiscordAttachment attachment) {
        if (attachment == null || attachments.size() >= Message.MAX_FILE_AMOUNT) {
            return this;
        }

        List<DiscordAttachment> merged = new ArrayList<>(attachments);
        merged.add(attachment);

        return new DiscordMessageBody(content, embeds, merged);
    }

    public DiscordMessageBody withAppended(DiscordMessageBody other) {
        DiscordMessageBody result = withAdditionalEmbeds(other.embeds);

        for (DiscordAttachment attachment : other.attachments)
            result = result.withAttachment(attachment);

        return result;
    }
}

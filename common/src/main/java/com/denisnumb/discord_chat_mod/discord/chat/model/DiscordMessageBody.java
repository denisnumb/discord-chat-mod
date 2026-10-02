package com.denisnumb.discord_chat_mod.discord.chat.model;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record DiscordMessageBody(Optional<String> content, List<MessageEmbed> embeds) {
    public DiscordMessageBody {
        embeds = List.copyOf(embeds);
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

        return new DiscordMessageBody(content, merged);
    }
}
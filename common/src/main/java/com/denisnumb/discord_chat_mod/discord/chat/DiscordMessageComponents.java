package com.denisnumb.discord_chat_mod.discord.chat;

import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record DiscordMessageComponents(Optional<String> content, Optional<MessageEmbed> embed) {
    public boolean hasContentAndEmbed(){
        return content.isPresent() && embed.isPresent();
    }

    public boolean hasOnlyContent(){
        return content.isPresent() && embed.isEmpty();
    }

    public @Nullable String getContent(){
        return content.orElse(null);
    }

    public @Nullable MessageEmbed getEmbed(){
        return embed.orElse(null);
    }
}
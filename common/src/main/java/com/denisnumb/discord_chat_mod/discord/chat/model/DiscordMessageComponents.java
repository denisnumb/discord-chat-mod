package com.denisnumb.discord_chat_mod.discord.chat.model;

import org.jetbrains.annotations.Nullable;

public record DiscordMessageComponents(DiscordMessageBody webhook, DiscordMessageBody regular) {
    public DiscordMessageComponents withAttachment(@Nullable DiscordAttachment attachment) {
        return new DiscordMessageComponents(webhook.withAttachment(attachment), regular.withAttachment(attachment));
    }

    public DiscordMessageComponents withAppended(DiscordMessageBody body) {
        return new DiscordMessageComponents(webhook.withAppended(body), regular.withAppended(body));
    }
}

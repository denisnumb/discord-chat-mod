package com.denisnumb.discord_chat_mod.linking.model;

import java.util.UUID;

public record AccountLink(
        UUID uuid,
        String name,
        String discordId,
        long linkedAt
) {
    public AccountLink withName(String newName) {
        return new AccountLink(uuid, newName, discordId, linkedAt);
    }
}

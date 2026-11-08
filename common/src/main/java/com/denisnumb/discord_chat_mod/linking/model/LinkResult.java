package com.denisnumb.discord_chat_mod.linking.model;

import org.jetbrains.annotations.Nullable;

public record LinkResult(Status status, @Nullable AccountLink link) {
    public enum Status {
        LINKED,
        ALREADY_LINKED,
        /**
         * The Discord user has reached maxMinecraftAccountsPerDiscordUser
         */
        LIMIT_REACHED,
        /**
         * Storage is not loaded (no world is running)
         */
        UNAVAILABLE
    }

    public boolean isSuccess() {
        return status == Status.LINKED || status == Status.ALREADY_LINKED;
    }

    public static LinkResult of(Status status) {
        return new LinkResult(status, null);
    }
}

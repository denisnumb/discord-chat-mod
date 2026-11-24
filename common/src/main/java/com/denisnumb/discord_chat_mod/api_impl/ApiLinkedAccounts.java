package com.denisnumb.discord_chat_mod.api_impl;

import com.denisnumb.discord_chat_mod.api.LinkListener;
import com.denisnumb.discord_chat_mod.api.LinkedAccount;
import com.denisnumb.discord_chat_mod.api.LinkedAccounts;
import com.denisnumb.discord_chat_mod.api.Registration;
import com.denisnumb.discord_chat_mod.api.UnlinkReason;
import com.denisnumb.discord_chat_mod.linking.AccountLinkListener;
import com.denisnumb.discord_chat_mod.linking.AccountLinkService;
import com.denisnumb.discord_chat_mod.linking.AccountLinking;
import com.denisnumb.discord_chat_mod.linking.model.AccountLink;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

final class ApiLinkedAccounts implements LinkedAccounts {
    private final String modId;

    ApiLinkedAccounts(String modId) {
        this.modId = modId;
    }

    @Override
    public boolean isEnabled() {
        return AccountLinking.isEnabled() && AccountLinkService.isLoaded();
    }

    @Override
    public Optional<LinkedAccount> getByPlayer(UUID player) {
        Objects.requireNonNull(player, "player");
        if (!isEnabled())
            return Optional.empty();
        return AccountLinkService.getByUuid(player).map(ApiLinkedAccounts::toApi);
    }

    @Override
    public List<LinkedAccount> getByDiscordId(String discordUserId) {
        Objects.requireNonNull(discordUserId, "discordUserId");
        if (!isEnabled())
            return List.of();
        return AccountLinkService.getByDiscordId(discordUserId).stream().map(ApiLinkedAccounts::toApi).toList();
    }

    @Override
    public Registration addListener(LinkListener listener) {
        Objects.requireNonNull(listener, "listener");

        AccountLinkListener adapter = new ModLinkListener(modId, listener);
        AccountLinkService.addListener(adapter);

        AtomicBoolean registered = new AtomicBoolean(true);
        return () -> {
            if (registered.compareAndSet(true, false))
                AccountLinkService.removeListener(adapter);
        };
    }

    static LinkedAccount toApi(AccountLink link) {
        return LinkedAccount.of(link.uuid(), link.name(), link.discordId(), Instant.ofEpochMilli(link.linkedAt()));
    }

    static UnlinkReason toApi(com.denisnumb.discord_chat_mod.linking.model.UnlinkReason reason) {
        return switch (reason) {
            case PLAYER -> UnlinkReason.PLAYER;
            case DISCORD_USER -> UnlinkReason.DISCORD_USER;
            case ADMIN -> UnlinkReason.ADMIN;
            case GUILD_LEFT -> UnlinkReason.GUILD_LEFT;
            case REPLACED -> UnlinkReason.REPLACED;
            //noinspection UnnecessaryDefault - keeps the API stable when new internal reasons are added
            default -> UnlinkReason.OTHER;
        };
    }

    private record ModLinkListener(String modId, LinkListener listener) implements AccountLinkListener {
        @Override
        public void onLinked(AccountLink link) {
            try {
                listener.onLinked(toApi(link));
            } catch (RuntimeException e) {
                throw failure(e);
            }
        }

        @Override
        public void onUnlinked(AccountLink link, com.denisnumb.discord_chat_mod.linking.model.UnlinkReason reason) {
            try {
                listener.onUnlinked(toApi(link), toApi(reason));
            } catch (RuntimeException e) {
                throw failure(e);
            }
        }

        private RuntimeException failure(RuntimeException cause) {
            return new RuntimeException("Link listener of mod \"" + modId + "\" failed: " + cause.getMessage(), cause);
        }
    }
}

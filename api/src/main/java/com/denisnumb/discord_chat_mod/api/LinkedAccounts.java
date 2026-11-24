package com.denisnumb.discord_chat_mod.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Minecraft ↔ Discord account linking ({@code [accountLinking]} in the config). All queries are answered from local
 * storage without contacting Discord, so they are cheap and work while the bot is disconnected.
 *
 * @since 1.0.0
 */
public interface LinkedAccounts {
    /**
     * @return {@code true} if account linking is enabled by the administrator and the link storage is loaded
     * (a world is running)
     */
    boolean isEnabled();

    /**
     * @return the link of a Minecraft account, if linked
     */
    Optional<LinkedAccount> getByPlayer(UUID player);

    /**
     * @return all Minecraft accounts linked to the Discord user (one user may link several accounts)
     */
    List<LinkedAccount> getByDiscordId(String discordUserId);

    /**
     * Subscribes to link changes. The listener is called from the thread that changed the link (a Discord thread or
     * the server thread).
     */
    Registration addListener(LinkListener listener);
}

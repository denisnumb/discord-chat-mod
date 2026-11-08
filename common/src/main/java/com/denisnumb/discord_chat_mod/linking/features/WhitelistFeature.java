package com.denisnumb.discord_chat_mod.linking.features;

import com.denisnumb.discord_chat_mod.config.configs.AccountLinkingConfig;
import com.denisnumb.discord_chat_mod.linking.AccountLinkListener;
import com.denisnumb.discord_chat_mod.linking.AccountLinking;
import com.denisnumb.discord_chat_mod.linking.model.AccountLink;
import com.denisnumb.discord_chat_mod.linking.model.UnlinkReason;
import com.denisnumb.discord_chat_mod.locale.MinecraftLocaleProvider;
import com.denisnumb.discord_chat_mod.utils.MojangApiUtils;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.server;
import static com.denisnumb.discord_chat_mod.utils.MinecraftUtils.logErrorToServer;
import static com.denisnumb.discord_chat_mod.utils.MinecraftUtils.logWarnToServer;


public final class WhitelistFeature implements AccountLinkListener {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static boolean isActive() {
        return AccountLinking.isEnabled() && AccountLinkingConfig.WHITELIST_LINKED_PLAYERS.get();
    }

    @Override
    public void onLinked(AccountLink link) {
        if (isActive())
            runWhitelistCommand("add", link.name());
    }

    @Override
    public void onUnlinked(AccountLink link, UnlinkReason reason) {
        // REPLACED: the Minecraft account is still linked (to another Discord user)
        if (reason == UnlinkReason.REPLACED)
            return;

        if (isActive())
            runWhitelistCommand("remove", link.name());
    }

    private static void runWhitelistCommand(String action, String playerName) {
        MinecraftServer srv = server;
        if (srv == null)
            return;

        if (!MojangApiUtils.isValidPlayerName(playerName)) {
            logWarnToServer(MinecraftLocaleProvider.Linking.Error.invalidPlayerName(playerName));
            return;
        }

        srv.execute(() -> {
            try {
                srv.getCommands().performPrefixedCommand(
                        srv.createCommandSourceStack().withSuppressedOutput(),
                        "whitelist " + action + " " + playerName
                );
                LOGGER.info("[AccountLinking] whitelist {} {}", action, playerName);
            } catch (Exception e) {
                logErrorToServer(MinecraftLocaleProvider.Linking.Error.whitelistCommand(action, playerName, String.valueOf(e.getMessage())));
                LOGGER.error("", e);
            }
        });
    }
}

package com.denisnumb.discord_chat_mod.linking.features;

import com.denisnumb.discord_chat_mod.config.configs.AccountLinkingConfig;
import com.denisnumb.discord_chat_mod.linking.AccountLinkService;
import com.denisnumb.discord_chat_mod.linking.AccountLinking;
import com.denisnumb.discord_chat_mod.linking.LinkCodeManager;
import com.denisnumb.discord_chat_mod.linking.discord.LinkingDiscordUtils;
import com.denisnumb.discord_chat_mod.locale.MinecraftLocaleProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.server;


public final class JoinGate {
    private JoinGate() {}

    private static final String NOT_WHITELISTED_KEY = "multiplayer.disconnect.not_whitelisted";

    public static Optional<Component> checkLogin(UUID uuid, String playerName, @Nullable Component vanillaReason) {
        MinecraftServer srv = server;
        if (srv == null || !srv.isDedicatedServer() || !AccountLinking.isEnabled() || !AccountLinkService.isLoaded())
            return Optional.empty();
        if (vanillaReason == null || !AccountLinkingConfig.WHITELIST_LINKED_PLAYERS.get() || !isNotWhitelistedReason(vanillaReason))
            return Optional.empty();

        // A linked player who is not whitelisted was removed from the whitelist manually: keep the vanilla message
        if (AccountLinkService.getByUuid(uuid).isPresent())
            return Optional.empty();

        return Optional.of(buildLinkRequiredScreen(uuid, playerName));
    }

    private static boolean isNotWhitelistedReason(Component reason) {
        return reason.getContents() instanceof TranslatableContents translatable
                && NOT_WHITELISTED_KEY.equals(translatable.getKey());
    }

    private static Component buildLinkRequiredScreen(UUID uuid, String playerName) {
        LinkCodeManager.PendingCode code = AccountLinkService.getOrCreateCode(uuid, playerName);
        String guildName = LinkingDiscordUtils.getGuildNames();
        long minutesLeft = Math.max(1, (code.secondsLeft(System.currentTimeMillis()) + 59) / 60);

        return Component.empty()
                .append(MinecraftLocaleProvider.Linking.JoinGate.title().withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                .append("\n\n")
                .append(MinecraftLocaleProvider.Linking.JoinGate.instruction(guildName))
                .append("\n\n")
                .append(Component.literal("/link code:" + code.code()).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .append("\n\n")
                .append(MinecraftLocaleProvider.Linking.Code.expires(minutesLeft).withStyle(ChatFormatting.GRAY))
                .append("\n")
                .append(MinecraftLocaleProvider.Linking.JoinGate.reconnect().withStyle(ChatFormatting.GRAY));
    }
}

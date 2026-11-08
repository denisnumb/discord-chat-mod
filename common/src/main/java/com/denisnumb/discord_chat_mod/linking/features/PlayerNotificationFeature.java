package com.denisnumb.discord_chat_mod.linking.features;

import com.denisnumb.discord_chat_mod.linking.AccountLinkListener;
import com.denisnumb.discord_chat_mod.linking.discord.LinkingDiscordUtils;
import com.denisnumb.discord_chat_mod.linking.model.AccountLink;
import com.denisnumb.discord_chat_mod.linking.model.UnlinkReason;
import com.denisnumb.discord_chat_mod.locale.MinecraftLocaleProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.server;

public final class PlayerNotificationFeature implements AccountLinkListener {
    @Override
    public void onLinked(AccountLink link) {
        notifyPlayer(link.uuid(), MinecraftLocaleProvider.Linking.Notification.linked(LinkingDiscordUtils.describeUser(link.discordId()))
                .withStyle(ChatFormatting.GREEN));
    }

    @Override
    public void onUnlinked(AccountLink link, UnlinkReason reason) {
        if (reason == UnlinkReason.PLAYER || reason == UnlinkReason.REPLACED)
            return;

        notifyPlayer(link.uuid(), MinecraftLocaleProvider.Linking.Notification.unlinked(LinkingDiscordUtils.describeUser(link.discordId()))
                .withStyle(ChatFormatting.YELLOW));
    }

    private static void notifyPlayer(UUID uuid, Component message) {
        MinecraftServer srv = server;
        if (srv == null)
            return;

        srv.execute(() -> {
            ServerPlayer player = srv.getPlayerList().getPlayer(uuid);
            if (player != null)
                player.sendSystemMessage(message);
        });
    }
}

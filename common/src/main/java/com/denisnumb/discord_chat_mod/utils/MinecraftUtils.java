package com.denisnumb.discord_chat_mod.utils;

import com.denisnumb.discord_chat_mod.config.configs.LogsConfig;
import com.mojang.brigadier.StringReader;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.denisnumb.discord_chat_mod.compat.VanishCompatProvider;

import java.util.*;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.server;
import static com.denisnumb.discord_chat_mod.chat.MinecraftMessageSender.sendSystemMessageToPlayersBySelector;

public final class MinecraftUtils {
    private MinecraftUtils() {}

    private static final Logger LOGGER = LogUtils.getLogger();

    public static List<ServerPlayer> getPlayerListBySelector(String selector){
        try {
            CommandSourceStack fakeSource = server.createCommandSourceStack()
                    .withSuppressedOutput()
                    .withPermission(LevelBasedPermissionSet.ADMIN);
            EntitySelectorParser parser = new EntitySelectorParser(new StringReader(selector), true);

            return parser.parse().findPlayers(fakeSource);
        } catch (Exception e){
            return List.of();
        }
    }

    public static void showTitleBarMessage(Component message) {
        Minecraft.getInstance().gui.setOverlayMessage(message, false);
    }

    public static void logErrorToServer(Component message) {
        LOGGER.error(message.getString());
        if (LogsConfig.LOG_DISCORD_ERRORS_TO_SERVER_CHAT.get())
            sendSystemMessageToPlayersBySelector(buildLogMessageComponent(message, ChatFormatting.RED.getColor()), LogsConfig.DISCORD_ERRORS_CHAT_PLAYER_SELECTOR.get());
    }

    public static void logWarnToServer(Component message) {
        LOGGER.warn(message.getString());
        if (LogsConfig.LOG_DISCORD_ERRORS_TO_SERVER_CHAT.get())
            sendSystemMessageToPlayersBySelector(buildLogMessageComponent(message, ChatFormatting.YELLOW.getColor()), LogsConfig.DISCORD_ERRORS_CHAT_PLAYER_SELECTOR.get());
    }

    public static int getServerPlayerCount(@Nullable MinecraftServer server) {
        return VanishCompatProvider.get().getVisiblePlayerCount(server);
    }

    public static int getServerMaxPlayers(@Nullable MinecraftServer server) {
        if (server != null && server.getPlayerList() != null)
            return server.getMaxPlayers();
        return 20;
    }

    public static String[] getServerPlayerNames(@Nullable MinecraftServer server) {
        if (server != null && server.getPlayerList() != null)
            return VanishCompatProvider.get().filterVanishedPlayers(server, server.getPlayerNames());
        return new String[0];
    }

    private static Component buildLogMessageComponent(Component message, int color) {
        return Component.empty()
                .append(Component.literal("[discord_chat_mod] ")
                        .withStyle(style -> style.withBold(true))
                )
                .append(message).withColor(color);
    }
}

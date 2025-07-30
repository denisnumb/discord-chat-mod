package com.denisnumb.discord_chat_mod;

import com.denisnumb.discord_chat_mod.config.PlatformConfig;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import static com.denisnumb.discord_chat_mod.ColorUtils.Color.RED;
import static com.denisnumb.discord_chat_mod.ColorUtils.Color.YELLOW;
import static com.denisnumb.discord_chat_mod.DiscordChatMod.server;

public class MinecraftUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void sendMessageToPlayersBySelector(Component message, String selector) {
        CommandSourceStack fakeSource = server.createCommandSourceStack()
                .withSuppressedOutput()
                .withPermission(4);

        try {
            EntitySelectorParser parser = new EntitySelectorParser(new StringReader(selector), true);
            EntitySelector entitySelector = parser.parse();

            for (ServerPlayer player : entitySelector.findPlayers(fakeSource))
                player.sendSystemMessage(ComponentUtils.updateForEntity(null, message, player, 0), false);
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        } catch (Exception ignored){}
    }

    public static void sendMessageToAllPlayers(Component message){
        sendMessageToPlayersBySelector(message, "@a");
    }

    public static void showTitleBarMessage(Component message){
        Minecraft.getInstance().gui.setOverlayMessage(message, false);
    }

    public static void logErrorToServer(String message) {
        LOGGER.error(message);
        if (PlatformConfig.getConfig().isLoggingDiscordErrorsToServerChatEnabled())
            sendMessageToPlayersBySelector(buildLogMessageComponent(message, RED), PlatformConfig.getConfig().discordErrorsChatPlayerSelector());
    }

    public static void logWarnToServer(String message) {
        LOGGER.warn(message);
        if (PlatformConfig.getConfig().isLoggingDiscordErrorsToServerChatEnabled())
            sendMessageToPlayersBySelector(buildLogMessageComponent(message, YELLOW), PlatformConfig.getConfig().discordErrorsChatPlayerSelector());
    }

    public static int getServerPlayerCount(@Nullable MinecraftServer server){
        if (server != null && server.getPlayerList() != null)
            return server.getPlayerCount();
        return 0;
    }

    public static int getServerMaxPlayers(@Nullable MinecraftServer server){
        if (server != null && server.getPlayerList() != null)
            return server.getMaxPlayers();
        return 20;
    }

    public static String[] getServerPlayerNames(@Nullable MinecraftServer server){
        if (server != null && server.getPlayerList() != null)
            return server.getPlayerNames();
        return new String[0];
    }

    private static Component buildLogMessageComponent(String message, int color) {
        return Component.empty()
                .append(Component.literal("[discord_chat_mod] ")
                        .withStyle(style -> style.withBold(true))
                )
                .append(Component.literal(message)).withColor(color);
    }
}

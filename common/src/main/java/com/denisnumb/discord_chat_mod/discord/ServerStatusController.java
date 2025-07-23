package com.denisnumb.discord_chat_mod.discord;

import com.denisnumb.discord_chat_mod.config.PlatformConfig;
import com.mojang.logging.LogUtils;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.denisnumb.discord_chat_mod.ColorUtils.Color.*;
import static com.denisnumb.discord_chat_mod.DiscordChatMod.*;
import static com.denisnumb.discord_chat_mod.discord.DiscordChannelRegistry.pinnedStatusMessageChannel;
import static com.denisnumb.discord_chat_mod.MinecraftUtils.getTranslate;
import static com.denisnumb.discord_chat_mod.ModLanguageKey.*;
import static com.denisnumb.discord_chat_mod.discord.DiscordUtils.*;

public class ServerStatusController {
    @Nullable
    private static Message serverStatusMessage;
    @Nullable
    private static ScheduledExecutorService scheduler;
    private static long lastInvocationTime = 0;
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void updateServerStatusWithDelay() {
        if (!isDiscordConnected() || scheduler == null)
            return;
        lastInvocationTime = System.currentTimeMillis();
        scheduler.schedule(() -> {
            if (System.currentTimeMillis() - lastInvocationTime >= 10000) {
                updateServerStatus();
            }
        }, 10, TimeUnit.SECONDS);
    }

    public static void initServerStatusController(){
        scheduler = Executors.newSingleThreadScheduledExecutor();
        Optional<Message> existingStatusMessage = findPinnedStatusMessage();
        serverStatusMessage = existingStatusMessage.orElseGet(()
                -> sendServerStatusMessageComplete(pinnedStatusMessageChannel, createServerStatusMessageEmbed()).orElse(null));

        if (serverStatusMessage != null && existingStatusMessage.isEmpty() && !serverStatusMessage.isPinned()){
            try{
                serverStatusMessage.pin().queue();
            } catch (Exception e){
                LOGGER.error(e.getMessage());
            }
        }

        updateServerStatusWithDelay();
    }

    public static void updateServerStatusMessageToUnavailable(){
        if (scheduler != null)
            scheduler.close();
        if (!isDiscordConnected() || !PlatformConfig.getConfig().isPinnedStatusMessageEnabled() || serverStatusMessage == null)
            return;
        editMessageEmbeds(serverStatusMessage, buildEmbed(getTranslate(SERVER_UNAVAILABLE), RED));
    }

    private static MessageEmbed createServerStatusMessageEmbed(){
        return server.getPlayerCount() == 0
                ? buildEmbed(getTranslate(SERVER_AVAILABLE), DARK_GREEN)
                : buildEmbed(getOnlineCountString(), String.join("\n", server.getPlayerNames()), GREEN);
    }

    private static void updateServerStatus(){
        if (PlatformConfig.getConfig().isPinnedStatusMessageEnabled() && serverStatusMessage != null)
            editMessageEmbeds(serverStatusMessage, createServerStatusMessageEmbed());
        jda.getPresence().setActivity(Activity.customStatus(getOnlineCountString()));
    }

    private static String getOnlineCountString(){
        return String.format(
                getTranslate(ONLINE_PLAYERS),
                server.getPlayerCount(),
                server.getMaxPlayers()
        );
    }

    private static Optional<Message> findPinnedStatusMessage(){
        try {
            return pinnedStatusMessageChannel.retrievePinnedMessages()
                    .complete()
                    .stream()
                    .filter(message -> message.getAuthor().getId().equals(jda.getSelfUser().getId()))
                    .findFirst();
        } catch (Exception e){
            LOGGER.error(e.getMessage());
            return Optional.empty();
        }
    }
}

package com.denisnumb.discord_chat_mod.discord.utils;

import com.denisnumb.discord_chat_mod.config.ConfigProvider;
import com.denisnumb.discord_chat_mod.discord.data_providers.StickersProvider;
import com.denisnumb.discord_chat_mod.discord.model.ChannelCategory;
import com.denisnumb.discord_chat_mod.discord.model.DiscordGuildContext;
import com.denisnumb.discord_chat_mod.locale.MinecraftLocaleProvider;
import com.denisnumb.discord_chat_mod.utils.JavaUtils;
import com.denisnumb.discord_chat_mod.utils.MinecraftUtils;
import com.mojang.logging.LogUtils;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.utils.FileUpload;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.*;
import static com.denisnumb.discord_chat_mod.utils.MinecraftUtils.logErrorToServer;
import static com.denisnumb.discord_chat_mod.discord.DiscordChannelRegistry.*;
import static com.denisnumb.discord_chat_mod.discord.chat_style.DiscordChatStyleProvider.*;


public final class DiscordMessageUtils {
    private DiscordMessageUtils() {
    }

    private static final Logger LOGGER = LogUtils.getLogger();
    private static ExecutorService EXECUTOR;

    public static void initDiscordSendExecutor() {
        EXECUTOR = Executors.newFixedThreadPool(5);
    }

    public static void stopDiscordSendExecutor() {
        if (EXECUTOR != null) {
            EXECUTOR.shutdownNow();
            EXECUTOR = null;
        }
    }

    public static void handleDiscord(Runnable prepareMessageFunc) {
        if (isDiscordConnected() && EXECUTOR != null)
            EXECUTOR.submit(prepareMessageFunc);
    }

    @Nullable
    public static FileUpload downloadStickerFile(StickersProvider.StickerData stickerData) {
        try {
            return FileUpload.fromData(
                    JavaUtils.getInputStreamFromUrl(stickerData.imageUrl()).readAllBytes(),
                    getStickerFileName(stickerData.imageUrl())
            );
        } catch (Exception ignored) {
            return null;
        }
    }

    public static void sendMessageFromServer(
            ChannelCategory channelCategory,
            List<DiscordGuildContext> guildContexts,
            DiscordMessageComponents messageComponents
    ) {
        sendMessageToGuilds(channelCategory, guildContexts, null, messageComponents, messageComponents, null, null);
    }

    public static void sendMessageFromServer(
            ChannelCategory channelCategory,
            List<DiscordGuildContext> guildContexts,
            DiscordMessageComponents messageComponents,
            FileUpload imageData
    ) {
        sendMessageToGuilds(channelCategory, guildContexts, null, messageComponents, messageComponents, null, imageData);
    }

    public static Optional<String> sendMessageFromPlayer(
            ChannelCategory channelCategory,
            List<DiscordGuildContext> guildContexts,
            Player player,
            DiscordMessageComponents messageComponentsWebhook,
            DiscordMessageComponents messageComponents,
            FileUpload imageData
    ) {
        Optional<String> optionalImageUrl = Optional.empty();

        for (DiscordGuildContext guildContext : guildContexts) {
            GuildMessageChannel channel = guildContext.getChannel(channelCategory);

            if (isChannelCategoryDisabled(channel))
                continue;

            Optional<String> optionalNewUrl = sendImageFromPlayer(guildContext, channel, player, messageComponentsWebhook, messageComponents, imageData);
            if (optionalImageUrl.isEmpty())
                optionalImageUrl = optionalNewUrl;

            duplicateMessageToDefaultChannel(guildContext, channelCategory,
                    () -> sendImageFromPlayer(guildContext, guildContext.defaultChannel, player, messageComponentsWebhook, messageComponents, imageData)
            );
        }

        return optionalImageUrl;
    }

    public static void sendMessageFromPlayer(
            ChannelCategory channelCategory,
            List<DiscordGuildContext> guildContexts,
            Player player,
            DiscordMessageComponents messageComponentsWebhook,
            DiscordMessageComponents messageComponents
    ) {
        sendMessageToGuilds(channelCategory, guildContexts, player, messageComponentsWebhook, messageComponents, null, null);
    }

    public static void sendMessageFromPlayer(
            ChannelCategory channelCategory,
            List<DiscordGuildContext> guildContexts,
            Player player,
            DiscordMessageComponents messageComponentsWebhook,
            DiscordMessageComponents messageComponents,
            StickersProvider.StickerData stickerData
    ) {
        sendMessageToGuilds(channelCategory, guildContexts, player, messageComponentsWebhook, messageComponents, stickerData, null);
    }

    public static Optional<Message> sendWebhookMessage(
            GuildMessageChannel channel,
            Webhook webhook,
            boolean complete,
            @Nullable String avatarUrl,
            String userName,
            DiscordMessageComponents messageComponents,
            @Nullable StickersProvider.StickerData stickerData,
            @Nullable FileUpload... files
    ) {
        if (!isDiscordConnected())
            return Optional.empty();
        try {
            return new DiscordMessageSender.WebhookBuilder(webhook)
                    .withThread(channel)
                    .withAvatarUrl(avatarUrl)
                    .withUserName(userName)
                    .withContent(messageComponents.getContent())
                    .withEmbeds(messageComponents.getEmbed())
                    .withSticker(stickerData)
                    .withFiles(files)
                    .send(complete);
        } catch (Exception e) {
            LOGGER.error("SendWebhookError", e);
        }

        return Optional.empty();
    }

    public static Optional<Message> sendChannelMessage(
            GuildMessageChannel channel,
            boolean complete,
            DiscordMessageComponents messageComponents,
            @Nullable StickersProvider.StickerData stickerData,
            @Nullable FileUpload... files
    ) {
        if (!isDiscordConnected())
            return Optional.empty();
        try {
            return new DiscordMessageSender.ChannelBuilder(channel)
                    .withContent(messageComponents.getContent())
                    .withEmbeds(messageComponents.getEmbed())
                    .withSticker(stickerData)
                    .withFiles(files)
                    .send(complete);
        } catch (InsufficientPermissionException e) {
            logErrorToServer(MinecraftLocaleProvider.Discord.Error.sendMessageWithCause(channel.getName(), e.getMessage()));
            LOGGER.error("", e);
        } catch (ErrorResponseException e) {
            logErrorToServer(MinecraftLocaleProvider.Discord.Error.sendMessageWithCause(channel.getName(), e.getMeaning()));
            LOGGER.error("", e);
        } catch (Exception e) {
            logErrorToServer(MinecraftLocaleProvider.Discord.Error.sendMessage(channel.getName()));
            LOGGER.error("", e);
        }

        return Optional.empty();
    }

    public static void editMessage(Message message, DiscordMessageComponents components) {
        try {
            if (components.hasContentAndEmbed())
                message.editMessage(components.getContent()).setEmbeds(components.getEmbed()).queue();
            else if (components.hasOnlyContent())
                message.editMessage(components.getContent()).queue();
            else
                message.editMessageEmbeds(components.getEmbed()).queue();
        } catch (Exception e) {
            LOGGER.error("EditMessageError", e);
        }
    }

    private static void sendMessageToGuilds(
            ChannelCategory channelCategory,
            List<DiscordGuildContext> guildContexts,
            @Nullable Player player,
            DiscordMessageComponents messageComponentsWebhook,
            DiscordMessageComponents messageComponents,
            @Nullable StickersProvider.StickerData stickerData,
            @Nullable FileUpload imageData
    ) {
        for (DiscordGuildContext guildContext : guildContexts) {
            GuildMessageChannel channel = guildContext.getChannel(channelCategory);

            if (isChannelCategoryDisabled(channel))
                continue;

            duplicateMessageToDefaultChannel(guildContext, channelCategory,
                    () -> sendMessage(guildContext, guildContext.defaultChannel, player, messageComponentsWebhook, messageComponents, stickerData, imageData)
            );

            sendMessage(guildContext, channel, player, messageComponentsWebhook, messageComponents, stickerData, imageData);
        }
    }

    private static Optional<String> sendImageFromPlayer(
            DiscordGuildContext guildContext,
            GuildMessageChannel channel,
            Player player,
            DiscordMessageComponents messageComponentsWebhook,
            DiscordMessageComponents messageComponents,
            FileUpload imageData
    ) {
        return guildContext.getWebhook(channel)
                .map(webhook -> sendWebhookMessage(
                        channel,
                        webhook,
                        true,
                        MinecraftUtils.getPlayerAvatarUrl(player),
                        player.getDisplayName().getString(),
                        messageComponentsWebhook,
                        null,
                        imageData
                ))
                .orElseGet(() -> sendChannelMessage(
                        channel,
                        true,
                        messageComponents,
                        null,
                        imageData
                ))
                .flatMap(message -> message.getAttachments().stream()
                        .findFirst()
                        .map(Message.Attachment::getUrl)
                );
    }

    private static void sendMessage(
            DiscordGuildContext guildContext,
            GuildMessageChannel channel,
            @Nullable Player player,
            DiscordMessageComponents messageComponentsWebhook,
            DiscordMessageComponents messageComponents,
            @Nullable StickersProvider.StickerData stickerData,
            @Nullable FileUpload imageData
    ) {
        guildContext.getWebhook(channel).ifPresentOrElse(webhook -> {
                    String avatarUrl = player == null ? null : MinecraftUtils.getPlayerAvatarUrl(player);
                    String userName = player == null ? getWebhookServerName() : player.getDisplayName().getString();
                    sendWebhookMessage(channel, webhook, false, avatarUrl, userName, messageComponentsWebhook, stickerData, imageData);
                },
                () -> sendChannelMessage(channel, false, messageComponents, stickerData, imageData)
        );
    }

    private static void duplicateMessageToDefaultChannel(DiscordGuildContext context, ChannelCategory category, Runnable sendMessageFunction) {
        if (isDiscordConnected()
                && context.duplicateMessages
                && !context.defaultChannel.equals(context.getChannel(category))) {
            sendMessageFunction.run();
        }
    }

    private static String getWebhookServerName() {
        String configValue = ConfigProvider.getConfig().webhookServerName();
        return configValue.isBlank() ? null : configValue.replaceAll("(?i)discord", "DC");
    }

    private static String getStickerFileName(String stickerUrl) {
        String[] parts = stickerUrl.split("\\.");
        return "sticker." + parts[parts.length - 1];
    }
}
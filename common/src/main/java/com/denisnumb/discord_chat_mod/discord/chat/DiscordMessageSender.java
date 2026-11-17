package com.denisnumb.discord_chat_mod.discord.chat;

import com.denisnumb.discord_chat_mod.compat.vanish.VanishCompatProvider;
import com.denisnumb.discord_chat_mod.config.configs.WebhookModeConfig;
import com.denisnumb.discord_chat_mod.discord.chat.model.DiscordAttachment;
import com.denisnumb.discord_chat_mod.discord.chat.model.DiscordMessageBody;
import com.denisnumb.discord_chat_mod.discord.chat.model.DiscordMessageComponents;
import com.denisnumb.discord_chat_mod.utils.PlayerAvatarProvider;
import com.denisnumb.discord_chat_mod.discord.data_providers.StickersProvider;
import com.denisnumb.discord_chat_mod.discord.model.ChannelCategory;
import com.denisnumb.discord_chat_mod.discord.model.DiscordGuildContext;
import com.denisnumb.discord_chat_mod.discord.utils.DiscordRequestSender;
import com.denisnumb.discord_chat_mod.locale.MinecraftLocaleProvider;
import com.denisnumb.discord_chat_mod.utils.JavaUtils;
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
import java.util.stream.Stream;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.*;
import static com.denisnumb.discord_chat_mod.utils.MinecraftUtils.logErrorToServer;
import static com.denisnumb.discord_chat_mod.discord.DiscordGuildRegistry.*;


public final class DiscordMessageSender {
    private DiscordMessageSender() {}

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
            DiscordMessageBody body
    ) {
        sendMessageToGuilds(channelCategory, guildContexts, null, new DiscordMessageComponents(body, body), null);
    }

    public static Optional<String> sendMessageFromPlayerAndGetAttachmentUrl(
            ChannelCategory channelCategory,
            List<DiscordGuildContext> guildContexts,
            Player player,
            DiscordMessageComponents messageComponents
    ) {
        Optional<String> optionalImageUrl = Optional.empty();
        if (VanishCompatProvider.isVanished(player))
            return optionalImageUrl;

        for (DiscordGuildContext guildContext : guildContexts) {
            GuildMessageChannel channel = guildContext.getChannel(channelCategory);

            if (isChannelCategoryDisabled(channel))
                continue;

            Optional<String> optionalNewUrl = sendMessageAndGetAttachmentUrl(guildContext, channel, player, messageComponents);
            if (optionalImageUrl.isEmpty())
                optionalImageUrl = optionalNewUrl;

            duplicateMessageToDefaultChannel(guildContext, channelCategory,
                    () -> sendMessageAndGetAttachmentUrl(guildContext, guildContext.defaultChannel, player, messageComponents)
            );
        }

        return optionalImageUrl;
    }

    public static void sendMessageFromPlayer(
            ChannelCategory channelCategory,
            List<DiscordGuildContext> guildContexts,
            Player player,
            DiscordMessageComponents messageComponents
    ) {
        sendMessageToGuilds(channelCategory, guildContexts, player, messageComponents, null);
    }

    public static void sendMessageFromPlayer(
            ChannelCategory channelCategory,
            List<DiscordGuildContext> guildContexts,
            Player player,
            DiscordMessageComponents messageComponents,
            StickersProvider.StickerData stickerData
    ) {
        sendMessageToGuilds(channelCategory, guildContexts, player, messageComponents, stickerData);
    }

    public static Optional<Message> sendWebhookMessage(
            GuildMessageChannel channel,
            Webhook webhook,
            boolean complete,
            @Nullable String avatarUrl,
            String userName,
            DiscordMessageBody body,
            @Nullable StickersProvider.StickerData stickerData,
            @Nullable FileUpload... files
    ) {
        if (!isDiscordConnected())
            return Optional.empty();
        try {
            return new DiscordRequestSender.WebhookBuilder(webhook)
                    .withThread(channel)
                    .withAvatarUrl(avatarUrl)
                    .withUserName(userName)
                    .withContent(body.getContent())
                    .withEmbeds(body.embeds())
                    .withSticker(stickerData)
                    .withFiles(collectFiles(body, files))
                    .send(complete);
        } catch (Exception e) {
            LOGGER.error("SendWebhookError", e);
        }

        return Optional.empty();
    }

    public static Optional<Message> sendChannelMessage(
            GuildMessageChannel channel,
            boolean complete,
            DiscordMessageBody body,
            @Nullable StickersProvider.StickerData stickerData,
            @Nullable FileUpload... files
    ) {
        if (!isDiscordConnected())
            return Optional.empty();
        try {
            return new DiscordRequestSender.ChannelBuilder(channel)
                    .withContent(body.getContent())
                    .withEmbeds(body.embeds())
                    .withSticker(stickerData)
                    .withFiles(collectFiles(body, files))
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

    public static void editMessage(Message message, DiscordMessageBody body) {
        try {
            if (body.hasContentAndEmbeds())
                message.editMessage(body.getContent()).setEmbeds(body.embeds()).queue();
            else if (body.hasOnlyContent())
                message.editMessage(body.getContent()).queue();
            else
                message.editMessageEmbeds(body.embeds()).queue();
        } catch (Exception e) {
            LOGGER.error("EditMessageError", e);
        }
    }

    private static void sendMessageToGuilds(
            ChannelCategory channelCategory,
            List<DiscordGuildContext> guildContexts,
            @Nullable Player player,
            DiscordMessageComponents messageComponents,
            @Nullable StickersProvider.StickerData stickerData
    ) {
        if (VanishCompatProvider.isVanished(player))
            return;

        for (DiscordGuildContext guildContext : guildContexts) {
            GuildMessageChannel channel = guildContext.getChannel(channelCategory);

            if (isChannelCategoryDisabled(channel))
                continue;

            duplicateMessageToDefaultChannel(guildContext, channelCategory,
                    () -> sendMessage(guildContext, guildContext.defaultChannel, player, messageComponents, stickerData)
            );

            sendMessage(guildContext, channel, player, messageComponents, stickerData);
        }
    }

    private static Optional<String> sendMessageAndGetAttachmentUrl(
            DiscordGuildContext guildContext,
            GuildMessageChannel channel,
            Player player,
            DiscordMessageComponents messageComponents
    ) {
        return guildContext.getWebhook(channel)
                .map(webhook -> sendWebhookMessage(
                        channel,
                        webhook,
                        true,
                        PlayerAvatarProvider.getPlayerAvatarUrl(player),
                        player.getDisplayName().getString(),
                        messageComponents.webhook(),
                        null
                ))
                .orElseGet(() -> sendChannelMessage(
                        channel,
                        true,
                        messageComponents.regular(),
                        null
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
            DiscordMessageComponents messageComponents,
            @Nullable StickersProvider.StickerData stickerData
    ) {
        guildContext.getWebhook(channel).ifPresentOrElse(webhook -> {
                    String avatarUrl = player == null ? null : PlayerAvatarProvider.getPlayerAvatarUrl(player);
                    String userName = player == null ? getWebhookServerName() : player.getDisplayName().getString();
                    sendWebhookMessage(channel, webhook, false, avatarUrl, userName, messageComponents.webhook(), stickerData);
                },
                () -> sendChannelMessage(channel, false, messageComponents.regular(), stickerData)
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
        String configValue = WebhookModeConfig.WEBHOOK_SERVER_NAME.get();
        return configValue.isBlank() ? null : configValue.replaceAll("(?i)discord", "DC");
    }

    private static String getStickerFileName(String stickerUrl) {
        String[] parts = stickerUrl.split("\\.");
        return "sticker." + parts[parts.length - 1];
    }

    private static FileUpload[] collectFiles(DiscordMessageBody body, @Nullable FileUpload[] files) {
        Stream<FileUpload> extraFiles = files == null ? Stream.empty() : Arrays.stream(files).filter(Objects::nonNull);
        return Stream.concat(body.attachments().stream().map(DiscordAttachment::toFileUpload), extraFiles)
                .toArray(FileUpload[]::new);
    }
}
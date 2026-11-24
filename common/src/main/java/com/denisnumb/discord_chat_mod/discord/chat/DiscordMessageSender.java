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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.*;
import static com.denisnumb.discord_chat_mod.utils.MinecraftUtils.logErrorToServer;
import static com.denisnumb.discord_chat_mod.discord.DiscordGuildRegistry.*;


public final class DiscordMessageSender {
    private DiscordMessageSender() {}

    private static final Logger LOGGER = LogUtils.getLogger();
    private static ExecutorService EXECUTOR;
    private static final Set<CompletableFuture<?>> PENDING_TASKS = ConcurrentHashMap.newKeySet();

    public static final class NotConnectedException extends IllegalStateException {
        public NotConnectedException() {
            super("Discord is not connected");
        }
    }

    public record CategoryTargets(GuildMessageChannel channel, @Nullable GuildMessageChannel duplicate) {
        public List<GuildMessageChannel> all() {
            return duplicate == null ? List.of(channel) : List.of(channel, duplicate);
        }
    }

    public static void initDiscordSendExecutor() {
        EXECUTOR = Executors.newFixedThreadPool(5);
    }

    public static void stopDiscordSendExecutor() {
        if (EXECUTOR != null) {
            EXECUTOR.shutdownNow();
            EXECUTOR = null;
        }

        for (CompletableFuture<?> future : List.copyOf(PENDING_TASKS))
            future.completeExceptionally(new NotConnectedException());
    }

    public static void handleDiscord(Runnable prepareMessageFunc) {
        if (isDiscordConnected() && EXECUTOR != null)
            EXECUTOR.submit(prepareMessageFunc);
    }

    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> task) {
        ExecutorService executor = EXECUTOR;
        if (!isDiscordConnected() || executor == null)
            return CompletableFuture.failedFuture(new NotConnectedException());

        CompletableFuture<T> future = new CompletableFuture<>();
        PENDING_TASKS.add(future);
        future.whenComplete((result, error) -> PENDING_TASKS.remove(future));

        try {
            executor.execute(() -> {
                if (future.isDone())
                    return;
                try {
                    future.complete(task.get());
                } catch (Throwable e) {
                    future.completeExceptionally(e);
                }
            });
        } catch (RejectedExecutionException e) {
            future.completeExceptionally(new NotConnectedException());
        }

        return future;
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
            Optional<CategoryTargets> targets = getCategoryTargets(guildContext, channelCategory);
            if (targets.isEmpty())
                continue;

            Optional<String> optionalNewUrl = sendMessageAndGetAttachmentUrl(guildContext, targets.get().channel(), player, messageComponents);
            if (optionalImageUrl.isEmpty())
                optionalImageUrl = optionalNewUrl;

            if (targets.get().duplicate() != null)
                sendMessageAndGetAttachmentUrl(guildContext, targets.get().duplicate(), player, messageComponents);
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
            @Nullable String userName,
            DiscordMessageBody body,
            @Nullable StickersProvider.StickerData stickerData,
            @Nullable FileUpload... files
    ) {
        if (!isDiscordConnected())
            return Optional.empty();
        try {
            return webhookRequest(channel, webhook, avatarUrl, userName, body, stickerData, files).send(complete);
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
            return channelRequest(channel, body, stickerData, files).send(complete);
        } catch (InsufficientPermissionException | ErrorResponseException e) {
            logErrorToServer(MinecraftLocaleProvider.Discord.Error.sendMessageWithCause(channel.getName(), describeSendError(e)));
            LOGGER.error("", e);
        } catch (Exception e) {
            logErrorToServer(MinecraftLocaleProvider.Discord.Error.sendMessage(channel.getName()));
            LOGGER.error("", e);
        }

        return Optional.empty();
    }

    public static CompletableFuture<Message> submitMessage(
            DiscordGuildContext guildContext,
            GuildMessageChannel channel,
            @Nullable String userName,
            @Nullable String avatarUrl,
            DiscordMessageComponents messageComponents,
            Collection<Message.MentionType> allowedMentions
    ) {
        try {
            DiscordRequestSender<?> request = guildContext.getWebhook(channel)
                    .<DiscordRequestSender<?>>map(webhook -> webhookRequest(
                            channel, webhook, avatarUrl, userName, messageComponents.webhook(), null, null))
                    .orElseGet(() -> channelRequest(channel, messageComponents.regular(), null, null));

            return request.withAllowedMentions(allowedMentions).submit();
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private static DiscordRequestSender<?> webhookRequest(
            GuildMessageChannel channel,
            Webhook webhook,
            @Nullable String avatarUrl,
            @Nullable String userName,
            DiscordMessageBody body,
            @Nullable StickersProvider.StickerData stickerData,
            @Nullable FileUpload[] files
    ) {
        return new DiscordRequestSender.WebhookBuilder(webhook)
                .withThread(channel)
                .withAvatarUrl(avatarUrl)
                .withUserName(userName)
                .withContent(body.getContent())
                .withEmbeds(body.embeds())
                .withSticker(stickerData)
                .withFiles(collectFiles(body, files));
    }

    private static DiscordRequestSender<?> channelRequest(
            GuildMessageChannel channel,
            DiscordMessageBody body,
            @Nullable StickersProvider.StickerData stickerData,
            @Nullable FileUpload[] files
    ) {
        return new DiscordRequestSender.ChannelBuilder(channel)
                .withContent(body.getContent())
                .withEmbeds(body.embeds())
                .withSticker(stickerData)
                .withFiles(collectFiles(body, files));
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
            Optional<CategoryTargets> targets = getCategoryTargets(guildContext, channelCategory);
            if (targets.isEmpty())
                continue;

            if (targets.get().duplicate() != null)
                sendMessage(guildContext, targets.get().duplicate(), player, messageComponents, stickerData);

            sendMessage(guildContext, targets.get().channel(), player, messageComponents, stickerData);
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

    public static Optional<CategoryTargets> getCategoryTargets(DiscordGuildContext guildContext, ChannelCategory category) {
        GuildMessageChannel channel = guildContext.getChannel(category);
        if (isChannelCategoryDisabled(channel))
            return Optional.empty();

        boolean duplicate = isDiscordConnected()
                && guildContext.duplicateMessages
                && !guildContext.defaultChannel.equals(channel);

        return Optional.of(new CategoryTargets(channel, duplicate ? guildContext.defaultChannel : null));
    }

    public static String describeSendError(Throwable error) {
        Throwable cause = error;
        while ((cause instanceof CompletionException || cause instanceof ExecutionException) && cause.getCause() != null)
            cause = cause.getCause();

        if (cause instanceof ErrorResponseException response)
            return response.getMeaning();
        return cause.getMessage() != null ? cause.getMessage() : cause.getClass().getSimpleName();
    }

    public static @Nullable String getWebhookServerName() {
        String configValue = WebhookModeConfig.WEBHOOK_SERVER_NAME.get();
        return configValue.isBlank() ? null : configValue;
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
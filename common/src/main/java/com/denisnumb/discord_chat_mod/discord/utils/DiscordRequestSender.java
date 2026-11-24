package com.denisnumb.discord_chat_mod.discord.utils;

import com.denisnumb.discord_chat_mod.discord.data_providers.StickersProvider;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.sticker.GuildSticker;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateRequest;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static com.denisnumb.discord_chat_mod.discord.chat.DiscordMessageSender.downloadStickerFile;


public abstract class DiscordRequestSender<T extends MessageCreateRequest<T> & RestAction<Message>> {
    protected T request;
    private @Nullable EnumSet<Message.MentionType> allowedMentions;

    private final Function<String, T> contentStarter;
    private final Function<List<MessageEmbed>, T> embedsStarter;
    private final Function<FileUpload[], T> filesStarter;

    protected DiscordRequestSender(
            Function<String, T> contentStarter,
            Function<List<MessageEmbed>, T> embedsStarter,
            Function<FileUpload[], T> filesStarter
    ) {
        this.contentStarter = contentStarter;
        this.embedsStarter = embedsStarter;
        this.filesStarter = filesStarter;
    }

    public final DiscordRequestSender<T> withContent(@Nullable String content) {
        if (content != null) {
            if (request != null) {
                request.setContent(content);
            } else {
                request = contentStarter.apply(content);
            }
        }
        return this;
    }

    public final DiscordRequestSender<T> withEmbeds(List<MessageEmbed> embeds) {
        if (!embeds.isEmpty() && embeds.stream().allMatch(Objects::nonNull)) {
            if (request != null) {
                request.addEmbeds(embeds);
            } else {
                request = embedsStarter.apply(embeds);
            }
        }
        return this;
    }

    public final DiscordRequestSender<T> withFiles(FileUpload... files) {
        if (files != null && files.length > 0 && Arrays.stream(files).allMatch(Objects::nonNull)) {
            if (request != null) {
                request.addFiles(files);
            } else {
                request = filesStarter.apply(files);
            }
        }
        return this;
    }

    public final DiscordRequestSender<T> withAllowedMentions(@Nullable Collection<Message.MentionType> allowedMentions) {
        this.allowedMentions = allowedMentions == null
                ? null
                : allowedMentions.isEmpty() ? EnumSet.noneOf(Message.MentionType.class) : EnumSet.copyOf(allowedMentions);
        return this;
    }

    public abstract DiscordRequestSender<T> withSticker(@Nullable StickersProvider.StickerData stickerData);

    public abstract void beforeSend();

    private void prepareRequest() {
        beforeSend();

        if (request != null && allowedMentions != null)
            request.setAllowedMentions(allowedMentions);
    }

    public final Optional<Message> send(boolean complete) {
        prepareRequest();

        if (complete)
            return Optional.of(request.complete());

        request.queue();
        return Optional.empty();
    }

    public final CompletableFuture<Message> submit() {
        prepareRequest();

        if (request == null)
            return CompletableFuture.failedFuture(new IllegalStateException("Message is empty"));

        return request.submit();
    }

    public static class WebhookBuilder extends DiscordRequestSender<WebhookMessageCreateAction<Message>> {
        private static final int MAX_USER_NAME_LENGTH = 80;

        private String avatarUrl;
        private String userName;
        private ThreadChannel thread;

        public WebhookBuilder(Webhook webhook) {
            super(
                    webhook::sendMessage,
                    webhook::sendMessageEmbeds,
                    webhook::sendFiles
            );
        }

        @Override
        public WebhookBuilder withSticker(StickersProvider.@Nullable StickerData stickerData) {
            if (stickerData != null) {
                withFiles(downloadStickerFile(stickerData));
            }

            return this;
        }

        @Override
        public void beforeSend() {
            if (request != null){
                request.setAvatarUrl(avatarUrl);
                request.setUsername(userName);
                request.setThread(thread);
            }
        }

        public WebhookBuilder withAvatarUrl(@Nullable String avatarUrl){
            this.avatarUrl = avatarUrl;
            return this;
        }

        public WebhookBuilder withUserName(@Nullable String userName) {
            this.userName = sanitizeUserName(userName);
            return this;
        }

        private static @Nullable String sanitizeUserName(@Nullable String userName) {
            if (userName == null)
                return null;

            String result = userName
                    .replaceAll("(?i)discord", "DC")
                    .replaceAll("(?i)clyde", "C1yde")
                    .strip();

            if (result.length() > MAX_USER_NAME_LENGTH)
                result = result.substring(0, MAX_USER_NAME_LENGTH);

            return result.isEmpty() ? null : result;
        }

        public WebhookBuilder withThread(GuildMessageChannel channel) {
            if (channel instanceof ThreadChannel threadChannel){
                this.thread = threadChannel;
            }

            return this;
        }
    }

    public static class ChannelBuilder extends DiscordRequestSender<MessageCreateAction> {
        private final GuildMessageChannel channel;

        public ChannelBuilder(GuildMessageChannel channel) {
            super(
                    channel::sendMessage,
                    channel::sendMessageEmbeds,
                    channel::sendFiles
            );
            this.channel = channel;
        }

        @Override
        public ChannelBuilder withSticker(StickersProvider.@Nullable StickerData stickerData) {
            if (stickerData == null)
                return this;

            GuildSticker sticker = channel.getGuild().getStickerById(stickerData.discordId());
            if (sticker != null) {
                if (request != null) {
                    request.setStickers(sticker);
                } else {
                    request = channel.sendStickers(sticker);
                }
            } else {
                withFiles(downloadStickerFile(stickerData));
            }

            return this;
        }

        @Override
        public void beforeSend() {

        }
    }
}
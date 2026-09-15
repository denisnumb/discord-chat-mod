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
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static com.denisnumb.discord_chat_mod.discord.utils.DiscordMessageUtils.downloadStickerFile;


public abstract class DiscordMessageSender<T extends MessageCreateRequest<T> & RestAction<Message>> {
    protected T request;

    private final Function<String, T> contentStarter;
    private final Function<MessageEmbed[], T> embedsStarter;
    private final Function<FileUpload[], T> filesStarter;

    protected DiscordMessageSender(
            Function<String, T> contentStarter,
            Function<MessageEmbed[], T> embedsStarter,
            Function<FileUpload[], T> filesStarter
    ) {
        this.contentStarter = contentStarter;
        this.embedsStarter = embedsStarter;
        this.filesStarter = filesStarter;
    }

    public final DiscordMessageSender<T> withContent(@Nullable String content) {
        if (content != null) {
            if (request != null) {
                request.setContent(content);
            } else {
                request = contentStarter.apply(content);
            }
        }
        return this;
    }

    public final DiscordMessageSender<T> withEmbeds(MessageEmbed... embeds) {
        if (Arrays.stream(embeds).allMatch(Objects::nonNull)) {
            if (request != null) {
                request.addEmbeds(embeds);
            } else {
                request = embedsStarter.apply(embeds);
            }
        }
        return this;
    }

    public final DiscordMessageSender<T> withFiles(FileUpload... files) {
        if (Arrays.stream(files).allMatch(Objects::nonNull)) {
            if (request != null) {
                request.addFiles(files);
            } else {
                request = filesStarter.apply(files);
            }
        }
        return this;
    }

    public abstract DiscordMessageSender<T> withSticker(@Nullable StickersProvider.StickerData stickerData);

    public abstract void beforeSend();

    public final Optional<Message> send(boolean complete) {
        beforeSend();

        if (complete)
            return Optional.of(request.complete());

        request.queue();
        return Optional.empty();
    }

    public static class WebhookBuilder extends DiscordMessageSender<WebhookMessageCreateAction<Message>> {
        private String avatarUrl;
        private String userName;
        private ThreadChannel thread;

        public WebhookBuilder(Webhook webhook) {
            super(
                    webhook::sendMessage,
                    embeds -> webhook.sendMessageEmbeds(Arrays.asList(embeds)),
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

        public WebhookBuilder withUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public WebhookBuilder withThread(GuildMessageChannel channel) {
            if (channel instanceof ThreadChannel threadChannel){
                this.thread = threadChannel;
            }

            return this;
        }
    }

    public static class ChannelBuilder extends DiscordMessageSender<MessageCreateAction> {
        private final GuildMessageChannel channel;

        public ChannelBuilder(GuildMessageChannel channel) {
            super(
                    channel::sendMessage,
                    embeds -> channel.sendMessageEmbeds(Arrays.asList(embeds)),
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
package com.denisnumb.discord_chat_mod.api_impl;

import com.denisnumb.discord_chat_mod.DiscordChatMod;
import com.denisnumb.discord_chat_mod.api.ChannelKey;
import com.denisnumb.discord_chat_mod.api.Delivery;
import com.denisnumb.discord_chat_mod.api.DiscordMessage;
import com.denisnumb.discord_chat_mod.api.Embed;
import com.denisnumb.discord_chat_mod.api.MentionType;
import com.denisnumb.discord_chat_mod.api.PlayerRef;
import com.denisnumb.discord_chat_mod.api.SendResult;
import com.denisnumb.discord_chat_mod.api.SendStatus;
import com.denisnumb.discord_chat_mod.api.Sender;
import com.denisnumb.discord_chat_mod.compat.vanish.VanishCompatProvider;
import com.denisnumb.discord_chat_mod.config.configs.ApiConfig;
import com.denisnumb.discord_chat_mod.discord.DiscordGuildRegistry;
import com.denisnumb.discord_chat_mod.discord.chat.DiscordMessageFormatter;
import com.denisnumb.discord_chat_mod.discord.chat.DiscordMessageSender;
import com.denisnumb.discord_chat_mod.discord.chat.model.DiscordAttachment;
import com.denisnumb.discord_chat_mod.discord.chat.model.DiscordMessageBody;
import com.denisnumb.discord_chat_mod.discord.chat.model.DiscordMessageComponents;
import com.denisnumb.discord_chat_mod.discord.model.DiscordGuildContext;
import com.denisnumb.discord_chat_mod.utils.PlayerAvatarProvider;
import com.mojang.logging.LogUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

final class ApiMessageSender {
    private ApiMessageSender() {}

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ApiRateLimiter RATE_LIMITER = new ApiRateLimiter();

    static CompletableFuture<SendResult> send(String modId, ChannelKey channel, DiscordMessage message) {
        ApiRoute route = ApiRoute.of(channel);
        if (route == null)
            return finish(modId, channel, rejected(SendStatus.UNKNOWN_CHANNEL));
        if (!ApiConfig.ENABLED.get() || ApiConfig.DISABLED_MODS.get().contains(modId))
            return finish(modId, channel, rejected(SendStatus.BLOCKED_BY_ADMIN));
        if (!DiscordChatApiImpl.isConnected())
            return finish(modId, channel, rejected(SendStatus.NOT_CONNECTED));
        if (!RATE_LIMITER.tryAcquire(modId, ApiConfig.MAX_MESSAGES_PER_MINUTE_PER_MOD.get()))
            return finish(modId, channel, rejected(SendStatus.RATE_LIMITED));

        CompletableFuture<SendResult> result = DiscordMessageSender.supplyAsync(() -> prepareAndDeliver(route, message))
                .thenCompose(Function.identity())
                .exceptionally(e -> {
                    Throwable cause = e instanceof CompletionException && e.getCause() != null ? e.getCause() : e;
                    if (cause instanceof DiscordMessageSender.NotConnectedException)
                        return SendResult.rejected(SendStatus.NOT_CONNECTED);

                    LOGGER.error("[api] Failed to send a message of mod \"{}\" to \"{}\"", modId, channel, cause);
                    return SendResult.rejected(SendStatus.FAILED);
                });

        return finish(modId, channel, result);
    }

    private static CompletableFuture<SendResult> rejected(SendStatus status) {
        return CompletableFuture.completedFuture(SendResult.rejected(status));
    }

    private static CompletableFuture<SendResult> finish(String modId, ChannelKey channel, CompletableFuture<SendResult> result) {
        if (!ApiConfig.LOG_API_MESSAGES.get())
            return result;

        return result.whenComplete((sendResult, error) -> {
            if (sendResult != null)
                LOGGER.info("[api] {} -> {}: {}", modId, channel, sendResult);
        });
    }

    // =================================================================================================================
    //                                                MESSAGE BODY
    // =================================================================================================================

    private static CompletableFuture<SendResult> prepareAndDeliver(ApiRoute route, DiscordMessage message) {
        Sender sender = message.sender();
        ServerPlayer onlinePlayer = sender.player().map(player -> findOnlinePlayer(player.uuid())).orElse(null);
        if (onlinePlayer != null && VanishCompatProvider.isVanished(onlinePlayer))
            return rejected(SendStatus.SENDER_HIDDEN);

        String userName;
        String avatarUrl;
        DiscordMessageComponents components;

        switch (sender.kind()) {
            case PLAYER -> {
                PlayerRef player = sender.player().orElseThrow();
                userName = onlinePlayer != null ? onlinePlayer.getDisplayName().getString() : player.name();
                avatarUrl = onlinePlayer != null
                        ? PlayerAvatarProvider.getPlayerAvatarUrl(onlinePlayer)
                        : PlayerAvatarProvider.getPlayerAvatarUrl(player.uuid(), player.name(), null);
                components = formatWithChatStyle(onlinePlayer, userName, message.content());
            }
            case CUSTOM -> {
                userName = sender.name().orElseThrow();
                avatarUrl = sender.avatarUrl().orElse(null);
                components = formatWithChatStyle(null, userName, message.content());
            }
            default -> {
                userName = DiscordMessageSender.getWebhookServerName();
                avatarUrl = null;
                DiscordMessageBody body = new DiscordMessageBody(message.content(), List.of());
                components = new DiscordMessageComponents(body, body);
            }
        }

        components = components.withAppended(new DiscordMessageBody(
                Optional.empty(),
                message.embeds().stream().map(ApiMessageSender::toJdaEmbed).toList(),
                message.files().stream().map(file -> new DiscordAttachment(file.fileName(), file.data(), file.spoiler())).toList()
        ));
        components = new DiscordMessageComponents(limitContent(components.webhook()), limitContent(components.regular()));

        return deliver(route, userName, avatarUrl, components, toJdaMentions(message));
    }

    private static @Nullable ServerPlayer findOnlinePlayer(UUID uuid) {
        MinecraftServer server = DiscordChatMod.server;
        return server == null ? null : server.getPlayerList().getPlayer(uuid);
    }

    private static DiscordMessageComponents formatWithChatStyle(@Nullable ServerPlayer player, String name, Optional<String> content) {
        if (content.isEmpty()) {
            DiscordMessageBody empty = new DiscordMessageBody(Optional.empty(), List.of());
            return new DiscordMessageComponents(empty, empty);
        }
        return DiscordMessageFormatter.formatPlayerChatMessage(player, name, content.get());
    }

    private static DiscordMessageBody limitContent(DiscordMessageBody body) {
        String content = body.getContent();
        if (content == null || content.length() <= DiscordMessage.MAX_CONTENT_LENGTH)
            return body;

        String limited = content.substring(0, DiscordMessage.MAX_CONTENT_LENGTH - 1) + "…";
        return new DiscordMessageBody(Optional.of(limited), body.embeds(), body.attachments());
    }

    private static Set<Message.MentionType> toJdaMentions(DiscordMessage message) {
        EnumSet<Message.MentionType> result = EnumSet.noneOf(Message.MentionType.class);

        if (message.allowedMentions().allows(MentionType.USERS))
            result.add(Message.MentionType.USER);
        if (message.allowedMentions().allows(MentionType.ROLES))
            result.add(Message.MentionType.ROLE);
        if (message.allowedMentions().allows(MentionType.EVERYONE) && ApiConfig.ALLOW_EVERYONE_MENTIONS.get()) {
            result.add(Message.MentionType.EVERYONE);
            result.add(Message.MentionType.HERE);
        }

        return result;
    }

    private static MessageEmbed toJdaEmbed(Embed embed) {
        EmbedBuilder builder = new EmbedBuilder();

        embed.title().ifPresent(title -> builder.setTitle(title, embed.url().orElse(null)));
        embed.description().ifPresent(description -> builder.setDescription(description));
        embed.color().ifPresent(color -> builder.setColor(color.intValue()));
        embed.authorName().ifPresent(name -> builder.setAuthor(name, embed.authorUrl().orElse(null), embed.authorIconUrl().orElse(null)));
        embed.footerText().ifPresent(text -> builder.setFooter(text, embed.footerIconUrl().orElse(null)));
        embed.thumbnailUrl().ifPresent(url -> builder.setThumbnail(url));
        embed.imageUrl().ifPresent(url -> builder.setImage(url));
        embed.timestamp().ifPresent(timestamp -> builder.setTimestamp(timestamp));
        for (Embed.Field field : embed.fields())
            builder.addField(field.name(), field.value(), field.inline());

        return builder.build();
    }

    // =================================================================================================================
    //                                                  DELIVERY
    // =================================================================================================================

    private static CompletableFuture<SendResult> deliver(
            ApiRoute route,
            @Nullable String userName,
            @Nullable String avatarUrl,
            DiscordMessageComponents components,
            Set<Message.MentionType> allowedMentions
    ) {
        List<CompletableFuture<Delivery>> deliveries = new ArrayList<>();

        for (DiscordGuildContext context : DiscordGuildRegistry.getAllContexts()) {
            String guildId = context.getGuild().getId();
            List<ApiRoute.Target> targets = route.resolve(context);

            if (targets.isEmpty()) {
                deliveries.add(CompletableFuture.completedFuture(Delivery.disabled(guildId)));
                continue;
            }

            for (ApiRoute.Target target : targets) {
                GuildMessageChannel channel = target.channel();
                if (channel == null) {
                    deliveries.add(CompletableFuture.completedFuture(Delivery.failed(
                            guildId,
                            target.unresolvedChannelId(),
                            "Channel not found or missing bot permissions"
                    )));
                    continue;
                }

                deliveries.add(DiscordMessageSender.submitMessage(context, channel, userName, avatarUrl, components, allowedMentions)
                        .handle((sent, error) -> {
                            if (error == null)
                                return Delivery.sent(guildId, sent.getChannelId(), sent.getId());

                            String reason = DiscordMessageSender.describeSendError(error);
                            LOGGER.warn("[api] Failed to send a message to #{} ({}): {}", channel.getName(), context.getGuild().getName(), reason);
                            return Delivery.failed(guildId, channel.getId(), reason);
                        }));
            }
        }

        return CompletableFuture.allOf(deliveries.toArray(CompletableFuture[]::new))
                .thenApply(ignored -> SendResult.of(deliveries.stream().map(CompletableFuture::join).toList()));
    }
}

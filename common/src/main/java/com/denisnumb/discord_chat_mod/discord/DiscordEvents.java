package com.denisnumb.discord_chat_mod.discord;

import com.denisnumb.discord_chat_mod.ColorUtils;
import com.denisnumb.discord_chat_mod.EmojiUtils;
import com.denisnumb.discord_chat_mod.MinecraftUtils;
import com.denisnumb.discord_chat_mod.config.ConfigProvider;
import com.denisnumb.discord_chat_mod.discord.chat_style.DiscordChatStyleProvider;
import com.denisnumb.discord_chat_mod.discord.data_providers.ChannelMembersProvider;
import com.denisnumb.discord_chat_mod.discord.model.DiscordGuildContext;
import com.denisnumb.discord_chat_mod.discord.model.DiscordMentionData;
import com.denisnumb.discord_chat_mod.discord.utils.WebhookUtils;
import com.denisnumb.discord_chat_mod.markdown.MarkdownParser;
import com.denisnumb.discord_chat_mod.markdown.MarkdownToComponentConverter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.sticker.StickerItem;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.Component;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.denisnumb.discord_chat_mod.ColorUtils.Color.CHAT_LINK_COLOR;
import static com.denisnumb.discord_chat_mod.DiscordChatMod.jda;
import static com.denisnumb.discord_chat_mod.DiscordChatMod.server;
import static com.denisnumb.discord_chat_mod.LocaleProvider.getTranslate;
import static com.denisnumb.discord_chat_mod.MinecraftUtils.getServerPlayerCount;
import static com.denisnumb.discord_chat_mod.ModLanguageKey.FORWARDED_GUILD_MESSAGE;
import static com.denisnumb.discord_chat_mod.ModLanguageKey.REPLY;
import static com.denisnumb.discord_chat_mod.ModLanguageKey.STICKER;
import static com.denisnumb.discord_chat_mod.chat_images.utils.ImageUtils.getInputStreamFromUrl;
import static com.denisnumb.discord_chat_mod.chat_style.ChatStyleUtils.applyParametersToTemplate;
import static com.denisnumb.discord_chat_mod.chat_style.ChatStyleUtils.parseConfigTemplateMarkdown;
import static com.denisnumb.discord_chat_mod.chat_style.Parameters.*;
import static com.denisnumb.discord_chat_mod.discord.utils.DiscordUrlsUtils.retrieveMessageEmbedUrls;
import static com.denisnumb.discord_chat_mod.discord.utils.DiscordMessageUtils.*;
import static com.denisnumb.discord_chat_mod.discord.utils.WebhookUtils.sendWebhookWithFiles;

public class DiscordEvents extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isWebhookMessage() || event.getAuthor().getId().equals(jda.getSelfUser().getId()))
            return;
        if (!isTrackedChannel(event.getMessage().getChannelId()))
            return;

        if (ConfigProvider.getConfig().isDiscordMessagesLoggingEnabled()){
            System.out.printf("[Discord] <%s> %s%n",
                    event.getAuthor().getEffectiveName(),
                    event.getMessage().getContentDisplay());
        }

        if (getServerPlayerCount(server) > 0){
            prepareComponents(event.getMessage())
                    .thenAccept(components -> components.forEach(MinecraftUtils::sendMessageToAllPlayers));
        }

        DiscordChannelRegistry.getAllContexts().stream()
                .filter(ctx -> !ctx.defaultChannel.getId().equals(event.getMessage().getChannelId()))
                .forEach(ctx -> retranslateGuildMessage(ctx, event));
    }

    private static void retranslateGuildMessage(DiscordGuildContext guildContext, MessageReceivedEvent event) {
        List<MessageEmbed> embeds = event.getMessage().getEmbeds();
        MessageEmbed embed = embeds.isEmpty() ? null : event.getMessage().getEmbeds().get(0);

        List<WebhookUtils.WebhookAttachment> attachments = new ArrayList<>();

        for (Message.Attachment attachment : event.getMessage().getAttachments()) {
            try {
                attachments.add(new WebhookUtils.WebhookAttachment(getInputStreamFromUrl(attachment.getUrl()).readAllBytes(), attachment.getFileName()));
            } catch (Exception ignored) {}
        }

        if (attachments.size() < 10 && !event.getMessage().getStickers().isEmpty()){
            StickerItem sticker = event.getMessage().getStickers().get(0);
            try {
                attachments.add(new WebhookUtils.WebhookAttachment(
                        getInputStreamFromUrl(sticker.getIconUrl()).readAllBytes(),
                        getStickerFileName(sticker.getIconUrl())
                ));
            } catch (Exception ignored) {}
        }

        String userName = ConfigProvider.getConfig()
                .discordGuildForwardedMessageUserNameStyle()
                .replace(USER, event.getAuthor().getGlobalName())
                .replace(MEMBER, event.getMember().getEffectiveName())
                .replace(GUILD, event.getGuild().getName());
        String messageContent = event.getMessage().getContentRaw();

        guildContext.getWebhook(guildContext.defaultChannel).ifPresentOrElse(
                webhook -> {
                    sendWebhookWithFiles(
                            webhook.getUrl(),
                            new WebhookUtils.WebhookPayload(messageContent, embed)
                                    .setAvatarUrl(event.getAuthor().getAvatarUrl())
                                    .setUsername(userName),
                            attachments
                    );
                },
                () -> {
                    String message = String.format(getTranslate(FORWARDED_GUILD_MESSAGE), event.getMember().getEffectiveName(), event.getGuild().getName()) + "\n" + messageContent;
                    prepareDiscordMessage(
                            guildContext.defaultChannel,
                            new DiscordChatStyleProvider.DiscordMessageComponents(
                                    Optional.of(message),
                                    embed == null
                                            ? Optional.empty()
                                            : Optional.of(embed)
                            )
                    ).ifPresent(mca -> {
                        for (WebhookUtils.WebhookAttachment attachment : attachments)
                            mca.addFiles(FileUpload.fromData(attachment.data(), attachment.fileName()));
                        sendDiscordMessage(mca, guildContext.defaultChannel, false);
                    });
                }
        );
    }

    private static CompletableFuture<List<Component>> prepareComponents(Message message) {
        Member member = Objects.requireNonNull(message.getMember());
        MessageContext ctx = buildMessageContext(member, message);

        CompletableFuture<Optional<Component>> textFuture = buildTextComponent(message, ctx);
        Optional<Component> attachmentComponent = buildAttachmentsComponent(message, ctx);
        Optional<Component> stickerComponent = buildStickerComponent(message, ctx);

        return textFuture.thenApply(textOpt -> {
            List<Component> result = new ArrayList<>();
            textOpt.ifPresent(result::add);
            attachmentComponent.ifPresent(result::add);
            stickerComponent.ifPresent(result::add);
            return result;
        });
    }

    private record MessageContext(
            Component guild,
            Component userName,
            Component replyPrefix,
            String configTemplate
    ) {}

    private static MessageContext buildMessageContext(Member member, Message message) {
        Color roleColor = member.getColor() == null ? Color.WHITE : member.getColor();
        Component guild = Component.literal(message.getGuild().getName());

        Component userName = Component.empty().append(
                Component.literal(member.getEffectiveName())
                        .withStyle(style -> style
                                .withColor(TextColor.parseColor(ColorUtils.getHexColor(roleColor.getRGB())))
                                .withInsertion("@" + ChannelMembersProvider.getMemberDisplayName(member))
                                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/mention " + ChannelMembersProvider.getMemberDisplayName(member)))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(member.getUser().getName())))
                        )
        );

        Component replyPrefix = buildReplyPrefix(message.getReferencedMessage());

        return new MessageContext(
                guild,
                userName,
                replyPrefix,
                ConfigProvider.getConfig().minecraftDiscordMessagesStyle()
        );
    }

    private static Component buildReplyPrefix(Message referencedMessage) {
        if (referencedMessage == null)
            return Component.empty();

        String replyAuthor = referencedMessage.getMember() != null
                ? ChannelMembersProvider.getMemberDisplayName(referencedMessage.getMember())
                : referencedMessage.getAuthor().getEffectiveName();

        String rawPreview = referencedMessage.getContentDisplay();
        String preview = rawPreview.length() > 50
                ? rawPreview.substring(0, 50) + "..."
                : rawPreview;

        return Component.literal(String.format(getTranslate(REPLY), replyAuthor) + " ")
                .withStyle(style -> style
                        .withColor(0x7A7A7A)
                        .withItalic(true)
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                Component.literal(replyAuthor + ": " + preview)))
                );
    }

    private static CompletableFuture<Optional<Component>> buildTextComponent(Message message, MessageContext ctx) {
        if (message.getContentRaw().isEmpty() && ctx.replyPrefix().getString().isEmpty())
            return CompletableFuture.completedFuture(Optional.empty());

        if (message.getContentRaw().isEmpty())
            return CompletableFuture.completedFuture(Optional.of(wrap(ctx.replyPrefix(), ctx)));

        Map<String, DiscordMentionData> mentions = collectMentions(message);
        String rawContent = EmojiUtils.replaceDiscordEmojiMentionsToEmojiNames(message.getContentRaw());

        return retrieveMessageEmbedUrls(message)
                .thenApply(embedUrls ->
                        new MarkdownToComponentConverter(
                                MarkdownParser.parseMarkdown(rawContent, embedUrls),
                                mentions
                        ).convertMarkdownTokensToComponent()
                )
                .exceptionally(ignored -> fallbackTextComponent(message, mentions))
                .thenApply(textPart -> {
                    Component combined = ctx.replyPrefix().getString().isEmpty()
                            ? textPart
                            : Component.empty().append(ctx.replyPrefix()).append(textPart);
                    return Optional.of(wrap(combined, ctx));
                });
    }

    private static MutableComponent fallbackTextComponent(Message message, Map<String, DiscordMentionData> mentions) {
        String content = message.getContentRaw();
        for (var entry : mentions.entrySet())
            content = content.replace(entry.getKey(), entry.getValue().prettyMention);

        return Component.literal(content);
    }

    private static Optional<Component> buildAttachmentsComponent(Message message, MessageContext ctx) {
        if (message.getAttachments().isEmpty())
            return Optional.empty();

        MutableComponent part = Component.empty();
        List<Message.Attachment> attachments = message.getAttachments();
        for (int i = 0; i < attachments.size(); i++) {
            var file = attachments.get(i);
            boolean isLast = i == attachments.size() - 1;
            part.append(
                    Component.literal(file.getFileName() + (isLast ? "" : "\n"))
                            .withStyle(style -> style
                                    .withColor(CHAT_LINK_COLOR)
                                    .withItalic(true)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, file.getUrl()))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(file.getUrl())))
                            )
            );
        }
        return Optional.of(wrap(part, ctx));
    }

    private static Optional<Component> buildStickerComponent(Message message, MessageContext ctx) {
        if (message.getStickers().isEmpty())
            return Optional.empty();

        StickerItem sticker = message.getStickers().get(0);
        Component part = Component.literal(String.format(getTranslate(STICKER), sticker.getName()))
                .withStyle(style -> style
                        .withItalic(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, sticker.getIconUrl()))
                );

        return Optional.of(wrap(part, ctx));
    }

    private static Component wrap(Component messageComponent, MessageContext ctx) {
        return applyParametersToTemplate(
                parseConfigTemplateMarkdown(ctx.configTemplate()),
                Map.of(GUILD, ctx.guild(), MEMBER, ctx.userName(), MESSAGE, messageComponent)
        );
    }

    private static Map<String, DiscordMentionData> collectMentions(Message message) {
        Map<String, DiscordMentionData> mentions = new HashMap<>();

        message.getMentions()
                .getMembers()
                .forEach(u -> mentions.put(u.getAsMention(), new DiscordMentionData(u)));

        message.getMentions()
                .getRoles()
                .forEach(r -> mentions.put(r.getAsMention(), new DiscordMentionData(r)));

        message.getMentions()
                .getChannels()
                .forEach(c -> mentions.put(c.getAsMention(), new DiscordMentionData(c)));

        return mentions;
    }

    private static boolean isTrackedChannel(String channelId) {
        return DiscordChannelRegistry.getAllContexts().stream()
                .map(ctx -> ctx.getDefaultChannel().getId())
                .anyMatch(id -> id.equals(channelId));
    }
}

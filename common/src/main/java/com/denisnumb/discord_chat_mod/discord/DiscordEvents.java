package com.denisnumb.discord_chat_mod.discord;

import com.denisnumb.discord_chat_mod.config.ConfigProvider;
import com.denisnumb.discord_chat_mod.discord.chat_style.DiscordChatStyleProvider;
import com.denisnumb.discord_chat_mod.discord.model.DiscordGuildContext;
import com.denisnumb.discord_chat_mod.discord.model.DiscordMentionData;
import com.denisnumb.discord_chat_mod.markdown.MarkdownParser;
import com.denisnumb.discord_chat_mod.markdown.MarkdownPattern;
import com.denisnumb.discord_chat_mod.markdown.MarkdownToComponentConverter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.sticker.StickerItem;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.net.URI;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.denisnumb.discord_chat_mod.ColorUtils.Color.CHAT_LINK_COLOR;
import static com.denisnumb.discord_chat_mod.DiscordChatMod.jda;
import static com.denisnumb.discord_chat_mod.DiscordChatMod.server;
import static com.denisnumb.discord_chat_mod.LocaleProvider.getTranslate;
import static com.denisnumb.discord_chat_mod.MinecraftUtils.getServerPlayerCount;
import static com.denisnumb.discord_chat_mod.MinecraftUtils.sendMessageToAllPlayers;
import static com.denisnumb.discord_chat_mod.ModLanguageKey.FORWARDED_GUILD_MESSAGE;
import static com.denisnumb.discord_chat_mod.ModLanguageKey.STICKER;
import static com.denisnumb.discord_chat_mod.chat_images.utils.ImageUtils.getInputStreamFromUrl;
import static com.denisnumb.discord_chat_mod.chat_style.ChatStyleUtils.applyParametersToTemplate;
import static com.denisnumb.discord_chat_mod.chat_style.ChatStyleUtils.parseConfigTemplateMarkdown;
import static com.denisnumb.discord_chat_mod.chat_style.Parameters.*;
import static com.denisnumb.discord_chat_mod.discord.DiscordUtils.*;
import static com.denisnumb.discord_chat_mod.discord.WebhookUtils.sendWebhookWithFiles;

public class DiscordEvents extends ListenerAdapter {

    /**
     * Only retrieve embed URLs for Discord attachment links.
     * Don't retrieve embed URLs for other hosts as they may:
     * <ul>
     *     <li>Point media types that are incompatible with this mod, such as Tenor embed URLs pointing to video files.</li>
     *     <li>Be "incorrect", such as an embed URL for a social media post being a link to the post's image, causing the link to the post itself being overridden.</li>
     * </ul>
     */
    private static final Set<String> DISCORD_HOSTS = Set.of("cdn.discordapp.com", "media.discordapp.net");

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isWebhookMessage() || event.getAuthor().getId().equals(jda.getSelfUser().getId()))
            return;

        if (!DiscordChannelRegistry.getAllContexts()
                .stream()
                .map(ctx -> ctx.getDefaultChannel().getId())
                .toList()
                .contains(event.getMessage().getChannelId())
        )
            return;

        if (ConfigProvider.getConfig().isDiscordMessagesLoggingEnabled())
            System.out.printf("[Discord] <%s> %s%n", event.getAuthor().getEffectiveName(), event.getMessage().getContentDisplay());

        if (getServerPlayerCount(server) > 0)
            prepareComponents(event.getMessage()).thenAccept(components -> {
                for (Component component : components)
                    sendMessageToAllPlayers(component);
            });

        for (DiscordGuildContext guildContext : DiscordChannelRegistry.getAllContexts())
            if (!guildContext.defaultChannel.getId().equals(event.getMessage().getChannelId()))
                retranslateGuildMessage(guildContext, event);
    }

    public static void retranslateGuildMessage(DiscordGuildContext guildContext, MessageReceivedEvent event) {
        List<MessageEmbed> embeds = event.getMessage().getEmbeds();
        MessageEmbed embed = embeds.isEmpty() ? null : event.getMessage().getEmbeds().getFirst();

        List<WebhookUtils.WebhookAttachment> attachments = new ArrayList<>();

        for (Message.Attachment attachment : event.getMessage().getAttachments()) {
            try {
                attachments.add(new WebhookUtils.WebhookAttachment(getInputStreamFromUrl(attachment.getUrl()).readAllBytes(), attachment.getFileName()));
            } catch (Exception ignored) {}
        }

        if (attachments.size() < 10 && !event.getMessage().getStickers().isEmpty()){
            StickerItem sticker = event.getMessage().getStickers().getFirst();
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

    private static @NotNull CompletableFuture<List<Component>> prepareComponents(Message message) {
        List<Component> components = new ArrayList<>();

        Member member = Objects.requireNonNull(message.getMember());
        String userName = member.getEffectiveName();
        Color roleColor = member.getColor() == null ? Color.WHITE : member.getColor();
        String configTemplate = ConfigProvider.getConfig().minecraftDiscordMessagesStyle();

        Component guildComponent = Component.literal(message.getGuild().getName());

        Component userNameComponent = Component.empty()
                .append(Component.literal(userName)
                .withColor(roleColor.getRGB())
                .withStyle(style ->
                        style.withInsertion("@" + ChannelMembersProvider.getMemberDisplayName(member))
                                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/mention " + ChannelMembersProvider.getMemberDisplayName(member)))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(member.getUser().getName())))
                )
        );

        CompletableFuture<List<Component>> componentsFuture;

        if (!message.getContentRaw().isEmpty()) {
            Map<String, DiscordMentionData> mentions = new HashMap<>();

            for (Member user : message.getMentions().getMembers())
                mentions.put(user.getAsMention(), new DiscordMentionData(user));
            for (Role role : message.getMentions().getRoles())
                mentions.put(role.getAsMention(), new DiscordMentionData(role));
            for (GuildChannel channel : message.getMentions().getChannels())
                mentions.put(channel.getAsMention(), new DiscordMentionData(channel));

            componentsFuture = retrieveMessageEmbedUrls(message).thenApply(embedUrls ->
                new MarkdownToComponentConverter(
                    MarkdownParser.parseMarkdown(
                        replaceDiscordEmojiMentionsToEmojiNames(message.getContentRaw()),
                        embedUrls
                    ), mentions
                ).convertMarkdownTokensToComponent()
            ).exceptionally(ignored -> {
                String content = message.getContentRaw();
                for (var entry : mentions.entrySet())
                    content = content.replace(entry.getKey(), entry.getValue().prettyMention);
                return Component.literal(content);
            }).thenApply(textPart -> {
                addComponentsPart(components, configTemplate, guildComponent, userNameComponent, textPart);
                return components;
            });
        } else {
            componentsFuture = CompletableFuture.completedFuture(components);
        }

        return componentsFuture.thenApply(components1 -> {
            if (!message.getAttachments().isEmpty()) {
                MutableComponent attachmentPart = Component.empty();
                int index = 0;
                List<Message.Attachment> attachments = message.getAttachments();
                for (var file : attachments) {
                    attachmentPart.append(Component.literal(file.getFileName() + (++index < attachments.size() ? "\n" : ""))
                        .withColor(CHAT_LINK_COLOR).withStyle(style -> style.withItalic(true)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, file.getUrl()))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(file.getUrl())))
                        )
                    );
                }

                addComponentsPart(components1, configTemplate, guildComponent, userNameComponent, attachmentPart);
            }

            if (!message.getStickers().isEmpty()) {
                MutableComponent stickerPart = Component.empty();
                StickerItem sticker = message.getStickers().getFirst();
                stickerPart.append(Component.literal(String.format(getTranslate(STICKER), sticker.getName()))
                    .withStyle(style -> style
                        .withItalic(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, sticker.getIconUrl()))
                    )
                );

                addComponentsPart(components1, configTemplate, guildComponent, userNameComponent, stickerPart);
            }

            return components1;
        });
    }

    private static void addComponentsPart(
        List<Component> components,
            String configTemplate,
            Component guildComponent,
            Component userNameComponent,
            Component messageComponent) {
        components.add(applyParametersToTemplate(
                parseConfigTemplateMarkdown(configTemplate),
                Map.of(GUILD, guildComponent, MEMBER, userNameComponent, MESSAGE, messageComponent)
        ));
    }

    /**
     * Retrieves refreshed Discord attachment links asynchronously via message embeds.
     */
    private static @NotNull CompletableFuture<Map<String, String>> retrieveMessageEmbedUrls(Message message) {
        return retrieveMessageEmbedUrls(message, 3);
    }

    private static @NotNull CompletableFuture<Map<String, String>> retrieveMessageEmbedUrls(Message message, int retries) {
        Map<String, String> embedUrls = getMessageEmbedUrls(message);
        try {
            /*
             * Links that have been sent for the first time may not have embeds generated yet,
             * so refetch the message a few times to get the embeds.
             * Only refetch the message if it contains URLs.
             */
            if (retries > 0 && embedUrls.isEmpty() && hasUrls(message.getContentRaw())) {
                return message.getChannel()
                    .retrieveMessageById(message.getId())
                    .submit()
                    .exceptionally(ignored -> message)
                    .thenCompose(refetchedMessage -> retrieveMessageEmbedUrls(refetchedMessage, retries - 1));
            }
        } catch (Exception ignored) {
        }
        return CompletableFuture.completedFuture(embedUrls);
    }

    private static @NotNull Map<String, String> getMessageEmbedUrls(Message message) {
        return message.getEmbeds()
            .stream()
            .map((embed) -> {
                String url = embed.getUrl();
                if (url == null)
                    return null;
                String mediaUrl = getMessageEmbedMediaUrl(embed);
                if (mediaUrl == null)
                    return null;
                URI uri = URI.create(url);
                if (!DISCORD_HOSTS.contains(uri.getHost()))
                    return null;
                return Map.entry(url, mediaUrl);
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static @Nullable String getMessageEmbedMediaUrl(MessageEmbed embed) {
        String mediaUrl = null;
        if (embed.getImage() != null)
            mediaUrl = embed.getImage().getUrl();
        else if (embed.getVideoInfo() != null)
            mediaUrl = embed.getVideoInfo().getUrl();
        else if (embed.getThumbnail() != null)
            mediaUrl = embed.getThumbnail().getUrl();
        return mediaUrl;
    }

    private static boolean hasUrls(String text) {
        return MarkdownPattern.URL.matcher(text).results().anyMatch(matchResult -> {
            URI uri = URI.create(matchResult.group());
            return DISCORD_HOSTS.contains(uri.getHost());
        });
    }
}

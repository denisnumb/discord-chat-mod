package com.denisnumb.discord_chat_mod.discord;

import com.denisnumb.discord_chat_mod.ColorUtils;
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
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.sticker.StickerItem;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
import static com.denisnumb.discord_chat_mod.discord.utils.DiscordUrlsUtils.retrieveMessageEmbedUrls;
import static com.denisnumb.discord_chat_mod.discord.utils.DiscordMessageUtils.*;
import static com.denisnumb.discord_chat_mod.discord.utils.WebhookUtils.sendWebhookWithFiles;

public class DiscordEvents extends ListenerAdapter {
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

    private static @NotNull CompletableFuture<List<Component>> prepareComponents(Message message) {
        List<Component> components = new ArrayList<>();

        Member member = Objects.requireNonNull(message.getMember());
        String userName = member.getEffectiveName();
        Color roleColor = member.getColor() == null ? Color.WHITE : member.getColor();
        String configTemplate = ConfigProvider.getConfig().minecraftDiscordMessagesStyle();

        Component guildComponent = Component.literal(message.getGuild().getName());

        Component userNameComponent = Component.empty()
                .append(Component.literal(userName)
                        .withStyle(style ->
                                style.withColor(TextColor.parseColor(ColorUtils.getHexColor(roleColor.getRGB())))
                                        .withInsertion("@" + ChannelMembersProvider.getMemberDisplayName(member))
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

        return componentsFuture.thenApply(messageComponents -> {
            if (!message.getAttachments().isEmpty()) {
                MutableComponent attachmentPart = Component.empty();
                int index = 0;
                List<Message.Attachment> attachments = message.getAttachments();
                for (var file : attachments) {
                    attachmentPart.append(Component.literal(file.getFileName() + (++index < attachments.size() ? "\n" : ""))
                            .withStyle(style -> style.withColor(CHAT_LINK_COLOR)
                                    .withItalic(true)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, file.getUrl()))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(file.getUrl())))
                            )
                    );
                }

                addComponentsPart(messageComponents, configTemplate, guildComponent, userNameComponent, attachmentPart);
            }

            if (!message.getStickers().isEmpty()) {
                MutableComponent stickerPart = Component.empty();
                StickerItem sticker = message.getStickers().get(0);
                stickerPart.append(Component.literal(String.format(getTranslate(STICKER), sticker.getName()))
                        .withStyle(style -> style
                                .withItalic(true)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, sticker.getIconUrl()))
                        )
                );

                addComponentsPart(messageComponents, configTemplate, guildComponent, userNameComponent, stickerPart);
            }

            return messageComponents;
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
}

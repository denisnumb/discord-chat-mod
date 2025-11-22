package com.denisnumb.discord_chat_mod.discord;

import com.denisnumb.discord_chat_mod.config.ConfigProvider;
import com.denisnumb.discord_chat_mod.discord.chat_style.DiscordChatStyleProvider;
import com.denisnumb.discord_chat_mod.discord.model.DiscordGuildContext;
import com.denisnumb.discord_chat_mod.discord.model.DiscordMentionData;
import com.denisnumb.discord_chat_mod.markdown.MarkdownParser;
import com.denisnumb.discord_chat_mod.markdown.MarkdownToComponentConverter;
import net.dv8tion.jda.api.entities.*;
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

import java.awt.*;
import java.util.List;
import java.util.*;

import static com.denisnumb.discord_chat_mod.ColorUtils.Color.*;
import static com.denisnumb.discord_chat_mod.DiscordChatMod.jda;
import static com.denisnumb.discord_chat_mod.DiscordChatMod.server;
import static com.denisnumb.discord_chat_mod.LocaleProvider.getTranslate;
import static com.denisnumb.discord_chat_mod.MinecraftUtils.*;
import static com.denisnumb.discord_chat_mod.ModLanguageKey.FORWARDED_GUILD_MESSAGE;
import static com.denisnumb.discord_chat_mod.ModLanguageKey.STICKER;
import static com.denisnumb.discord_chat_mod.chat_images.utils.ImageUtils.getInputStreamFromUrl;
import static com.denisnumb.discord_chat_mod.chat_style.ChatStyleUtils.*;
import static com.denisnumb.discord_chat_mod.chat_style.Parameters.*;
import static com.denisnumb.discord_chat_mod.discord.DiscordUtils.*;
import static com.denisnumb.discord_chat_mod.discord.WebhookUtils.sendWebhookWithFiles;

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
            for (Component component : prepareComponents(event.getMessage()))
                sendMessageToAllPlayers(component);

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

    private static @NotNull List<Component> prepareComponents(Message message) {
        ArrayList<Component> components = new ArrayList<>();

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

        if (!message.getContentRaw().isEmpty()) {
            Map<String, DiscordMentionData> mentions = new HashMap<>();

            for (Member user : message.getMentions().getMembers())
                mentions.put(user.getAsMention(), new DiscordMentionData(user));
            for (Role role : message.getMentions().getRoles())
                mentions.put(role.getAsMention(), new DiscordMentionData(role));
            for (GuildChannel channel : message.getMentions().getChannels())
                mentions.put(channel.getAsMention(), new DiscordMentionData(channel));

            Component textPart;
            try {
                textPart = new MarkdownToComponentConverter(
                        MarkdownParser.parseMarkdown(replaceDiscordEmojiMentionsToEmojiNames(message.getContentRaw())), mentions
                ).convertMarkdownTokensToComponent();
            } catch (Exception ignored) {
                String content = message.getContentRaw();
                for (var entry : mentions.entrySet())
                    content = content.replace(entry.getKey(), entry.getValue().prettyMention);
                textPart = Component.literal(content);
            }

            addComponentsPart(components, configTemplate, guildComponent, userNameComponent, textPart);
        }

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

            addComponentsPart(components, configTemplate, guildComponent, userNameComponent, attachmentPart);
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

            addComponentsPart(components, configTemplate, guildComponent, userNameComponent, stickerPart);
        }

        return components;
    }

    private static void addComponentsPart(
            ArrayList<Component> components,
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

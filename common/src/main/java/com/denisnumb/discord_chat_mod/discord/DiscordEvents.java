package com.denisnumb.discord_chat_mod.discord;

import com.denisnumb.discord_chat_mod.ColorUtils;
import com.denisnumb.discord_chat_mod.config.PlatformConfig;
import com.denisnumb.discord_chat_mod.discord.model.DiscordMentionData;
import com.denisnumb.discord_chat_mod.markdown.MarkdownParser;
import com.denisnumb.discord_chat_mod.markdown.MarkdownToComponentConverter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.sticker.StickerItem;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.*;

import static com.denisnumb.discord_chat_mod.ColorUtils.Color.*;
import static com.denisnumb.discord_chat_mod.DiscordChatMod.jda;
import static com.denisnumb.discord_chat_mod.DiscordChatMod.server;
import static com.denisnumb.discord_chat_mod.LocaleProvider.getTranslate;
import static com.denisnumb.discord_chat_mod.MinecraftUtils.*;
import static com.denisnumb.discord_chat_mod.ModLanguageKey.STICKER;
import static com.denisnumb.discord_chat_mod.discord.DiscordUtils.replaceDiscordEmojiMentionsToEmojiNames;

public class DiscordEvents extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getMessage().getChannelId().equals(PlatformConfig.getConfig().discordChannelId())
                || event.getAuthor().getId().equals(jda.getSelfUser().getId()))
            return;
        if (event.isWebhookMessage())
            return;

        if (PlatformConfig.getConfig().isDiscordMessagesLoggingEnabled())
            System.out.printf("[Discord] <%s> %s%n", event.getAuthor().getEffectiveName(), event.getMessage().getContentDisplay());
        if (getServerPlayerCount(server) == 0)
            return;

        for (Component component : prepareMessages(event.getMessage()))
            sendMessageToAllPlayers(component);
    }

    private static @NotNull List<Component> prepareMessages(Message message){
        ArrayList<Component> components = new ArrayList<>();

        Member member = Objects.requireNonNull(message.getMember());
        String userName = member.getEffectiveName();
        Color roleColor = member.getColor() == null ? Color.WHITE : member.getColor();

        Component basePart = Component.empty()
                .append(Component.literal("[discord] ")
                        .withStyle(style -> style.withBold(true).withColor(DISCORD_COLOR))
                )
                .append(Component.literal("<"))
                .append(Component.literal(userName)
                        .withStyle(style ->
                                style.withInsertion("@" + userName)
                                        .withColor(TextColor.parseColor(ColorUtils.getHexColor(roleColor)))
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/mention " + userName))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(member.getUser().getEffectiveName())))
                        )
                )
                .append(Component.literal("> "));

        if (!message.getContentRaw().isEmpty()){
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

            components.add(basePart.copy().append(textPart));
        }

        if (!message.getAttachments().isEmpty()){
            MutableComponent attachmentPart = Component.empty();
            int index = 0;
            List<Message.Attachment> attachments = message.getAttachments();
            for (var file : attachments){
                attachmentPart.append(Component.literal(file.getFileName() + (++index < attachments.size() ? "\n" : ""))
                        .withStyle(style -> style.withItalic(true)
                                .withColor(CHAT_LINK_COLOR)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, file.getUrl()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(file.getUrl())))
                        )
                );
            }

            components.add(basePart.copy().append(attachmentPart));
        }

        if (!message.getStickers().isEmpty()){
            MutableComponent stickerPart = Component.empty();
            StickerItem sticker = message.getStickers().get(0);
            stickerPart.append(Component.literal(String.format(getTranslate(STICKER), sticker.getName()))
                    .withStyle(style -> style
                            .withItalic(true)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, sticker.getIconUrl()))
                    )
            );


            components.add(basePart.copy().append(stickerPart));
        }

        return components;
    }
}

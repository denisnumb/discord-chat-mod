package com.denisnumb.discord_chat_mod.discord;

import com.denisnumb.discord_chat_mod.config.PlatformConfig;
import com.denisnumb.discord_chat_mod.network.screenshot.ScreenshotTransceiver;
import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.*;
import static com.denisnumb.discord_chat_mod.LocaleProvider.getTranslate;
import static com.denisnumb.discord_chat_mod.MinecraftUtils.logErrorToServer;
import static com.denisnumb.discord_chat_mod.ModLanguageKey.*;
import static com.denisnumb.discord_chat_mod.discord.WebhookUtils.*;

public class DiscordUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static MessageEmbed buildEmbed(String title, String description, int color){
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(color)
                .build();
    }

    public static MessageEmbed buildEmbed(String description, int color){
        return new EmbedBuilder()
                .setDescription(description)
                .setColor(color)
                .build();
    }

    public static void editMessageEmbeds(Message message, MessageEmbed embed){
        try {
            message.editMessageEmbeds(embed).queue();
        } catch (Exception e){
            LOGGER.error(e.getMessage());
        }
    }

    public static Optional<Message> sendServerStatusMessageComplete(GuildMessageChannel channel, MessageEmbed embed){
        return sendEmbedMessage(channel, embed, true);
    }

    public static void sendEmbedMessage(GuildMessageChannel channel, MessageEmbed embed){
        duplicateMessageToDefaultChannel(channel,
                () -> sendEmbedMessage(DiscordChannelRegistry.defaultChannel, embed)
        );

        Optional<Webhook> optionalWebhook = DiscordChannelRegistry.getChannelWebhook(channel);

        if (optionalWebhook.isEmpty()){
            sendEmbedMessage(channel, embed, false);
        } else {
            sendWebhook(optionalWebhook.get().getUrl(),
                    () -> new WebhookPayload(embed).setUsername(getWebhookServerName())
            );
        }
    }

    public static void sendTextMessage(GuildMessageChannel channel, @Nullable Player fromPlayer, String rawText, String formattedText) {
        duplicateMessageToDefaultChannel(channel,
                () -> sendTextMessage(DiscordChannelRegistry.defaultChannel, fromPlayer, rawText, formattedText)
        );

        Optional<Webhook> optionalWebhook = DiscordChannelRegistry.getChannelWebhook(channel);

        if (optionalWebhook.isEmpty()){
            prepareDiscordTextMessage(channel, formattedText).ifPresent(
                    messageCreateAction -> sendMessage(messageCreateAction, false)
            );
        } else {
            String webhookUrl = optionalWebhook.get().getUrl();
            if (fromPlayer == null)
                sendWebhook(webhookUrl, () -> new WebhookPayload(formattedText).setUsername(getWebhookServerName()));
            else {
                sendWebhook(webhookUrl, () -> new WebhookPayload(rawText)
                        .setUsername(fromPlayer.getName().getString())
                        .setAvatarUrl(getPlayerAvatarUrl(fromPlayer))
                );
            }
        }


    }

    public static Optional<String> sendTextMessageWithFileComplete(
            GuildMessageChannel channel,
            Player fromPlayer,
            String formattedText,
            ScreenshotTransceiver.ScreenshotData screenshotData
    ) {
        Optional<Webhook> optionalWebhook = DiscordChannelRegistry.getChannelWebhook(channel);
        Optional<String> optionalScreenshotUrl;

        if (optionalWebhook.isEmpty()){
            Optional<MessageCreateAction> createAction = prepareDiscordTextMessage(channel, formattedText);
            optionalScreenshotUrl = createAction.flatMap(mca -> sendMessage(mca.addFiles(FileUpload.fromData(screenshotData.data(), screenshotData.fileName())), true)
                    .map(message -> message.getAttachments().get(0).getUrl()));

        } else {
            try {
                optionalScreenshotUrl = sendWebhookWithImage(
                        optionalWebhook.get().getUrl(),
                        new WebhookPayload("")
                                .setUsername(fromPlayer.getName().getString())
                                .setAvatarUrl(getPlayerAvatarUrl(fromPlayer)),
                        screenshotData.data(),
                        screenshotData.fileName()
                ).get();
            } catch (Exception ignored) {
                optionalScreenshotUrl = Optional.empty();
            }
        }

        optionalScreenshotUrl.ifPresent(s -> duplicateMessageToDefaultChannel(channel,
                () -> sendTextMessage(DiscordChannelRegistry.defaultChannel, fromPlayer, s, "`<" + fromPlayer.getName().getString() + ">` " + s)
        ));

        return optionalScreenshotUrl;
    }

    private static void duplicateMessageToDefaultChannel(GuildMessageChannel duplicateChannel, Runnable sendMessageFunction){
        if (isDiscordConnected()
                && PlatformConfig.getConfig().isDuplicateMessagesEnabled()
                && !DiscordChannelRegistry.defaultChannel.equals(duplicateChannel)){
            sendMessageFunction.run();
        }
    }

    private static Optional<MessageCreateAction> prepareDiscordTextMessage(GuildMessageChannel channel, String text){
        if (!isDiscordConnected())
            return Optional.empty();
        try{
            return Optional.of(channel.sendMessage(text));
        } catch (Exception e){
            logErrorToServer(getTranslate(PREPARE_MESSAGE_ERROR));
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private static Optional<Message> sendEmbedMessage(GuildMessageChannel channel, MessageEmbed embed, boolean complete){
        if (!isDiscordConnected())
            return Optional.empty();
        try {
            if (complete){
                Optional<Message> message = sendMessage(channel.sendMessageEmbeds(embed), true);
                if (message.isPresent())
                    return message;
            } else
                sendMessage(channel.sendMessageEmbeds(embed), false);
        } catch (InsufficientPermissionException e){
            logErrorToServer(getTranslate(SEND_EMBED_ERROR));
            e.printStackTrace();
        } catch (Exception e){
            LOGGER.error(e.getMessage());
        }

        return Optional.empty();
    }

    private static Optional<Message> sendMessage(MessageCreateAction messageCreateAction, boolean complete) {
        try {
            if (complete)
                return Optional.of(messageCreateAction.complete());
            else
                messageCreateAction.queue();
        } catch (ErrorResponseException e) {
            logErrorToServer(String.format(getTranslate(SEND_MESSAGE_ERROR), e.getMeaning()));
            e.printStackTrace();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        return Optional.empty();
    }

    public static String replaceEmojiCodesToDiscordMentions(String text) {
        Pattern emojiPattern = Pattern.compile("(?<!\\\\):([a-zA-Z0-9_]{2,})(~([1-9][0-9]*))?:");
        Matcher matcher = emojiPattern.matcher(text);
        Map<String, List<RichCustomEmoji>> emojiMap = CustomEmojiProvider.getGuildEmojis(DiscordChannelRegistry.defaultChannel);

        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String baseName = matcher.group(1);
            String numberGroup = matcher.group(3);

            int index = 0;
            if (numberGroup != null) {
                try {
                    index = Integer.parseInt(numberGroup);
                } catch (NumberFormatException ignored) {
                    index = -1;
                }
            }

            List<RichCustomEmoji> emojis = emojiMap.getOrDefault(baseName, Collections.emptyList());
            if (index >= 0 && index < emojis.size()) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(emojis.get(index).getAsMention()));
            } else {
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group()));
            }
        }

        matcher.appendTail(result);
        return result.toString();
    }

    public static String replaceDiscordEmojiMentionsToEmojiNames(String text) {
        Pattern emojiMentionPattern = Pattern.compile("(?<!\\\\)<a?:([a-zA-Z0-9_]+):(\\d+)>");
        Matcher matcher = emojiMentionPattern.matcher(text);
        Map<String, List<RichCustomEmoji>> emojiMap = CustomEmojiProvider.getGuildEmojis(DiscordChannelRegistry.defaultChannel);

        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String name = matcher.group(1);
            String id = matcher.group(2);

            List<RichCustomEmoji> emojis = emojiMap.getOrDefault(name, Collections.emptyList());

            int index = -1;
            for (int i = 0; i < emojis.size(); i++) {
                if (emojis.get(i).getId().equals(id)) {
                    index = i;
                    break;
                }
            }

            String replacement = (index > 0)
                    ? ":" + name + "~" + index + ":"
                    : ":" + name + ":";

            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(result);
        return result.toString();
    }
}
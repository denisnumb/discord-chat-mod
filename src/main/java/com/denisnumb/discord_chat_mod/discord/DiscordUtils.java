package com.denisnumb.discord_chat_mod.discord;

import com.denisnumb.discord_chat_mod.markdown.tellraw.TellRawComponent;
import com.google.gson.Gson;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.*;
import static com.denisnumb.discord_chat_mod.MinecraftUtils.getTranslate;
import static com.denisnumb.discord_chat_mod.MinecraftUtils.logErrorToServer;
import static com.denisnumb.discord_chat_mod.ModLanguageKey.*;

public class DiscordUtils {
    private static final Gson gson = new Gson();
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final List<Permission> requiredPermissions = List.of(
            Permission.VIEW_CHANNEL,
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_SEND_IN_THREADS,
            Permission.MESSAGE_EMBED_LINKS,
            Permission.MESSAGE_ATTACH_FILES,
            Permission.MESSAGE_MANAGE,
            Permission.MESSAGE_HISTORY
    );

    public static Optional<Message> findPinnedStatusMessage(){
        try {
            return discordChannel.retrievePinnedMessages()
                    .complete()
                    .stream()
                    .filter(message -> message.getAuthor().getId().equals(jda.getSelfUser().getId()))
                    .findFirst();
        } catch (Exception e){
            LOGGER.error(e.getMessage());
            return Optional.empty();
        }
    }

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

    public static void sendShortEmbedMessage(String text, int color){
        sendEmbedMessage(buildEmbed(text, color), false);
    }

    public static void sendEmbedMessage(String title, String description, int color){
        sendEmbedMessage(buildEmbed(title, description, color), false);
    }

    public static Optional<Message> sendEmbedMessageComplete(MessageEmbed embed){
        return sendEmbedMessage(embed, true);
    }

    public static void sendMessage(MessageCreateAction messageCreateAction) {
        sendMessage(messageCreateAction, false);
    }

    public static Either<Message, Optional<ErrorResponseException>> sendMessageComplete(MessageCreateAction messageCreateAction) {
        return sendMessage(messageCreateAction, true);
    }

    public static Optional<MessageCreateAction> prepareDiscordTextMessage(String text){
        try{
            return Optional.of(discordChannel.sendMessage(text));
        } catch (Exception e){
            logErrorToServer(getTranslate(PREPARE_MESSAGE_ERROR, "Error sending message! Make sure the bot has access to the channel and the right to send messages."));
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private static Optional<Message> sendEmbedMessage(MessageEmbed embed, boolean complete){
        if (!isDiscordConnected())
            return Optional.empty();
        try {
            if (complete){
                Optional<Message> message = sendMessage(discordChannel.sendMessageEmbeds(embed), true).left();
                if (message.isPresent())
                    return message;
            } else
                sendMessage(discordChannel.sendMessageEmbeds(embed), false);
        } catch (InsufficientPermissionException e){
            logErrorToServer(getTranslate(SEND_EMBED_ERROR, "Error sending message! Make sure the bot has access to the channel, as well as the rights to send messages and embed links."));
            e.printStackTrace();
        } catch (Exception e){
            LOGGER.error(e.getMessage());
        }

        return Optional.empty();
    }

    private static Either<Message, Optional<ErrorResponseException>> sendMessage(MessageCreateAction messageCreateAction, boolean complete) {
        try {
            if (complete)
                return Either.left(messageCreateAction.complete());
            else
                messageCreateAction.queue();
        } catch (ErrorResponseException e) {
            logErrorToServer(String.format(
                    getTranslate(SEND_MESSAGE_ERROR, "Error sending message!\nCause: %s\nMake sure the bot has permission to embed links and attach files."),
                    e.getMeaning())
            );
            e.printStackTrace();
            return Either.right(Optional.of(e));
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        return Either.right(Optional.empty());
    }

    @SafeVarargs
    public static String prepareTellRawCommand(List<TellRawComponent>... parts){
        return prepareTellRawCommand("@a", parts);
    }

    @SafeVarargs
    public static String prepareTellRawCommand(String selector, List<TellRawComponent>... parts){
        List<Object> commandJson = new ArrayList<>();
        commandJson.add("");
        for (var part : parts)
            commandJson.addAll(part);

        return "/tellraw " + selector + " " + gson.toJson(commandJson);
    }

    public static String replaceEmojiCodesToDiscordMentions(String text){
        Pattern emojiPattern = Pattern.compile(":[a-zA-Z0-9_]{2,}:");
        Matcher matcher = emojiPattern.matcher(text);

        while (matcher.find()) {
            String emojiString = matcher.group();
            List<RichCustomEmoji> foundEmojis = discordChannel.getGuild()
                    .getEmojisByName(emojiString.replace(":", ""), true);
            if (!foundEmojis.isEmpty())
                text = text.replace(emojiString, foundEmojis.get(0).getAsMention());
        }

        return text;
    }
}
package com.denisnumb.discord_chat_mod.chat;

import com.denisnumb.discord_chat_mod.chat.message_part.MessageFormatContext;
import com.denisnumb.discord_chat_mod.chat.message_part.MessagePart;
import com.denisnumb.discord_chat_mod.chat.message_part.MessagePartFinder;
import com.denisnumb.discord_chat_mod.chat.message_part.TextPart;
import com.denisnumb.discord_chat_mod.chat.placeholder.ChatPlaceholderValues;
import com.denisnumb.discord_chat_mod.compat.xaero.XaeroWaypoint;
import com.denisnumb.discord_chat_mod.discord.model.ChannelCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class CommonMessageFormatter {
    private CommonMessageFormatter() {}

    public record FormattedMessage(Component forMinecraft, String forDiscord) {}

    private static final List<MessagePartFinder> PART_FINDERS = List.of(
            XaeroWaypoint.FINDER
    );

    public static FormattedMessage formatMessage(String message, ChannelCategory chatCategoryToParseMembers) {
        return format(message, chatCategoryToParseMembers, null);
    }

    public static FormattedMessage formatMessage(String message, ChannelCategory chatCategoryToParseMembers, ChatPlaceholderValues placeholderValues) {
        return format(message, chatCategoryToParseMembers, placeholderValues);
    }

    private static FormattedMessage format(String message, ChannelCategory chatCategoryToParseMembers, @Nullable ChatPlaceholderValues placeholderValues) {
        MessageFormatContext context = MessageFormatContext.create(chatCategoryToParseMembers, placeholderValues);

        List<FormattedMessage> formattedParts = splitIntoParts(message).stream()
                .map(part -> part.format(context))
                .toList();

        return join(formattedParts);
    }

    private static List<MessagePart> splitIntoParts(String message) {
        List<MessagePartFinder.Match> matches = PART_FINDERS.stream()
                .flatMap(finder -> finder.find(message).stream())
                .sorted(Comparator.comparingInt(MessagePartFinder.Match::start))
                .toList();

        List<MessagePart> parts = new ArrayList<>();
        int position = 0;

        for (MessagePartFinder.Match match : matches) {
            if (match.start() < position)
                continue;

            if (match.start() > position)
                parts.add(new TextPart(message.substring(position, match.start())));

            parts.add(match.part());
            position = match.end();
        }

        if (position < message.length() || parts.isEmpty())
            parts.add(new TextPart(message.substring(position)));

        return parts;
    }

    private static FormattedMessage join(List<FormattedMessage> parts) {
        if (parts.size() == 1)
            return parts.getFirst();

        MutableComponent forMinecraft = Component.empty();
        StringBuilder forDiscord = new StringBuilder();

        for (FormattedMessage part : parts) {
            forMinecraft.append(part.forMinecraft());
            forDiscord.append(part.forDiscord());
        }

        return new FormattedMessage(forMinecraft, forDiscord.toString());
    }
}

package com.denisnumb.discord_chat_mod.markdown;

import com.denisnumb.discord_chat_mod.discord.model.DiscordMentionData;
import com.denisnumb.discord_chat_mod.utils.ComponentUtils;
import net.minecraft.network.chat.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static com.denisnumb.discord_chat_mod.utils.ColorUtils.Color.CHAT_LINK_COLOR;

public final class MarkdownToComponentConverter {
    private MarkdownToComponentConverter() {}

    public static MutableComponent convertTokens(List<MarkdownToken> tokens) {
        return convertTokens(tokens, Map.of());
    }

    public static MutableComponent convertTokens(List<MarkdownToken> tokens, Map<String, DiscordMentionData> mentions) {
        MutableComponent result = Component.empty();

        for (MarkdownToken token : tokens)
            result.append(convertToken(token, mentions));

        return result;
    }

    public static MutableComponent convertToken(MarkdownToken token) {
        return convertToken(token, Map.of());
    }

    public static MutableComponent convertToken(MarkdownToken token, Map<String, DiscordMentionData> mentions) {
        String textPart = token.text;
        MutableComponent component;

        if (mentions.containsKey(textPart)) {
            DiscordMentionData mentionData = mentions.get(textPart);
            textPart = mentionData.prettyMention;
            component = ComponentUtils.buildGradientComponent(textPart, mentionData.colors);

            if (mentionData.memberData != null) {
                component.withStyle(style -> style
                        .withInsertion(mentionData.prettyMention)
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal(mentionData.memberData.discordName)))
                );
            }
        } else if (token.isGradient()) {
            component = ComponentUtils.buildGradientComponent(textPart, token.gradientColors);
        } else if (token.isColored()) {
            component = Component.literal(textPart).withStyle(style -> style.withColor(token.color));
        } else {
            component = Component.literal(textPart);
        }

        if (!textPart.isBlank()) {
            String finalTextPart = textPart;
            component.withStyle(style -> applyTokenStyle(style, token, finalTextPart, false));
        }

        return component;
    }

    public static Style buildStyleFromToken(MarkdownToken token, boolean skipColor) {
        return applyTokenStyle(Style.EMPTY, token, token.text, skipColor);
    }

    private static Style applyTokenStyle(Style style, MarkdownToken token, String hoverText, boolean skipColor) {
        style = style.withBold(token.bold)
                .withItalic(token.italic)
                .withStrikethrough(token.strikethrough)
                .withUnderlined(token.underlined)
                .withObfuscated(token.obfuscated);

        if (!skipColor && token.isColored())
            style = style.withColor(token.color);

        if (token.obfuscated)
            style = style.withHoverEvent(new HoverEvent.ShowText(Component.literal(hoverText)));

        if (token.isUrl()) {
            String hoverValue = token.obfuscated
                    ? String.format("%s (%s)", hoverText, token.url)
                    : token.url;
            style = style.withColor(CHAT_LINK_COLOR)
                    .withHoverEvent(new HoverEvent.ShowText(Component.literal(hoverValue)));
            try {
                style = style.withClickEvent(new ClickEvent.OpenUrl(URI.create(token.url)));
            } catch (IllegalArgumentException ignored) {}
        }

        return style;
    }
}
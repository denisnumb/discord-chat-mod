package com.denisnumb.discord_chat_mod.markdown;

import com.denisnumb.discord_chat_mod.config.configs.MinecraftChatStyleConfig;
import com.denisnumb.discord_chat_mod.discord.model.DiscordMentionData;
import com.denisnumb.discord_chat_mod.utils.ComponentUtils;
import net.minecraft.network.chat.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

public final class MarkdownToComponentConverter {
    private MarkdownToComponentConverter() {}

    public static MutableComponent convertTokens(List<MarkdownToken> tokens) {
        return convertTokens(tokens, Map.of());
    }

    public static MutableComponent convertTokens(List<MarkdownToken> tokens, Map<String, DiscordMentionData> mentions) {
        return convertTokens(tokens, mentions, Map.of());
    }

    public static MutableComponent convertTokens(List<MarkdownToken> tokens, Map<String, DiscordMentionData> mentions, Map<Character, Component> inlineComponents) {
        MutableComponent result = Component.empty();

        for (MarkdownToken token : tokens) {
            if (inlineComponents.isEmpty()) {
                result.append(convertToken(token, mentions));
                continue;
            }

            StringBuilder part = new StringBuilder();
            for (char c : token.text.toCharArray()) {
                Component inline = inlineComponents.get(c);
                if (inline == null) {
                    part.append(c);
                    continue;
                }

                appendTokenPart(result, token, part.toString(), mentions);
                part.setLength(0);
                result.append(inline.copy());
            }
            appendTokenPart(result, token, part.toString(), mentions);
        }

        return result;
    }

    private static void appendTokenPart(MutableComponent result, MarkdownToken token, String text, Map<String, DiscordMentionData> mentions) {
        if (text.equals(token.text))
            result.append(convertToken(token, mentions));
        else if (!text.isEmpty())
            result.append(convertToken(token.copyWithText(text, text), mentions));
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
        } else if (token.isUrl()) {
            component = ComponentUtils.buildGradientComponent(textPart, MinecraftChatStyleConfig.CHAT_LINK_COLORS.get());
        } else if (token.isGradient()) {
            component = ComponentUtils.buildGradientComponent(textPart, token.gradientColors);
        } else {
            component = Component.literal(textPart);
        }

        if (!textPart.isBlank()) {
            String hoverText = textPart;
            component.withStyle(style -> applyTokenStyle(style, token, hoverText));
        }

        return component;
    }

    public static Style buildStyleFromToken(MarkdownToken token) {
        return applyTokenStyle(Style.EMPTY, token, token.text);
    }

    private static Style applyTokenStyle(Style style, MarkdownToken token, String hoverText) {
        style = style.withBold(token.bold)
                .withItalic(token.italic)
                .withStrikethrough(token.strikethrough)
                .withUnderlined(token.underlined)
                .withObfuscated(token.obfuscated);

        if (!token.isUrl() && !token.isGradient() && token.isColored())
            style = style.withColor(token.color);

        if (token.obfuscated)
            style = style.withHoverEvent(new HoverEvent.ShowText(Component.literal(hoverText)));

        if (token.isUrl()) {
            String hoverValue = token.obfuscated
                    ? String.format("%s (%s)", hoverText, token.url)
                    : token.url;

            style = style.withHoverEvent(new HoverEvent.ShowText(Component.literal(hoverValue)));
            try {
                style = style.withClickEvent(new ClickEvent.OpenUrl(URI.create(token.url)));
            } catch (IllegalArgumentException ignored) {}
        }

        return style;
    }
}
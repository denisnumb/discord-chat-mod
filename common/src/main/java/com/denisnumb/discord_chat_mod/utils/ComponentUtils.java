package com.denisnumb.discord_chat_mod.utils;

import com.denisnumb.discord_chat_mod.config.ConfigProvider;
import com.denisnumb.discord_chat_mod.locale.DiscordLocaleProvider;
import com.denisnumb.discord_chat_mod.markdown.MarkdownToken;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.net.URI;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.denisnumb.discord_chat_mod.markdown.MarkdownToComponentConverter.buildStyleFromToken;
import static com.denisnumb.discord_chat_mod.utils.JavaUtils.codePointLength;
import static com.denisnumb.discord_chat_mod.utils.JavaUtils.orNull;

public final class ComponentUtils {
    private ComponentUtils() {}

    public static MutableComponent wrapInSquareBrackets(Component component) {
        return net.minecraft.network.chat.ComponentUtils.wrapInSquareBrackets(component);
    }

    public static MutableComponent applyTokenStyleToComponent(MarkdownToken placeholderToken, Component value) {
        MutableComponent base = placeholderToken.isGradient()
                ? applyGradientToComponent(value, placeholderToken.gradientColors)
                : value.copy();

        return applyStyleRecursive(base, buildStyleFromToken(placeholderToken), true);
    }

    public static MutableComponent buildUrlComponent(String text, String url, boolean addHoverEvent) {
        return buildUrlComponent(Component.literal(text), url, addHoverEvent);
    }

    public static MutableComponent buildUrlComponent(Component component, String url, boolean addHoverEvent) {
        return applyGradientToComponent(component, ConfigProvider.getConfig().minecraftChatLinkColors()).withStyle(style -> {
            if (addHoverEvent){
                style = style.withHoverEvent(new HoverEvent.ShowText(Component.literal(url)));
            }

            try {
                style = style.withClickEvent(new ClickEvent.OpenUrl(URI.create(url)));
            } catch (IllegalArgumentException ignored) {}

            return style;
        });
    }

    public static MutableComponent buildGradientComponent(String text, int[] gradientColors) {
        return applyGradientToComponent(Component.literal(text), gradientColors);
    }

    public static MutableComponent applyGradientToComponent(Component component, int[] gradientColors) {
        if (gradientColors == null || gradientColors.length == 0)
            return component.copy();

        Component resolved = resolveTranslatable(component);
        List<Component> flat = resolved.toFlatList(resolved.getStyle());

        int gradientLength = 0;
        if (gradientColors.length > 1) {
            for (Component part : flat) {
                if (part.getStyle().getColor() == null)
                    gradientLength += codePointLength(part.plainCopy().getString());
            }
        }

        boolean solidColor = gradientColors.length == 1;
        TextColor solid = solidColor ? TextColor.fromRgb(gradientColors[0]) : null;
        int segments = Math.max(gradientColors.length - 1, 1);

        MutableComponent result = Component.empty();
        int index = 0;

        for (Component part : flat) {
            String text = part.plainCopy().getString();
            if (text.isEmpty())
                continue;

            Style partStyle = part.getStyle();
            boolean hasOwnColor = partStyle.getColor() != null;
            Style visual = visualOnly(partStyle);
            Style interaction = interactionOnly(partStyle);

            if (hasOwnColor || solidColor) {
                Style color = hasOwnColor ? visual : visual.withColor(solid);
                Style finalStyle = color
                        .withHoverEvent(interaction.getHoverEvent())
                        .withClickEvent(interaction.getClickEvent())
                        .withInsertion(interaction.getInsertion());
                result.append(Component.literal(text).setStyle(finalStyle));
                continue;
            }

            MutableComponent group = Component.empty().setStyle(interaction);

            int i = 0;
            while (i < text.length()) {
                int cp = text.codePointAt(i);
                int charCount = Character.charCount(cp);
                String ch = text.substring(i, i + charCount);

                int color = ColorUtils.interpolateGradient(
                        gradientColors, segments,
                        gradientLength <= 1 ? 0.0 : (double) index / (gradientLength - 1));
                Style charStyle = visual.withColor(TextColor.fromRgb(color));
                index++;

                group.append(Component.literal(ch).setStyle(charStyle));
                i += charCount;
            }

            result.append(group);
        }

        return result;
    }

    private static MutableComponent applyStyleRecursive(MutableComponent component, Style underlay, boolean isRoot) {
        Style layer = isRoot ? underlay : visualOnly(underlay);
        Style merged = mergeStyles(component.getStyle(), layer);
        MutableComponent copy = component.plainCopy().setStyle(merged);
        for (Component sibling : component.getSiblings())
            copy.append(applyStyleRecursive(sibling.copy(), underlay, false));
        return copy;
    }

    private static Style mergeStyles(Style main, Style second) {
        return Style.EMPTY
                .withBold(orNull(main.isBold(), second.isBold()))
                .withItalic(orNull(main.isItalic(), second.isItalic()))
                .withUnderlined(orNull(main.isUnderlined(), second.isUnderlined()))
                .withStrikethrough(orNull(main.isStrikethrough(), second.isStrikethrough()))
                .withObfuscated(orNull(main.isObfuscated(), second.isObfuscated()))
                .withColor(main.getColor() != null ? main.getColor() : second.getColor())
                .withClickEvent(main.getClickEvent() != null ? main.getClickEvent() : second.getClickEvent())
                .withHoverEvent(main.getHoverEvent() != null ? main.getHoverEvent() : second.getHoverEvent())
                .withInsertion(main.getInsertion() != null ? main.getInsertion() : second.getInsertion());
    }

    private static final Pattern FORMAT_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%])");

    private static MutableComponent resolveTranslatable(Component component) {
        MutableComponent resolved = component.getContents() instanceof TranslatableContents tc
                ? formatTranslatable(DiscordLocaleProvider.getTranslate(tc.getKey()), tc.getArgs())
                : component.plainCopy();

        resolved.setStyle(component.getStyle());
        for (Component sibling : component.getSiblings())
            resolved.append(resolveTranslatable(sibling));

        return resolved;
    }

    private static MutableComponent formatTranslatable(String template, Object[] args) {
        MutableComponent result = Component.empty();
        Matcher matcher = FORMAT_PATTERN.matcher(template);

        int last = 0;
        int sequentialIndex = 0;

        while (matcher.find()) {
            if (matcher.start() > last)
                result.append(Component.literal(template.substring(last, matcher.start())));

            String positional = matcher.group(1);
            char type = matcher.group(2).charAt(0);

            if (type == '%') {
                result.append(Component.literal("%"));
            } else {
                int argIndex = positional != null
                        ? Integer.parseInt(positional) - 1
                        : sequentialIndex++;

                if (argIndex >= 0 && argIndex < args.length)
                    result.append(argToComponent(args[argIndex]));
                else
                    result.append(Component.literal(matcher.group()));
            }

            last = matcher.end();
        }

        if (last < template.length())
            result.append(Component.literal(template.substring(last)));

        return result;
    }

    private static Component argToComponent(Object arg) {
        if (arg instanceof Component c)
            return resolveTranslatable(c);
        if (arg instanceof FormattedText ft)
            return Component.literal(ft.getString());
        return Component.literal(String.valueOf(arg));
    }

    private static Style interactionOnly(Style style) {
        return Style.EMPTY
                .withHoverEvent(style.getHoverEvent())
                .withClickEvent(style.getClickEvent())
                .withInsertion(style.getInsertion());
    }

    private static Style visualOnly(Style style) {
        return style
                .withHoverEvent(null)
                .withClickEvent(null)
                .withInsertion(null);
    }
}

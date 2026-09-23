package com.denisnumb.discord_chat_mod.chat.template;

import com.denisnumb.discord_chat_mod.markdown.MarkdownParser;
import com.denisnumb.discord_chat_mod.markdown.MarkdownToComponentConverter;
import com.denisnumb.discord_chat_mod.markdown.MarkdownToken;
import com.denisnumb.discord_chat_mod.utils.ComponentUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.denisnumb.discord_chat_mod.chat.template.TemplateParameters.Translatable.unwrapBraces;
import static com.denisnumb.discord_chat_mod.chat.template.TemplateParameterFactory.buildTimestampParameters;
import static com.denisnumb.discord_chat_mod.utils.JavaUtils.mergeMaps;

public final class TemplateFactory {
    private TemplateFactory() {}

    public static Component getStyledTranslatableMessage(
            List<MarkdownToken> template,
            String translatableParam,
            LinkedHashMap<String, Component> placeholderComponents,
            Map<String, Component> extraParams
    ) {
        if (!templateContains(template, translatableParam))
            return applyParametersToTemplate(template, mergeMaps(placeholderComponents, extraParams));

        String[] placeholderNames = placeholderComponents.keySet().toArray(new String[0]);
        Map<String, MarkdownToken> paramTokens = parseTemplateParameterTokens(template, placeholderNames);

        Object[] translatableArgs = placeholderComponents.entrySet().stream()
                .map(entry -> {
                    MarkdownToken token = paramTokens.get(entry.getKey());
                    Component value = entry.getValue();
                    return token == null
                            ? value.copy()
                            : ComponentUtils.applyTokenStyleToComponent(token, value);
                })
                .toArray();

        Component translatableContent = Component.translatable(unwrapBraces(translatableParam), translatableArgs);
        List<MarkdownToken> cleanedTemplate = removeParametersFromTemplate(template, placeholderNames);
        Map<String, Component> allParams = mergeMaps(Map.of(translatableParam, translatableContent), extraParams);

        return applyParametersToTemplate(cleanedTemplate, allParams);
    }

    public static Map<String, MarkdownToken> parseTemplateParameterTokens(List<MarkdownToken> template, String... parameters) {
        Map<String, MarkdownToken> result = new HashMap<>();
        Pattern pattern = buildOrPattern(parameters);

        for (MarkdownToken token : template) {
            Matcher matcher = pattern.matcher(token.text);
            while (matcher.find())
                result.put(matcher.group(), token);
        }
        return result;
    }

    public static List<MarkdownToken> parseConfigTemplateMarkdown(String configTemplate) {
        return MarkdownParser.parseMarkdown(configTemplate);
    }

    public static MutableComponent applyParametersToTemplate(
            List<MarkdownToken> template,
            Map<String, Component> parameterToComponent
    ) {
        parameterToComponent = mergeMaps(parameterToComponent, buildTimestampParameters());

        MutableComponent result = Component.empty();
        Pattern pattern = buildOrPattern(parameterToComponent.keySet().toArray(new String[0]));

        for (MarkdownToken token : template) {
            String text = token.text;
            Matcher matcher = pattern.matcher(text);

            if (!matcher.find()) {
                result.append(MarkdownToComponentConverter.convertToken(token));
                continue;
            }

            int currentPos = 0;
            do {
                String before = text.substring(currentPos, matcher.start());
                if (!before.isEmpty())
                    result.append(MarkdownToComponentConverter.convertToken(token.copyWithText(before)));

                Component value = parameterToComponent.get(matcher.group());
                if (value != null)
                    result.append(ComponentUtils.applyTokenStyleToComponent(token, value));

                currentPos = matcher.end();
            } while (matcher.find());

            if (currentPos < text.length())
                result.append(MarkdownToComponentConverter.convertToken(token.copyWithText(text.substring(currentPos))));
        }

        return result;
    }

    private static List<MarkdownToken> removeParametersFromTemplate(List<MarkdownToken> template, String... parameters) {
        Pattern pattern = buildOrPattern(parameters);
        List<MarkdownToken> result = new ArrayList<>();
        boolean pendingSpaceTrim = false;

        for (MarkdownToken token : template) {
            boolean hadPlaceholder = pattern.matcher(token.text).find();
            String cleaned = pattern.matcher(token.text).replaceAll("");

            if (hadPlaceholder) {
                cleaned = cleaned.stripLeading();
                pendingSpaceTrim = true;
            } else if (pendingSpaceTrim) {
                cleaned = cleaned.stripLeading();
                pendingSpaceTrim = false;
            }

            if (!cleaned.isEmpty())
                result.add(token.copyWithText(cleaned));
        }
        return result;
    }

    private static boolean templateContains(List<MarkdownToken> template, String param) {
        return template.stream().anyMatch(t -> t.text.contains(param));
    }

    private static Pattern buildOrPattern(String... parameters) {
        return Pattern.compile(Arrays.stream(parameters)
                .map(Pattern::quote)
                .collect(Collectors.joining("|")));
    }
}

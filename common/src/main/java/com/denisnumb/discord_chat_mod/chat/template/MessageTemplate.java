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


public final class MessageTemplate<T extends MessageType> {
    private final String rawTemplate;
    private final List<MarkdownToken> tokens;
    private final Pattern availableParametersPattern;
    private final TemplateParameter.Translatable translatableParam;

    public MessageTemplate(String rawTemplate, T type) {
        this.rawTemplate = rawTemplate;

        String markdownSafeParametersTemplate = replaceMarkdownUnsafeParameters(rawTemplate);
        this.tokens = MarkdownParser.parseMarkdown(markdownSafeParametersTemplate);

        Set<TemplatePlaceholder> presentPlaceholders = scanPresentPlaceholders(markdownSafeParametersTemplate);
        this.availableParametersPattern = buildOrPattern(
                presentPlaceholders.stream()
                        .filter(type.availableParameters()::contains)
                        .toList()
        );

        this.translatableParam = type.translatable()
                .filter(presentPlaceholders::contains)
                .orElse(null);
    }

    public String getRawTemplate() {
        return rawTemplate;
    }

    public Component applyParameters(MessageType.MessageParamsBuilder<T> params) {
        return translatableParam != null
                ? renderTranslatable(translatableParam, params.build())
                : renderPlain(tokens, availableParametersPattern, params.build());
    }

    private Component renderTranslatable(
            TemplateParameter.Translatable translatableParam,
            Map<TemplatePlaceholder, Component> parameterToComponent
    ) {
        List<TemplatePlaceholder> args = translatableParam.getOrderedArgs();
        Map<TemplatePlaceholder, MarkdownToken> argsStyles = parseParameterStyles(args);

        Object[] translatableArgs = args.stream()
                .map(arg -> {
                    MarkdownToken style = argsStyles.get(arg);
                    Component value = parameterToComponent.get(arg);
                    return style == null
                            ? value.copy()
                            : ComponentUtils.applyTokenStyleToComponent(style, value);
                })
                .toArray();

        Component translatableContent = Component.translatable(translatableParam.translationKey(), translatableArgs);
        List<MarkdownToken> cleanedTemplate = removeParametersFromTemplate(args);

        Map<TemplatePlaceholder, Component> remainingParams = new HashMap<>(parameterToComponent);
        args.forEach(remainingParams::remove);
        remainingParams.put(translatableParam, translatableContent);

        Pattern cleanedPattern = buildOrPattern(remainingParams.keySet());
        return renderPlain(cleanedTemplate, cleanedPattern, remainingParams);
    }

    private static MutableComponent renderPlain(
            List<MarkdownToken> templateTokens,
            Pattern pattern,
            Map<TemplatePlaceholder, Component> parameterToComponent
    ) {
        MutableComponent result = Component.empty();

        for (MarkdownToken token : templateTokens) {
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

                Component value = parameterToComponent.get(TemplateParameter.fromString(matcher.group()));
                if (value != null)
                    result.append(ComponentUtils.applyTokenStyleToComponent(token, value));

                currentPos = matcher.end();
            } while (matcher.find());

            if (currentPos < text.length())
                result.append(MarkdownToComponentConverter.convertToken(token.copyWithText(text.substring(currentPos))));
        }

        return result;
    }

    public Map<TemplatePlaceholder, MarkdownToken> parseParameterStyles(Collection<TemplatePlaceholder> parameters) {
        Map<TemplatePlaceholder, MarkdownToken> result = new HashMap<>();
        Pattern pattern = buildOrPattern(parameters);

        for (MarkdownToken token : tokens) {
            Matcher matcher = pattern.matcher(token.text);
            while (matcher.find())
                result.put(TemplateParameter.fromString(matcher.group()), token);
        }
        return result;
    }

    private List<MarkdownToken> removeParametersFromTemplate(Collection<TemplatePlaceholder> parameters) {
        Pattern pattern = buildOrPattern(parameters);
        List<MarkdownToken> result = new ArrayList<>();
        boolean pendingSpaceTrim = false;

        for (MarkdownToken token : tokens) {
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

    private static Pattern buildOrPattern(Collection<? extends TemplatePlaceholder> parameters) {
        if (parameters.isEmpty())
            return Pattern.compile("(?!)");

        return Pattern.compile(parameters.stream()
                .map(parameter -> Pattern.quote(parameter.getMarkdownSafePlaceholder()))
                .collect(Collectors.joining("|")));
    }

    private static Set<TemplatePlaceholder> scanPresentPlaceholders(String rawTemplate) {
        Set<TemplatePlaceholder> result = new HashSet<>();
        for (TemplateParameter placeholder : TemplateParameter.values())
            if (rawTemplate.contains(placeholder.getMarkdownSafePlaceholder()))
                result.add(placeholder);
        for (TemplateParameter.Translatable placeholder : TemplateParameter.Translatable.values())
            if (rawTemplate.contains(placeholder.getMarkdownSafePlaceholder()))
                result.add(placeholder);

        return result;
    }

    private static String replaceMarkdownUnsafeParameters(String rawTemplate) {
        String markdownSafeTemplate = rawTemplate;
        for (TemplatePlaceholder param : TemplateParameter.values())
            if (!param.getPlaceholder().equals(param.getMarkdownSafePlaceholder()))
                markdownSafeTemplate = markdownSafeTemplate.replace(param.getPlaceholder(), param.getMarkdownSafePlaceholder());

        for (TemplatePlaceholder param : TemplateParameter.Translatable.values())
            if (!param.getPlaceholder().equals(param.getMarkdownSafePlaceholder()))
                markdownSafeTemplate = markdownSafeTemplate.replace(param.getPlaceholder(), param.getMarkdownSafePlaceholder());

        return markdownSafeTemplate;
    }
}
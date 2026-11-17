package com.denisnumb.discord_chat_mod.chat.placeholder;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Finds and replaces {@link ChatPlaceholder}s in a message text.
 */
public final class ChatPlaceholders {
    private ChatPlaceholders() {}

    /**
     * Group 1: the space before the placeholder <p>
     * Group 2: the escape character <p>
     * Group 3: the placeholder token
     */
    private static final Pattern PATTERN = Pattern.compile(
            "( ?)(\\\\)?(" + Arrays.stream(ChatPlaceholder.values())
                    .map(placeholder -> Pattern.quote(placeholder.token()))
                    .collect(Collectors.joining("|")) + ")"
    );

    private static final Map<String, ChatPlaceholder> BY_TOKEN = Arrays.stream(ChatPlaceholder.values())
            .collect(Collectors.toMap(ChatPlaceholder::token, Function.identity()));

    public static Set<ChatPlaceholder> find(String text) {
        Set<ChatPlaceholder> result = EnumSet.noneOf(ChatPlaceholder.class);
        Matcher matcher = PATTERN.matcher(text);
        while (matcher.find())
            if (matcher.group(2) == null)
                result.add(BY_TOKEN.get(matcher.group(3)));
        return result;
    }

    /**
     * Text for the markdown parser: the {@code resolved} placeholders are replaced with their {@link ChatPlaceholder#marker()},
     * the escaped ones become plain text, the others stay as is
     */
    public static String markForMinecraft(String text, Set<ChatPlaceholder> resolved) {
        String withoutMarkers = text.chars()
                .filter(c -> !ChatPlaceholder.isMarker((char) c))
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return replace(withoutMarkers, placeholder -> resolved.contains(placeholder)
                ? String.valueOf(placeholder.marker())
                : null
        );
    }

    public static String replaceForDiscord(String text, Map<ChatPlaceholder, String> values) {
        return replace(text, values::get);
    }

    /**
     * @param valueOf the value of a placeholder, or null to keep it as is
     */
    private static String replace(String text, Function<ChatPlaceholder, String> valueOf) {
        boolean[] removed = {false};

        String result = PATTERN.matcher(text).replaceAll(match -> {
            String space = match.group(1);
            String token = match.group(3);
            String value = match.group(2) == null ? valueOf.apply(BY_TOKEN.get(token)) : null;

            if (value == null)
                return Matcher.quoteReplacement(space + token);

            if (value.isEmpty()) {
                removed[0] = true;
                return "";
            }

            return Matcher.quoteReplacement(space + value);
        });

        return removed[0] ? result.strip() : result;
    }
}

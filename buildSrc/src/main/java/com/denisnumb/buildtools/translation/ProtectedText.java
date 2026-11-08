package com.denisnumb.buildtools.translation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ProtectedText {
    private static final Pattern PROTECTED = Pattern.compile(String.join("|",
            "`[^`\\n]*`",               // inline code: `%s`, `/cmd`
            "%(?:\\d+\\$)?(?:\\.\\d+)?[sdf]",      // format placeholders: %s, %d, %1$s, %.2f
            "%%",                                  // escaped percent
            "§[0-9a-fk-orA-FK-OR]",                // minecraft formatting codes
            "\\{[^{}\\n]*}",                       // literal braces
            "<[^<>\\n]+>",                         // discord mentions/emojis <@id>, <:name:id>
            "https?://\\S+",                       // urls
            ":[A-Za-z0-9_+-]+:",                   // emoji shortcodes :name:
            "\\*+", "~~", "\\|\\|", "__"           // markdown markers
    ));
    private static final Pattern LETTER = Pattern.compile("\\p{L}");

    private final List<TextSegment> segments;
    private final List<String> tokens;

    private ProtectedText(List<TextSegment> segments, List<String> tokens) {
        this.segments = Collections.unmodifiableList(segments);
        this.tokens = Collections.unmodifiableList(tokens);
    }

    public static ProtectedText of(String text) {
        List<TextSegment> segments = new ArrayList<>();
        List<String> tokens = new ArrayList<>();
        Matcher m = PROTECTED.matcher(text);
        int last = 0;
        while (m.find()) {
            if (m.start() > last)
                segments.add(TextSegment.text(text.substring(last, m.start())));
            segments.add(TextSegment.token(tokens.size()));
            tokens.add(m.group());
            last = m.end();
        }
        if (last < text.length())
            segments.add(TextSegment.text(text.substring(last)));
        return new ProtectedText(segments, tokens);
    }

    public List<TextSegment> segments() {
        return segments;
    }

    public boolean isTranslatable() {
        for (TextSegment segment : segments) {
            if (!segment.isToken() && LETTER.matcher(segment.text()).find())
                return true;
        }
        return false;
    }

    public String original() {
        return restoreUnchecked(segments);
    }

    public String restore(List<TextSegment> translated) throws TranslationException {
        int[] seen = new int[tokens.size()];
        for (TextSegment segment : translated) {
            if (!segment.isToken())
                continue;
            if (segment.token() >= tokens.size())
                throw new TranslationException("translator produced unknown placeholder #" + segment.token());
            seen[segment.token()]++;
        }
        for (int i = 0; i < seen.length; i++) {
            if (seen[i] != 1) {
                throw new TranslationException("fragment " + tokens.get(i) + " was "
                        + (seen[i] == 0 ? "lost" : "duplicated") + " by the translator");
            }
        }
        String result = restoreUnchecked(translated);
        if (result.isBlank())
            throw new TranslationException("translator returned an empty string");
        return result;
    }

    private String restoreUnchecked(List<TextSegment> list) {
        StringBuilder sb = new StringBuilder();
        for (TextSegment segment : list)
            sb.append(segment.isToken() ? tokens.get(segment.token()) : segment.text());
        return sb.toString();
    }
}

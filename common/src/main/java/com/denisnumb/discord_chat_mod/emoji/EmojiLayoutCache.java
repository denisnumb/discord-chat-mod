package com.denisnumb.discord_chat_mod.emoji;

import com.denisnumb.discord_chat_mod.chat_images.model.AbstractImage;
import com.denisnumb.discord_chat_mod.config.configs.ClientConfig;
import com.denisnumb.discord_chat_mod.discord.data_providers.CustomEmojiProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;

import static com.denisnumb.discord_chat_mod.emoji.EmojiUtils.EMOJI_PATTERN;

public final class EmojiLayoutCache {
    private EmojiLayoutCache() {}

    public static final int EMOJI_WIDTH = 9;
    private static final int MAX_STRING_CACHE = 512;

    private static final AtomicInteger version = new AtomicInteger();
    private static int appliedVersion = 0;
    private static boolean computing = false;
    private static volatile boolean disabled = false;

    private static final Map<FormattedCharSequence, EmojiLayout> SEQUENCE_CACHE = new WeakHashMap<>();

    private static final Map<String, String> STRING_CACHE =
            new LinkedHashMap<>(64, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                    return size() > MAX_STRING_CACHE;
                }
            };

    public static void invalidate() {
        version.incrementAndGet();
    }

    public static void onClientConfigChanged() {
        disabled = ClientConfig.EMOJIFUL_COMPATIBILITY.get();
        invalidate();
    }

    private static boolean isInactive() {
        return disabled || (CustomEmojiProvider.CLIENT_EMOJI_CACHE.isEmpty() && !UnicodeEmojis.isAvailable());
    }

    private static void syncVersion() {
        int v = version.get();
        if (v != appliedVersion) {
            appliedVersion = v;
            SEQUENCE_CACHE.clear();
            STRING_CACHE.clear();
        }
    }

    public static EmojiLayout get(FormattedCharSequence text) {
        return get(Minecraft.getInstance().font, text);
    }

    public static EmojiLayout get(Font font, FormattedCharSequence text) {
        if (text == null || text == FormattedCharSequence.EMPTY || computing || isInactive())
            return EmojiLayout.NONE;

        syncVersion();

        EmojiLayout cached = SEQUENCE_CACHE.get(text);
        if (cached != null)
            return cached;

        if (!new PreScan().mayContainEmoji(text))
            return EmojiLayout.NONE;

        EmojiLayout layout;
        computing = true;
        try {
            layout = compute(font, text);
        } finally {
            computing = false;
        }

        SEQUENCE_CACHE.put(text, layout);
        return layout;
    }

    public static FormattedCharSequence replacedOrSelf(FormattedCharSequence text) {
        EmojiLayout layout = get(text);
        return layout.isEmpty() ? text : layout.replaced();
    }

    public static String replaceForWidth(String text) {
        if (text == null || isInactive() || (text.indexOf(':') < 0 && !UnicodeEmojis.mayNeedGlyphs(text)))
            return text;

        syncVersion();

        String cached = STRING_CACHE.get(text);
        if (cached != null)
            return cached;

        String result = computeString(text);
        STRING_CACHE.put(text, result);
        return result;
    }

    private static final class PreScan implements FormattedCharSink {
        private int run = -1;
        private boolean found;

        boolean mayContainEmoji(FormattedCharSequence text) {
            text.accept(this);
            return found;
        }

        @Override
        public boolean accept(int index, @NotNull Style style, int cp) {
            if (UnicodeEmojis.isSequenceCandidate(cp)) {
                found = true;
            } else if (cp == ':') {
                found = run >= 2;
                run = 0;
            } else if (run >= 0 && (cp == '_' || cp == '~' || (cp < 128 && Character.isLetterOrDigit(cp)))) {
                run++;
            } else {
                run = -1;
            }
            return !found;
        }
    }

    private record Replacement(int start, int end, @Nullable AbstractImage image, int glyph) {}

    private static EmojiLayout compute(Font font, FormattedCharSequence text) {
        IntList cps = new IntList();
        IntList charStart = new IntList();
        List<Style> styles = new ArrayList<>();
        StringBuilder raw = new StringBuilder();
        boolean[] hasColon = {false};
        boolean[] hasSequence = {false};

        text.accept((idx, style, cp) -> {
            charStart.add(raw.length());
            raw.appendCodePoint(cp);
            cps.add(cp);
            styles.add(style);
            if (cp == ':')
                hasColon[0] = true;
            else if (!hasSequence[0] && UnicodeEmojis.isSequenceCandidate(cp))
                hasSequence[0] = true;
            return true;
        });

        boolean unicodeAvailable = UnicodeEmojis.isAvailable();
        List<Replacement> replacements = hasColon[0] ? findEmojiCodes(raw, charStart, unicodeAvailable) : List.of();
        if (hasSequence[0] && unicodeAvailable)
            replacements = addSequenceGlyphs(cps.toArray(), replacements);

        if (replacements.isEmpty())
            return EmojiLayout.NONE;

        boolean hasImages = replacements.stream().anyMatch(r -> r.image() != null);
        List<FormattedCharSequence> parts = new ArrayList<>();
        List<EmojiLayout.Slot> slots = new ArrayList<>();
        int xOffset = 0;
        int lastCp = 0;

        for (Replacement replacement : replacements) {
            if (replacement.start() > lastCp) {
                FormattedCharSequence before = slice(cps, styles, lastCp, replacement.start());
                parts.add(before);
                if (hasImages)
                    xOffset += font.width(before);
            }

            if (replacement.image() != null) {
                slots.add(new EmojiLayout.Slot(xOffset, replacement.image()));
                parts.add(FormattedCharSequence.codepoint(' ', Style.EMPTY));
                parts.add(FormattedCharSequence.codepoint(' ', Style.EMPTY.withBold(true)));
                xOffset += EMOJI_WIDTH;
            } else {
                FormattedCharSequence glyph = FormattedCharSequence.codepoint(replacement.glyph(), styles.get(replacement.start()));
                parts.add(glyph);
                if (hasImages)
                    xOffset += font.width(glyph);
            }

            lastCp = replacement.end();
        }

        if (lastCp < cps.size())
            parts.add(slice(cps, styles, lastCp, cps.size()));

        return new EmojiLayout(FormattedCharSequence.composite(parts), List.copyOf(slots));
    }

    private static List<Replacement> findEmojiCodes(CharSequence raw, IntList charStart, boolean unicodeAvailable) {
        boolean customAvailable = !CustomEmojiProvider.CLIENT_EMOJI_CACHE.isEmpty();
        if (!customAvailable && !unicodeAvailable)
            return List.of();

        List<Replacement> replacements = new ArrayList<>();
        Matcher matcher = EMOJI_PATTERN.matcher(raw);
        while (matcher.find()) {
            String name = matcher.group(1);
            int startCp = charToCp(charStart, matcher.start());
            int endCp = charToCp(charStart, matcher.end());

            AbstractImage image = CustomEmojiProvider.CLIENT_EMOJI_CACHE.get(name);
            if (image != null) {
                replacements.add(new Replacement(startCp, endCp, image, 0));
                continue;
            }

            UnicodeEmojis.Entry unicode = unicodeAvailable && matcher.group(2) == null ? UnicodeEmojis.byName(name) : null;
            if (unicode != null)
                replacements.add(new Replacement(startCp, endCp, null, unicode.glyph()));
        }
        return replacements;
    }

    private static List<Replacement> addSequenceGlyphs(int[] cps, List<Replacement> codes) {
        List<Replacement> result = new ArrayList<>(codes.size() + 4);
        int next = 0;
        int i = 0;
        while (i < cps.length) {
            if (next < codes.size() && i >= codes.get(next).start()) {
                Replacement code = codes.get(next++);
                result.add(code);
                i = code.end();
                continue;
            }

            int limit = next < codes.size() ? codes.get(next).start() : cps.length;
            UnicodeEmojis.Match match = UnicodeEmojis.matchAt(cps, i, limit);
            if (match == null) {
                i++;
                continue;
            }
            if (match.needsGlyph(cps))
                result.add(new Replacement(match.start(), match.end(), null, match.entry().glyph()));
            i = match.end();
        }
        return result;
    }

    private static String computeString(String text) {
        boolean customAvailable = !CustomEmojiProvider.CLIENT_EMOJI_CACHE.isEmpty();
        boolean unicodeAvailable = UnicodeEmojis.isAvailable();
        String result = text;

        if (text.indexOf(':') >= 0 && (customAvailable || unicodeAvailable)) {
            Matcher matcher = EMOJI_PATTERN.matcher(text);
            if (matcher.find()) {
                StringBuilder sb = new StringBuilder(text.length());
                int last = 0;
                do {
                    sb.append(text, last, matcher.start());
                    UnicodeEmojis.Entry unicode;
                    if (CustomEmojiProvider.CLIENT_EMOJI_CACHE.get(matcher.group(1)) != null)
                        sb.append("  ");
                    else if (unicodeAvailable && matcher.group(2) == null && (unicode = UnicodeEmojis.byName(matcher.group(1))) != null)
                        sb.append(unicode.glyphText());
                    else
                        sb.append(text, matcher.start(), matcher.end());
                    last = matcher.end();
                } while (matcher.find());
                sb.append(text, last, text.length());
                result = sb.toString();
            }
        }

        return unicodeAvailable ? UnicodeEmojis.replaceWithGlyphs(result) : result;
    }

    private static FormattedCharSequence slice(IntList cps, List<Style> styles, int from, int to) {
        List<FormattedCharSequence> out = new ArrayList<>();
        int i = from;
        while (i < to) {
            Style style = styles.get(i);
            StringBuilder run = new StringBuilder();
            int j = i;
            while (j < to && styles.get(j).equals(style)) {
                run.appendCodePoint(cps.get(j));
                j++;
            }
            out.add(FormattedCharSequence.forward(run.toString(), style));
            i = j;
        }
        return out.size() == 1 ? out.getFirst() : FormattedCharSequence.composite(out);
    }

    private static int charToCp(IntList charStart, int charIndex) {
        int lo = 0, hi = charStart.size();
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (charStart.get(mid) < charIndex) lo = mid + 1;
            else hi = mid;
        }

        return lo;
    }

    private static final class IntList {
        private int[] data = new int[32];
        private int size;

        void add(int v) {
            if (size == data.length)
                data = Arrays.copyOf(data, size * 2);
            data[size++] = v;
        }

        int get(int i) {
            return data[i];
        }

        int size() {
            return size;
        }

        int[] toArray() {
            return Arrays.copyOf(data, size);
        }
    }
}
package com.denisnumb.discord_chat_mod.emoji;

import com.denisnumb.discord_chat_mod.chat_images.model.AbstractImage;
import com.denisnumb.discord_chat_mod.config.configs.ClientConfig;
import com.denisnumb.discord_chat_mod.discord.data_providers.CustomEmojiProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

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
        return disabled || CustomEmojiProvider.CLIENT_EMOJI_CACHE.isEmpty();
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
        if (text == null || text.indexOf(':') < 0 || isInactive())
            return text;

        syncVersion();

        String cached = STRING_CACHE.get(text);
        if (cached != null)
            return cached;

        String result = computeString(text);
        STRING_CACHE.put(text, result);
        return result;
    }

    private static EmojiLayout compute(Font font, FormattedCharSequence text) {
        IntList cps = new IntList();
        IntList charStart = new IntList();
        List<Style> styles = new ArrayList<>();
        StringBuilder raw = new StringBuilder();
        boolean[] hasColon = {false};

        text.accept((idx, style, cp) -> {
            charStart.add(raw.length());
            raw.appendCodePoint(cp);
            cps.add(cp);
            styles.add(style);
            if (cp == ':')
                hasColon[0] = true;
            return true;
        });

        if (!hasColon[0])
            return EmojiLayout.NONE;

        Matcher matcher = EMOJI_PATTERN.matcher(raw);
        if (!matcher.find())
            return EmojiLayout.NONE;

        List<FormattedCharSequence> parts = new ArrayList<>();
        List<EmojiLayout.Slot> slots = new ArrayList<>();
        int xOffset = 0;
        int lastCp = 0;

        do {
            AbstractImage image = CustomEmojiProvider.CLIENT_EMOJI_CACHE.get(matcher.group(1));
            if (image == null)
                continue;

            int startCp = charToCp(charStart, matcher.start());
            int endCp = charToCp(charStart, matcher.end());

            if (startCp > lastCp) {
                FormattedCharSequence before = slice(cps, styles, lastCp, startCp);
                parts.add(before);
                xOffset += font.width(before);
            }

            slots.add(new EmojiLayout.Slot(xOffset, image));

            parts.add(FormattedCharSequence.codepoint(' ', Style.EMPTY));
            parts.add(FormattedCharSequence.codepoint(' ', Style.EMPTY.withBold(true)));
            xOffset += EMOJI_WIDTH;

            lastCp = endCp;
        } while (matcher.find());

        if (slots.isEmpty())
            return EmojiLayout.NONE;

        if (lastCp < cps.size())
            parts.add(slice(cps, styles, lastCp, cps.size()));

        return new EmojiLayout(FormattedCharSequence.composite(parts), List.copyOf(slots));
    }

    private static String computeString(String text) {
        Matcher matcher = EMOJI_PATTERN.matcher(text);
        if (!matcher.find())
            return text;

        StringBuilder sb = new StringBuilder(text.length());
        int last = 0;
        do {
            sb.append(text, last, matcher.start());
            if (CustomEmojiProvider.CLIENT_EMOJI_CACHE.get(matcher.group(1)) != null)
                sb.append("  ");
            else
                sb.append(text, matcher.start(), matcher.end());
            last = matcher.end();
        } while (matcher.find());
        sb.append(text, last, text.length());
        return sb.toString();
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
    }
}
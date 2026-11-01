package com.denisnumb.discord_chat_mod.emoji;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class UnicodeEmojis {
    private UnicodeEmojis() {
    }

    public static final String DATA_RESOURCE = "/assets/discord_chat_mod/emoji/emoji_data.json";

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int SUPPORTED_FORMAT = 1;
    private static final int VARIATION_SELECTOR_16 = 0xFE0F;
    private static final int ZERO_WIDTH_JOINER = 0x200D;
    private static final int COMBINING_ENCLOSING_KEYCAP = 0x20E3;

    public record Entry(
            String emoji,
            List<String> names,
            @Nullable String category,
            List<String> keywords,
            boolean skinTone,
            int glyph
    ) {
        public String name() {
            return names.getFirst();
        }

        public String glyphText() {
            return Character.toString(glyph);
        }
    }

    public record Match(Entry entry, int start, int end) {
        public boolean needsGlyph(int[] codePoints) {
            return end - start != 1 || codePoints[start] != entry.glyph();
        }
    }

    private static final class Node {
        final Map<Integer, Node> children = new HashMap<>(4);
        @Nullable Entry entry;
    }

    private record Data(
            List<String> categories,
            List<Entry> all,
            Map<String, List<Entry>> byCategory,
            Map<String, Entry> byName,
            Map<String, Entry> byKey,
            Node trie
    ) {
        static final Data EMPTY = new Data(List.of(), List.of(), Map.of(), Map.of(), Map.of(), new Node());
    }

    private static volatile Data data;

    public static void preloadAsync() {
        Thread thread = new Thread(UnicodeEmojis::data, "DiscordChatMod emoji data loader");
        thread.setDaemon(true);
        thread.start();
    }

    private static Data data() {
        Data current = data;
        if (current == null) {
            synchronized (UnicodeEmojis.class) {
                current = data;
                if (current == null)
                    data = current = load();
            }
        }
        return current;
    }

    private static Data load() {
        try (InputStream stream = UnicodeEmojis.class.getResourceAsStream(DATA_RESOURCE)) {
            if (stream == null) {
                LOGGER.warn("{} not found: unicode emoji names, the emoji picker's standard emoji and composite emoji rendering are disabled", DATA_RESOURCE);
                return Data.EMPTY;
            }

            JsonObject root = new Gson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
            int format = root.get("format").getAsInt();
            if (format != SUPPORTED_FORMAT) {
                LOGGER.error("{} has format {}, but this version of the mod supports format {}", DATA_RESOURCE, format, SUPPORTED_FORMAT);
                return Data.EMPTY;
            }

            List<String> categories = new ArrayList<>();
            for (JsonElement category : root.getAsJsonArray("categories"))
                categories.add(category.getAsString());

            List<Entry> all = new ArrayList<>();
            Map<String, List<Entry>> byCategory = new LinkedHashMap<>();
            categories.forEach(category -> byCategory.put(category, new ArrayList<>()));
            Map<String, Entry> byName = new HashMap<>();
            Map<String, Entry> byKey = new HashMap<>();
            Node trie = new Node();

            for (JsonElement element : root.getAsJsonArray("emoji")) {
                Entry entry = parseEntry(element.getAsJsonObject());
                if (entry == null)
                    continue;

                String key = stripVariationSelectors(entry.emoji());
                if (byKey.putIfAbsent(key, entry) != null)
                    continue;

                all.add(entry);
                for (String name : entry.names())
                    byName.putIfAbsent(name, entry);
                if (!entry.skinTone() && entry.category() != null)
                    byCategory.computeIfAbsent(entry.category(), c -> new ArrayList<>()).add(entry);

                int[] sequence = key.codePoints().toArray();
                if (sequence.length == 1 && sequence[0] != entry.glyph())
                    sequence = new int[]{sequence[0], VARIATION_SELECTOR_16};
                insert(trie, sequence, entry);
            }

            Map<String, List<Entry>> frozenCategories = new LinkedHashMap<>();
            byCategory.forEach((category, entries) -> frozenCategories.put(category, List.copyOf(entries)));

            LOGGER.info("Loaded {} unicode emoji from {}", all.size(), DATA_RESOURCE);
            return new Data(List.copyOf(categories), List.copyOf(all), frozenCategories, byName, byKey, trie);
        } catch (Exception e) {
            LOGGER.error("Failed to load {}", DATA_RESOURCE, e);
            return Data.EMPTY;
        }
    }

    @Nullable
    private static Entry parseEntry(JsonObject json) {
        String emoji = json.get("e").getAsString();
        List<String> names = strings(json.getAsJsonArray("n"));
        if (emoji.isEmpty() || names.isEmpty())
            return null;

        int glyph;
        if (json.has("g")) {
            glyph = Integer.parseInt(json.get("g").getAsString(), 16);
        } else {
            int[] codePoints = stripVariationSelectors(emoji).codePoints().toArray();
            if (codePoints.length != 1) {
                LOGGER.warn("Emoji :{}: has no glyph and is not a single code point, skipped", names.getFirst());
                return null;
            }
            glyph = codePoints[0];
        }

        boolean skinTone = json.has("t") && json.get("t").getAsInt() == 1;
        String category = json.has("c") ? json.get("c").getAsString() : null;
        List<String> keywords = json.has("k") ? strings(json.getAsJsonArray("k")) : List.of();
        return new Entry(emoji, names, category, keywords, skinTone, glyph);
    }

    private static List<String> strings(JsonArray array) {
        List<String> result = new ArrayList<>(array.size());
        for (JsonElement element : array)
            result.add(element.getAsString());
        return List.copyOf(result);
    }

    private static void insert(Node root, int[] sequence, Entry entry) {
        Node node = root;
        for (int codePoint : sequence)
            node = node.children.computeIfAbsent(codePoint, cp -> new Node());
        if (node.entry == null)
            node.entry = entry;
    }

    public static boolean isAvailable() {
        return !data().all().isEmpty();
    }

    public static List<String> categories() {
        return data().categories();
    }

    public static List<Entry> category(String id) {
        return data().byCategory().getOrDefault(id, List.of());
    }

    public static List<Entry> all() {
        return data().all();
    }

    @Nullable
    public static Entry byName(String name) {
        return data().byName().get(name);
    }

    @Nullable
    public static Entry byEmoji(String text) {
        return data().byKey().get(stripVariationSelectors(text));
    }

    public static String glyphFor(String emoji) {
        Entry entry = byEmoji(emoji);
        return entry == null ? emoji : entry.glyphText();
    }

    public static String stripVariationSelectors(String text) {
        return text.indexOf(VARIATION_SELECTOR_16) < 0 ? text : text.replace("️", "");
    }

    public static List<Entry> search(String query, boolean useKeywords, boolean includeSkinTones) {
        String q = query.trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty())
            return List.of();

        record Scored(Entry entry, int score, int order) {
        }
        List<Scored> scored = new ArrayList<>();
        List<Entry> all = data().all();
        for (int i = 0; i < all.size(); i++) {
            Entry entry = all.get(i);
            if (entry.skinTone() && !includeSkinTones)
                continue;
            int score = score(entry, q, useKeywords);
            if (score >= 0)
                scored.add(new Scored(entry, score, i));
        }

        scored.sort(Comparator.comparingInt(Scored::score).thenComparingInt(Scored::order));
        return scored.stream().map(Scored::entry).toList();
    }

    private static int score(Entry entry, String q, boolean useKeywords) {
        int best = -1;
        for (String name : entry.names()) {
            int s = name.equals(q) ? 0 : name.startsWith(q) ? 1 : name.contains(q) ? 2 : -1;
            if (s >= 0 && (best < 0 || s < best))
                best = s;
        }
        if (best >= 0 || !useKeywords)
            return best;

        for (String keyword : entry.keywords()) {
            int s = keyword.startsWith(q) ? 3 : keyword.contains(q) ? 4 : -1;
            if (s >= 0 && (best < 0 || s < best))
                best = s;
        }
        return best;
    }

    public static boolean isSequenceCandidate(int codePoint) {
        return codePoint >= 0x10000
                || codePoint == ZERO_WIDTH_JOINER
                || codePoint == COMBINING_ENCLOSING_KEYCAP
                || codePoint == VARIATION_SELECTOR_16;
    }

    public static boolean mayNeedGlyphs(String text) {
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (Character.isSurrogate(ch) || ch == ZERO_WIDTH_JOINER || ch == COMBINING_ENCLOSING_KEYCAP || ch == VARIATION_SELECTOR_16)
                return true;
        }
        return false;
    }

    @Nullable
    public static Match matchAt(int[] codePoints, int start, int end) {
        if (start >= end || codePoints[start] == VARIATION_SELECTOR_16)
            return null;

        Node first = data().trie().children.get(codePoints[start]);
        if (first == null)
            return null;

        Match[] best = {null};
        walk(codePoints, start, start + 1, end, first, best);
        return best[0];
    }

    private static void walk(int[] codePoints, int start, int i, int end, Node node, Match[] best) {
        if (node.entry != null) {
            int stop = i < end && codePoints[i] == VARIATION_SELECTOR_16 ? i + 1 : i;
            if (best[0] == null || stop > best[0].end())
                best[0] = new Match(node.entry, start, stop);
        }
        if (i >= end)
            return;

        Node child = node.children.get(codePoints[i]);
        if (child != null)
            walk(codePoints, start, i + 1, end, child, best);
        if (codePoints[i] == VARIATION_SELECTOR_16)
            walk(codePoints, start, i + 1, end, node, best);
    }

    public static List<Match> findAll(int[] codePoints) {
        List<Match> matches = new ArrayList<>();
        int i = 0;
        while (i < codePoints.length) {
            Match match = matchAt(codePoints, i, codePoints.length);
            if (match != null) {
                matches.add(match);
                i = match.end();
            } else {
                i++;
            }
        }
        return matches;
    }

    public static String replaceWithGlyphs(String text) {
        if (!mayNeedGlyphs(text) || !isAvailable())
            return text;

        int[] codePoints = text.codePoints().toArray();
        StringBuilder result = null;
        int last = 0;
        for (Match match : findAll(codePoints)) {
            if (!match.needsGlyph(codePoints))
                continue;
            if (result == null)
                result = new StringBuilder(text.length());
            appendCodePoints(result, codePoints, last, match.start());
            result.appendCodePoint(match.entry().glyph());
            last = match.end();
        }

        if (result == null)
            return text;
        appendCodePoints(result, codePoints, last, codePoints.length);
        return result.toString();
    }

    public static String replaceWithNames(String text) {
        if (!isAvailable())
            return text;

        int[] codePoints = text.codePoints().toArray();
        List<Match> matches = findAll(codePoints);
        if (matches.isEmpty())
            return text;

        StringBuilder result = new StringBuilder(text.length() + matches.size() * 8);
        int last = 0;
        for (Match match : matches) {
            appendCodePoints(result, codePoints, last, match.start());
            result.append(':').append(match.entry().name()).append(':');
            last = match.end();
        }
        appendCodePoints(result, codePoints, last, codePoints.length);
        return result.toString();
    }

    private static void appendCodePoints(StringBuilder builder, int[] codePoints, int from, int to) {
        for (int i = from; i < to; i++)
            builder.appendCodePoint(codePoints[i]);
    }
}

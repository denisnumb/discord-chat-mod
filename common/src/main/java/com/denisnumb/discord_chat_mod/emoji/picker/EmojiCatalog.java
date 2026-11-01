package com.denisnumb.discord_chat_mod.emoji.picker;

import com.denisnumb.discord_chat_mod.discord.data_providers.CustomEmojiProvider;
import com.denisnumb.discord_chat_mod.discord.data_providers.StickersProvider;
import com.denisnumb.discord_chat_mod.emoji.UnicodeEmojis;
import com.denisnumb.discord_chat_mod.locale.MinecraftLocaleProvider;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public final class EmojiCatalog {
    private EmojiCatalog() {}

    public enum Group {
        PEOPLE("people", "😀", MinecraftLocaleProvider.EmojiPicker.Group::people),
        NATURE("nature", "🐻", MinecraftLocaleProvider.EmojiPicker.Group::nature),
        FOOD("food", "🍔", MinecraftLocaleProvider.EmojiPicker.Group::food),
        ACTIVITY("activity", "⚽", MinecraftLocaleProvider.EmojiPicker.Group::activities),
        TRAVEL("travel", "🚗", MinecraftLocaleProvider.EmojiPicker.Group::travel),
        OBJECTS("objects", "💡", MinecraftLocaleProvider.EmojiPicker.Group::objects),
        SYMBOLS("symbols", "🔣", MinecraftLocaleProvider.EmojiPicker.Group::symbols),
        FLAGS("flags", "🏁", MinecraftLocaleProvider.EmojiPicker.Group::flags);

        public final String id;
        public final String icon;
        private final Supplier<Component> title;

        Group(String id, String icon, Supplier<Component> title) {
            this.id = id;
            this.icon = icon;
            this.title = title;
        }

        public Component title() {
            return title.get();
        }

        public String iconGlyph() {
            return UnicodeEmojis.glyphFor(icon);
        }
    }

    public record UnicodeGroup(Group group, List<PickerEntry.UnicodeEmoji> entries) {}

    private static List<UnicodeGroup> unicodeGroups;

    public static List<UnicodeGroup> unicodeGroups() {
        if (unicodeGroups == null)
            unicodeGroups = loadUnicodeGroups();
        return unicodeGroups;
    }

    private static List<UnicodeGroup> loadUnicodeGroups() {
        List<UnicodeGroup> groups = new ArrayList<>();
        for (Group group : Group.values()) {
            List<PickerEntry.UnicodeEmoji> entries = UnicodeEmojis.category(group.id).stream()
                    .map(PickerEntry.UnicodeEmoji::new)
                    .toList();
            if (!entries.isEmpty())
                groups.add(new UnicodeGroup(group, entries));
        }
        return List.copyOf(groups);
    }

    public static List<PickerEntry.CustomEmoji> customEmojis() {
        return CustomEmojiProvider.CLIENT_EMOJI_CACHE.keySet().stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .map(PickerEntry.CustomEmoji::new)
                .toList();
    }

    public static int customEmojiCount() {
        return CustomEmojiProvider.CLIENT_EMOJI_CACHE.size();
    }

    public static Map<String, List<PickerEntry.Sticker>> stickersByGuild() {
        Map<String, List<PickerEntry.Sticker>> grouped = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (String key : StickersProvider.CLIENT_STICKER_CACHE.keySet()) {
            PickerEntry.Sticker sticker = parseSticker(key);
            grouped.computeIfAbsent(sticker.guild(), g -> new ArrayList<>()).add(sticker);
        }

        Map<String, List<PickerEntry.Sticker>> result = new LinkedHashMap<>();
        grouped.forEach((guild, stickers) -> {
            stickers.sort(Comparator.comparing(PickerEntry.Sticker::name, String.CASE_INSENSITIVE_ORDER));
            result.put(guild, List.copyOf(stickers));
        });
        return result;
    }

    public static int stickerCount() {
        return StickersProvider.CLIENT_STICKER_CACHE.size();
    }

    static PickerEntry.Sticker parseSticker(String key) {
        String trimmed = key.trim();
        if (trimmed.endsWith(")")) {
            int depth = 0;
            for (int i = trimmed.length() - 1; i > 0; i--) {
                char c = trimmed.charAt(i);
                if (c == ')')
                    depth++;
                else if (c == '(' && --depth == 0) {
                    if (trimmed.charAt(i - 1) != ' ')
                        break;
                    String name = trimmed.substring(0, i - 1).trim();
                    String guild = trimmed.substring(i + 1, trimmed.length() - 1);
                    return new PickerEntry.Sticker(key, name.isEmpty() ? trimmed : name, guild);
                }
            }
        }
        return new PickerEntry.Sticker(key, trimmed, "");
    }

    public static String normalizeQuery(String query) {
        String q = query.trim().toLowerCase(Locale.ROOT);
        if (q.startsWith(":"))
            q = q.substring(1);
        if (q.endsWith(":"))
            q = q.substring(0, q.length() - 1);
        return q;
    }

    public static List<PickerEntry.UnicodeEmoji> searchUnicode(String query) {
        return UnicodeEmojis.search(normalizeQuery(query), true, false).stream()
                .map(PickerEntry.UnicodeEmoji::new)
                .toList();
    }

    public static List<PickerEntry.CustomEmoji> searchCustom(String query) {
        String q = normalizeQuery(query);
        if (q.isEmpty())
            return List.of();

        List<Scored<PickerEntry.CustomEmoji>> scored = new ArrayList<>();
        int order = 0;
        for (PickerEntry.CustomEmoji entry : customEmojis()) {
            int score = scoreName(entry.name(), q);
            if (score >= 0)
                scored.add(new Scored<>(entry, score, order));
            order++;
        }
        return sorted(scored);
    }

    public static List<PickerEntry.Sticker> searchStickers(String query) {
        String q = normalizeQuery(query);
        if (q.isEmpty())
            return List.of();

        List<Scored<PickerEntry.Sticker>> scored = new ArrayList<>();
        int order = 0;
        for (List<PickerEntry.Sticker> stickers : stickersByGuild().values()) {
            for (PickerEntry.Sticker sticker : stickers) {
                int score = scoreName(sticker.name(), q);
                if (score < 0 && sticker.guild().toLowerCase(Locale.ROOT).contains(q))
                    score = 3;
                if (score >= 0)
                    scored.add(new Scored<>(sticker, score, order));
                order++;
            }
        }
        return sorted(scored);
    }

    private static int scoreName(String name, String q) {
        String n = name.toLowerCase(Locale.ROOT);
        if (n.equals(q))
            return 0;
        if (n.startsWith(q))
            return 1;
        if (n.contains(q))
            return 2;
        return -1;
    }

    private record Scored<T>(T entry, int score, int order) {}

    private static <T> List<T> sorted(List<Scored<T>> scored) {
        scored.sort(Comparator.<Scored<T>>comparingInt(Scored::score).thenComparingInt(Scored::order));
        return scored.stream().map(Scored::entry).toList();
    }

    @Nullable
    public static PickerEntry resolve(String id) {
        if (id.startsWith(PickerEntry.UnicodeEmoji.ID_PREFIX)) {
            UnicodeEmojis.Entry entry = UnicodeEmojis.byEmoji(id.substring(PickerEntry.UnicodeEmoji.ID_PREFIX.length()));
            return entry == null || entry.skinTone() ? null : new PickerEntry.UnicodeEmoji(entry);
        }
        if (id.startsWith(PickerEntry.CustomEmoji.ID_PREFIX)) {
            String name = id.substring(PickerEntry.CustomEmoji.ID_PREFIX.length());
            return CustomEmojiProvider.CLIENT_EMOJI_CACHE.containsKey(name) ? new PickerEntry.CustomEmoji(name) : null;
        }
        return null;
    }
}

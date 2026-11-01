package com.denisnumb.discord_chat_mod.emoji.picker;

import com.denisnumb.discord_chat_mod.chat_images.model.AbstractImage;
import com.denisnumb.discord_chat_mod.discord.data_providers.CustomEmojiProvider;
import com.denisnumb.discord_chat_mod.discord.data_providers.StickersProvider;
import com.denisnumb.discord_chat_mod.emoji.UnicodeEmojis;
import org.jetbrains.annotations.Nullable;

public sealed interface PickerEntry permits PickerEntry.UnicodeEmoji, PickerEntry.CustomEmoji, PickerEntry.Sticker {
    String id();

    String displayName();

    record UnicodeEmoji(UnicodeEmojis.Entry entry) implements PickerEntry {
        public static final String ID_PREFIX = "u:";

        public String unicode() {
            return entry.emoji();
        }

        public String glyph() {
            return entry.glyphText();
        }

        @Override
        public String id() {
            return ID_PREFIX + unicode();
        }

        @Override
        public String displayName() {
            return ":" + entry.name() + ":";
        }

        public String insertText() {
            return unicode();
        }
    }

    record CustomEmoji(String name) implements PickerEntry {
        public static final String ID_PREFIX = "c:";

        @Override
        public String id() {
            return ID_PREFIX + name;
        }

        @Override
        public String displayName() {
            return ":" + name + ":";
        }

        public String insertText() {
            return ":" + name + ":";
        }

        @Nullable
        public AbstractImage image() {
            return CustomEmojiProvider.CLIENT_EMOJI_CACHE.get(name);
        }
    }

    record Sticker(String key, String name, String guild) implements PickerEntry {
        public static final String ID_PREFIX = "s:";

        @Override
        public String id() {
            return ID_PREFIX + key;
        }

        @Override
        public String displayName() {
            return name;
        }

        @Nullable
        public AbstractImage image() {
            return StickersProvider.CLIENT_STICKER_CACHE.get(key);
        }
    }
}

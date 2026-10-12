package com.denisnumb.discord_chat_mod.emoji;

import com.denisnumb.discord_chat_mod.chat_images.model.AbstractImage;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public record EmojiLayout(FormattedCharSequence replaced, List<Slot> slots) {
    public static final EmojiLayout NONE = new EmojiLayout(null, List.of());

    public record Slot(int xOffset, AbstractImage image) {}

    public boolean isEmpty() {
        return slots.isEmpty();
    }
}
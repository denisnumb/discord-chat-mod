package com.denisnumb.discord_chat_mod.utils;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class FormattedCharSequenceUtils {
    private FormattedCharSequenceUtils() {}

    public static FormattedCharSequence subFormattedCharSequence(FormattedCharSequence text, int start, int end) {
        if (start >= end || start < 0) {
            return FormattedCharSequence.EMPTY;
        }

        List<FormattedCharSequence> parts = new ArrayList<>();
        AtomicInteger index = new AtomicInteger();

        text.accept((i, style, codePoint) -> {
            if (index.get() >= start && index.get() < end)
                parts.add(FormattedCharSequence.codepoint(codePoint, style));
            index.getAndIncrement();
            return true;
        });

        return FormattedCharSequence.composite(parts);
    }

    public static boolean hasRunCommandClickEvent(FormattedCharSequence seq, String command) {
        boolean[] found = {false};
        seq.accept((index, style, codePoint) -> {
            if (style.getClickEvent() instanceof ClickEvent.RunCommand(String cmd)
                    && cmd.equals(command)) {
                found[0] = true;
                return false;
            }
            return true;
        });
        return found[0];
    }
}

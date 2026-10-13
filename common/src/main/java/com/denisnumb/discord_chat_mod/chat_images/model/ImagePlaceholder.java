package com.denisnumb.discord_chat_mod.chat_images.model;

import org.jetbrains.annotations.Nullable;

public record ImagePlaceholder(@Nullable String url, boolean embedded) {
    public static final ImagePlaceholder NONE = new ImagePlaceholder(null, false);

    public boolean isNone() {
        return url == null;
    }
}
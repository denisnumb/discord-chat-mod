package com.denisnumb.discord_chat_mod.chat_images.model;

import net.minecraft.resources.ResourceLocation;

public abstract class AbstractImage {
    public final String url;
    public final ImageSize imageSize;
    public final ImageSize originalSize;
    public final ResourceLocation spoilerResourceLocation;
    private final boolean isSpoiler;
    private boolean isSpoilerOpened = false;

    protected AbstractImage(
            String url,
            ImageSize imageSize,
            ImageSize originalSize,
            boolean isSpoiler,
            ResourceLocation spoilerResourceLocation
    ) {
        this.url = url;
        this.imageSize = imageSize;
        this.originalSize = originalSize;
        this.isSpoiler = isSpoiler;
        this.spoilerResourceLocation = spoilerResourceLocation;
    }

    public boolean isSpoilerAndNotOpened(){
        return isSpoiler && !isSpoilerOpened;
    }

    public void openSpoiler(){
        isSpoilerOpened = true;
    }
}
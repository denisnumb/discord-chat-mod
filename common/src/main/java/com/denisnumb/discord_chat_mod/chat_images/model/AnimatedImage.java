package com.denisnumb.discord_chat_mod.chat_images.model;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class AnimatedImage extends AbstractImage {
    public final List<ResourceLocation> frames;
    public final int frameDuration;

    public AnimatedImage(
            String url,
            List<ResourceLocation> frames,
            ImageSize imageSize,
            ImageSize originalSize,
            int frameDuration,
            boolean isSpoiler,
            ResourceLocation spoilerResourceLocation
    ) {
        super(url, imageSize, originalSize, isSpoiler, spoilerResourceLocation);
        this.frames = frames;
        this.frameDuration = frameDuration;
    }

    public ResourceLocation getCurrentFrame() {
        long time = System.currentTimeMillis();
        int totalDuration = frames.size() * frameDuration;
        long timeInLoop = time % totalDuration;

        int elapsedTime = 0;
        for (ResourceLocation frame : frames) {
            elapsedTime += frameDuration;
            if (elapsedTime > timeInLoop)
                return frame;
        }
        return frames.getFirst();
    }
}
package com.denisnumb.discord_chat_mod.network.image.model;

import org.jetbrains.annotations.Nullable;

public record ImagePartPacketPayload(
        @Nullable String url,
        String fileName,
        String mimeType,
        String displayName,
        SendTarget sendTarget,
        byte[] imageData
) {
    public ImagePartPacketPayload withUrl(String newUrl) {
        return new ImagePartPacketPayload(
                newUrl,
                fileName,
                mimeType,
                displayName,
                sendTarget,
                imageData
        );
    }
}
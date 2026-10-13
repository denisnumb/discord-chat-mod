package com.denisnumb.discord_chat_mod.mixin;

import com.denisnumb.discord_chat_mod.chat_images.model.ImageMessageLine;
import com.denisnumb.discord_chat_mod.chat_images.model.ImagePlaceholder;
import net.minecraft.client.GuiMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GuiMessage.Line.class)
public class GuiMessageLineMixin implements ImageMessageLine {
    @Unique private ImagePlaceholder discord_chat_mod$placeholder;

    @Override
    public ImagePlaceholder discord_chat_mod$getPlaceholder() {
        return discord_chat_mod$placeholder;
    }

    @Override
    public void discord_chat_mod$setPlaceholder(ImagePlaceholder p) {
        this.discord_chat_mod$placeholder = p;
    }
}
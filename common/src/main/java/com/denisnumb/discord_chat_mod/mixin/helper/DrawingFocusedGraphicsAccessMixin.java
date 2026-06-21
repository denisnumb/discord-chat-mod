package com.denisnumb.discord_chat_mod.mixin.helper;

import com.denisnumb.discord_chat_mod.ColorUtils;
import net.minecraft.util.ARGB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.client.gui.components.ChatComponent$DrawingFocusedGraphicsAccess")
public abstract class DrawingFocusedGraphicsAccessMixin {
    @Redirect(
            method = "handleTag",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ARGB;color(FI)I")
    )
    private int skipTransparentTagColor(float alpha, int color) {
        if (color == ColorUtils.Color.TRANSPARENT_IMAGE_TAG_COLOR)
            return 0;
        return ARGB.color(alpha, color);
    }
}

package com.denisnumb.discord_chat_mod.mixin;

import net.minecraft.client.gui.font.EmptyArea;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(targets = "net.minecraft.client.gui.Font$PreparedTextBuilder")
public abstract class PreparedTextBuilderMixin {
    @Shadow
    private @Nullable List<EmptyArea> emptyAreas;

    @Inject(method = "bounds", at = @At("RETURN"), cancellable = true)
    private void fixEmptyBounds(CallbackInfoReturnable<ScreenRectangle> cir) {
        if (cir.getReturnValue() != null) return;
        if (this.emptyAreas == null || this.emptyAreas.isEmpty()) return;

        float left = Float.MAX_VALUE;
        float top = Float.MAX_VALUE;
        float right = -Float.MAX_VALUE;
        float bottom = -Float.MAX_VALUE;

        for (EmptyArea area : this.emptyAreas) {
            left = Math.min(left, area.x());
            top = Math.min(top, area.y());
            right = Math.max(right, area.x() + area.advance());
            bottom = Math.max(bottom, area.y() + EmptyArea.DEFAULT_HEIGHT);
        }

        cir.setReturnValue(new ScreenRectangle(
                Mth.floor(left),
                Mth.floor(top),
                Mth.ceil(right - left),
                Mth.ceil(bottom - top)
        ));
    }
}




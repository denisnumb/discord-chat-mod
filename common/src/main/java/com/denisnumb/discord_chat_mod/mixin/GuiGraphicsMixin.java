package com.denisnumb.discord_chat_mod.mixin;

import com.denisnumb.discord_chat_mod.emoji.EmojiLayout;
import com.denisnumb.discord_chat_mod.emoji.EmojiLayoutCache;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {
    @Shadow public abstract void blit(RenderPipeline renderPipeline, Identifier atlasLocation, int x, int y, float uOffset, float vOffset, int width, int height, int textureWidth, int textureHeight);

    @ModifyArgs(
            method = "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/render/state/GuiTextRenderState;<init>(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;Lorg/joml/Matrix3x2fc;IIIIZZLnet/minecraft/client/gui/navigation/ScreenRectangle;)V"
            )
    )
    private void handleCustomDiscordEmojis(Args args) {
        Font font = args.get(0);
        FormattedCharSequence text = args.get(1);
        int x = args.get(3);
        int y = args.get(4);

        EmojiLayout layout = EmojiLayoutCache.get(font, text);
        if (layout.isEmpty())
            return;

        for (EmojiLayout.Slot slot : layout.slots()) {
            blit(RenderPipelines.GUI_TEXTURED, slot.image().getRenderFrame(),
                    x + slot.xOffset(), y - 1, 0, 0, 9, 9, 9, 9);
        }
        args.set(1, layout.replaced());
    }
}

package com.denisnumb.discord_chat_mod.mixin;

import com.denisnumb.discord_chat_mod.emoji.EmojiLayout;
import com.denisnumb.discord_chat_mod.emoji.EmojiLayoutCache;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(targets = "net.minecraft.client.gui.GuiGraphics$RenderingTextCollector")
public abstract class RenderingTextCollectorMixin {
    @Shadow @Final GuiGraphics field_63856;

    @ModifyArgs(
            method = "accept(Lnet/minecraft/client/gui/TextAlignment;IILnet/minecraft/client/gui/ActiveTextCollector$Parameters;Lnet/minecraft/util/FormattedCharSequence;)V",
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
            this.field_63856.blit(RenderPipelines.GUI_TEXTURED, slot.image().getRenderFrame(),
                    x + slot.xOffset(), y - 1, 0, 0, 9, 9, 9, 9);
        }
        args.set(1, layout.replaced());
    }
}

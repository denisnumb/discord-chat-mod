package com.denisnumb.discord_chat_mod.mixin;

import com.denisnumb.discord_chat_mod.MinecraftClientEvents;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.function.Consumer;

@Mixin(Screenshot.class)
public class ScreenshotMixin {
    @Inject(method = "method_1661(Lcom/mojang/blaze3d/platform/NativeImage;Ljava/io/File;Ljava/util/function/Consumer;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/NativeImage;writeToFile(Ljava/io/File;)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private static void _grab(NativeImage nativeImage, File file, Consumer<Component> consumer, CallbackInfo ci, @Local(argsOnly = true) File file3){
        consumer.accept(MinecraftClientEvents.handleScreenshot(file3));
        nativeImage.close();
        ci.cancel();
    }
}

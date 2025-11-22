package com.denisnumb.discord_chat_mod.neoforge.mixin;

import com.denisnumb.discord_chat_mod.MinecraftClientEvents;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ScreenshotEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.function.Consumer;

@Mixin(Screenshot.class)
public class ScreenshotMixin {
    @Inject(method = "lambda$_grab$2(Lcom/mojang/blaze3d/platform/NativeImage;Ljava/io/File;Lnet/neoforged/neoforge/client/event/ScreenshotEvent;Ljava/util/function/Consumer;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/NativeImage;writeToFile(Ljava/io/File;)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private static void _grab(NativeImage nativeimage, File target, ScreenshotEvent event, Consumer<Component> consumer, CallbackInfo ci, @Local(argsOnly = true) File file3){
        consumer.accept(MinecraftClientEvents.handleScreenshot(file3));
        nativeimage.close();
        ci.cancel();
    }
}

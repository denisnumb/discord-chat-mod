package com.denisnumb.discord_chat_mod.mixin;

import com.denisnumb.discord_chat_mod.chat_images.ImageScreen;
import com.denisnumb.discord_chat_mod.chat_images.ImageStorage;
import com.denisnumb.discord_chat_mod.chat_images.ImageUtils;
import com.denisnumb.discord_chat_mod.chat_images.model.AbstractImage;
import com.denisnumb.discord_chat_mod.network.screenshot.ScreenshotTransceiver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Shadow protected Minecraft minecraft;
    @Shadow public static boolean hasShiftDown() { return false; }

    @Inject(method = "handleComponentClicked", at = @At("HEAD"), cancellable = true)
    private void handleComponentClicked(Style style, CallbackInfoReturnable<Boolean> cir){
        if (style != null){
            ClickEvent clickEvent = style.getClickEvent();
            if (clickEvent != null){
                if (clickEvent.getAction() == ClickEvent.Action.OPEN_URL && ImageUtils.isLocalResourceUrl(clickEvent.getValue())){
                    cir.setReturnValue(true);
                }
                if (clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND){
                    String value = StringUtil.filterText(clickEvent.getValue());
                    if (value.startsWith("send_screenshot")) {
                        boolean sendAsSpoiler = hasShiftDown();
                        String filePath = value.replace("send_screenshot ", "");
                        discord_minecraft_chat$sendScreenshot(filePath, sendAsSpoiler);
                        cir.setReturnValue(true);
                    }
                    if (value.startsWith("open_image")) {
                        String imageUrl = value.replace("open_image ", "");
                        AbstractImage image = ImageStorage.IMAGE_CACHE.getOrDefault(imageUrl, null);
                        if (image != null){
                            if (image.isSpoilerAndNotOpened())
                                image.openSpoiler();
                            else {
                                minecraft.setScreen(new ImageScreen(image));
                            }
                        }
                        cir.setReturnValue(true);
                    }
                }
            }
        }
    }

    @Unique
    private void discord_minecraft_chat$sendScreenshot(String filePath, boolean sendAsSpoiler){
        File screenshotFile = new File(filePath);

        if (screenshotFile.exists() && screenshotFile.isFile() && screenshotFile.getName().endsWith(".png"))
            ScreenshotTransceiver.sendScreenshotToServer(screenshotFile, minecraft.player, sendAsSpoiler);
    }
}

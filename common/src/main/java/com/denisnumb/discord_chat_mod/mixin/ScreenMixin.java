package com.denisnumb.discord_chat_mod.mixin;

import com.denisnumb.discord_chat_mod.chat_images.ImageScreen;
import com.denisnumb.discord_chat_mod.chat_images.ImageSendScreen;
import com.denisnumb.discord_chat_mod.chat_images.ImageStorage;
import com.denisnumb.discord_chat_mod.chat_images.utils.ImageUtils;
import com.denisnumb.discord_chat_mod.chat_images.model.AbstractImage;
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
import java.io.IOException;
import java.nio.file.Files;

import static com.denisnumb.discord_chat_mod.LocaleProvider.getTranslateClient;
import static com.denisnumb.discord_chat_mod.ModLanguageKey.SCREENSHOT;
import static com.denisnumb.discord_chat_mod.chat_images.utils.ImageUtils.LOCAL_RESOURCE_PREFIX;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Shadow protected Minecraft minecraft;

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
                    if (value.startsWith(ImageStorage.SEND_SCREENSHOT_COMMAND)) {
                        File screenshotFile = new File(value.replace(ImageStorage.SEND_SCREENSHOT_COMMAND, ""));
                        if (screenshotFile.exists() && screenshotFile.isFile() && screenshotFile.getName().endsWith(".png")) {
                            try {
                                discord_minecraft_chat$openImageSendScreen(
                                        Files.readAllBytes(screenshotFile.toPath()),
                                        getTranslateClient(SCREENSHOT)
                                );
                            } catch (IOException ignored) {}
                        }
                        cir.setReturnValue(true);
                    }
                    if (value.startsWith(ImageStorage.OPEN_IMAGE_COMMAND)) {
                        String imageUrl = value.replace(ImageStorage.OPEN_IMAGE_COMMAND, "");
                        AbstractImage image = ImageStorage.IMAGE_CACHE.getOrDefault(imageUrl, null);
                        if (image != null){
                            if (image.isSpoilerAndNotOpened())
                                image.openSpoiler();
                            else {
                                minecraft.setScreen(new ImageScreen(image, minecraft.screen));
                            }
                        }
                        cir.setReturnValue(true);
                    }
                }
            }
        }
    }

    @Unique
    private void discord_minecraft_chat$openImageSendScreen(byte[] imageBytes, String displayName) {
        try {
            String imagePreviewUrl = LOCAL_RESOURCE_PREFIX + System.currentTimeMillis() + "/imagePreview";
            String mimeType = ImageUtils.getMimeType(imageBytes);
            AbstractImage image = ImageStorage.registerImageFromBytes(imagePreviewUrl, mimeType, imageBytes);
            minecraft.setScreen(new ImageSendScreen(image, imageBytes, mimeType, displayName, minecraft.screen, minecraft.player));
        } catch (Exception ignored) {}
    }
}

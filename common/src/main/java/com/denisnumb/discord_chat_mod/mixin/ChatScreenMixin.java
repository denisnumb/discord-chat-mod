package com.denisnumb.discord_chat_mod.mixin;

import com.denisnumb.discord_chat_mod.config.configs.ClientConfig;
import com.denisnumb.discord_chat_mod.utils.MinecraftUtils;
import com.denisnumb.discord_chat_mod.chat_images.ImageScreen;
import com.denisnumb.discord_chat_mod.chat_images.ImageSendScreen;
import com.denisnumb.discord_chat_mod.chat_images.ImageStorage;
import com.denisnumb.discord_chat_mod.chat_images.model.AbstractImage;
import com.denisnumb.discord_chat_mod.chat_images.clipboard.ClipboardImageUtils;
import com.denisnumb.discord_chat_mod.chat_images.utils.ImageUtils;
import com.denisnumb.discord_chat_mod.gui.ChatInputIconButton;
import com.denisnumb.discord_chat_mod.emoji.picker.EmojiPickerPopup;
import com.denisnumb.discord_chat_mod.emoji.picker.PickerEntry;
import com.denisnumb.discord_chat_mod.locale.MinecraftLocaleProvider;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringUtil;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import static com.denisnumb.discord_chat_mod.chat_images.ImageStorage.MAX_DRAG_DROP_FILE_SIZE;
import static com.denisnumb.discord_chat_mod.chat_images.ImageStorage.MAX_DRAG_DROP_FILE_SIZE_MB;
import static com.denisnumb.discord_chat_mod.chat_images.utils.ImageUtils.LOCAL_RESOURCE_PREFIX;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
    @Unique
    private static final Logger discord_chat_mod$LOGGER = LogUtils.getLogger();

    @Unique
    private static final int discord_chat_mod$CHAT_INPUT_MAX_LENGTH = 256;

    @Shadow
    protected EditBox input;

    @Shadow
    CommandSuggestions commandSuggestions;

    @Unique
    private EmojiPickerPopup discord_chat_mod$emojiPicker;

    @Unique
    private ChatInputIconButton discord_chat_mod$emojiButton;

    protected ChatScreenMixin(Component component) {
        super(component);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        boolean emojifulCompatibility = ClientConfig.EMOJIFUL_COMPATIBILITY.get();
        boolean attachButtonEnabled = ClientConfig.ENABLE_ATTACH_IMAGE_BUTTON.get() && !emojifulCompatibility;
        boolean emojiButtonEnabled = ClientConfig.ENABLE_EMOJI_PICKER_BUTTON.get() && !emojifulCompatibility;

        discord_chat_mod$emojiButton = null;
        int buttonCount = (attachButtonEnabled ? 1 : 0) + (emojiButtonEnabled ? 1 : 0);
        if (buttonCount == 0) {
            if (discord_chat_mod$emojiPicker != null)
                discord_chat_mod$emojiPicker.hide();
            return;
        }

        int iconSize = this.input.getHeight();
        int padding = 2;

        this.input.setWidth(this.input.getWidth() - iconSize * buttonCount);

        int iconX = this.input.getX() + this.input.getWidth() - padding;
        int iconY = this.input.getY() - padding;

        if (emojiButtonEnabled) {
            discord_chat_mod$emojiButton = addRenderableWidget(new ChatInputIconButton(
                    iconX, iconY, iconSize,
                    Component.literal("\uD83D\uDE00"),
                    MinecraftLocaleProvider.EmojiPicker.open(),
                    btn -> discord_chat_mod$toggleEmojiPicker(),
                    () -> discord_chat_mod$emojiPicker != null && discord_chat_mod$emojiPicker.isVisible()
            ));
            iconX += iconSize;

            if (discord_chat_mod$emojiPicker == null)
                discord_chat_mod$emojiPicker = discord_chat_mod$createEmojiPicker();
            else
                discord_chat_mod$emojiPicker.relayout(discord_chat_mod$pickerRightX(), discord_chat_mod$pickerBottomY());
        } else if (discord_chat_mod$emojiPicker != null) {
            discord_chat_mod$emojiPicker.hide();
        }

        if (attachButtonEnabled) {
            addRenderableWidget(new ChatInputIconButton(
                    iconX, iconY, iconSize,
                    Component.literal("📎"),
                    MinecraftLocaleProvider.SendImage.attachImage(),
                    btn -> discord_minecraft_chat$openImageFileDialog()
            ));
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (discord_chat_mod$isEmojiPickerVisible())
            discord_chat_mod$emojiPicker.render(graphics, mouseX, mouseY, partialTick);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (!discord_chat_mod$isEmojiPickerVisible())
            return;

        if (discord_chat_mod$emojiPicker.mouseClicked(event, doubleClick)) {
            cir.setReturnValue(true);
            return;
        }

        if (discord_chat_mod$emojiButton != null && discord_chat_mod$emojiButton.isMouseOver(event.x(), event.y()))
            return;

        discord_chat_mod$closeEmojiPicker();
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void onMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
        if (discord_chat_mod$isEmojiPickerVisible() && discord_chat_mod$emojiPicker.mouseScrolled(mouseX, mouseY, scrollY))
            cir.setReturnValue(true);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> cir) {
        if (discord_chat_mod$isEmojiPickerVisible()) {
            if (keyEvent.key() == GLFW.GLFW_KEY_ESCAPE)
                discord_chat_mod$closeEmojiPicker();
            else {
                discord_chat_mod$emojiPicker.focusSearch();
                discord_chat_mod$emojiPicker.keyPressed(keyEvent);
            }
            cir.setReturnValue(true);
            return;
        }

        if (!keyEvent.isPaste() || !ClientConfig.ENABLE_CLIPBOARD_IMAGE_PASTE.get())
            return;

        try {
            byte[] imageBytes = ClipboardImageUtils.getImageBytes();
            if (imageBytes.length == 0)
                return;

            discord_minecraft_chat$openImageSendScreen(imageBytes, MinecraftLocaleProvider.image().getString());
            cir.setReturnValue(true);
        } catch (IllegalStateException e) {
            MinecraftUtils.showTitleBarMessage(MinecraftLocaleProvider.SendImage.Error.readClipboard(e.getMessage()));
            discord_chat_mod$LOGGER.error("ClipboardImagePasteError: ", e);
        }
    }

    @Inject(method = "handleComponentClicked", at = @At("HEAD"), cancellable = true)
    private void handleComponentClicked(Style style, boolean bl, CallbackInfoReturnable<Boolean> cir){
        if (style != null){
            ClickEvent clickEvent = style.getClickEvent();
            if (clickEvent != null){
                if (clickEvent instanceof ClickEvent.OpenUrl(URI uri) && ImageUtils.isLocalResourceUrl(uri.toString())){
                    cir.setReturnValue(true);
                }
                if (clickEvent instanceof ClickEvent.RunCommand(String command)){
                    String value = StringUtil.filterText(command);
                    if (value.startsWith(ImageStorage.SEND_SCREENSHOT_COMMAND)) {
                        File screenshotFile = new File(value.replace(ImageStorage.SEND_SCREENSHOT_COMMAND, ""));
                        if (screenshotFile.exists() && screenshotFile.isFile() && screenshotFile.getName().endsWith(".png")) {
                            try {
                                discord_minecraft_chat$openImageSendScreen(
                                        Files.readAllBytes(screenshotFile.toPath()),
                                        MinecraftLocaleProvider.screenshot().getString()
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
    private EmojiPickerPopup discord_chat_mod$createEmojiPicker() {
        return new EmojiPickerPopup(minecraft, EmojiPickerPopup.Host.of(
                this::addWidget,
                this::removeWidget,
                this::setFocused,
                this::discord_chat_mod$insertEmoji,
                this::discord_chat_mod$sendSticker
        ));
    }

    @Unique
    private boolean discord_chat_mod$isEmojiPickerVisible() {
        return discord_chat_mod$emojiPicker != null && discord_chat_mod$emojiPicker.isVisible();
    }

    @Unique
    private int discord_chat_mod$pickerRightX() {
        return this.width - 2;
    }

    @Unique
    private int discord_chat_mod$pickerBottomY() {
        return this.input.getY() - 4;
    }

    @Unique
    private void discord_chat_mod$toggleEmojiPicker() {
        if (discord_chat_mod$emojiPicker == null)
            return;

        if (discord_chat_mod$emojiPicker.isVisible()) {
            discord_chat_mod$closeEmojiPicker();
            return;
        }

        this.commandSuggestions.hide();
        discord_chat_mod$emojiPicker.show(discord_chat_mod$pickerRightX(), discord_chat_mod$pickerBottomY());
    }

    @Unique
    private void discord_chat_mod$closeEmojiPicker() {
        if (discord_chat_mod$emojiPicker != null)
            discord_chat_mod$emojiPicker.hide();
        this.setFocused(this.input);
    }

    @Unique
    private void discord_chat_mod$insertEmoji(PickerEntry entry, boolean keepOpen) {
        String text = switch (entry) {
            case PickerEntry.UnicodeEmoji unicode -> unicode.insertText();
            case PickerEntry.CustomEmoji custom -> custom.insertText() + " ";
            case PickerEntry.Sticker sticker -> null;
        };

        if (text != null) {
            String value = this.input.getValue();
            int cursor = this.input.getCursorPosition();
            if (entry instanceof PickerEntry.CustomEmoji && cursor > 0 && !Character.isWhitespace(value.charAt(cursor - 1)))
                text = " " + text;

            if (value.length() + text.length() <= discord_chat_mod$CHAT_INPUT_MAX_LENGTH)
                this.input.insertText(text);
        }

        if (!keepOpen)
            discord_chat_mod$closeEmojiPicker();
    }

    @Unique
    private void discord_chat_mod$sendSticker(PickerEntry.Sticker sticker) {
        ClientPacketListener connection = minecraft.getConnection();
        if (connection == null)
            return;

        connection.sendCommand("send_sticker " + sticker.key());
        discord_chat_mod$emojiPicker.hide();
        minecraft.setScreen(null);
    }

    @Unique
    private void discord_minecraft_chat$openImageFileDialog() {
        CompletableFuture.runAsync(() -> {
            String path;
            try (MemoryStack stack = MemoryStack.stackPush()) {
                PointerBuffer filters = stack.mallocPointer(5);
                filters.put(stack.UTF8("*.png"));
                filters.put(stack.UTF8("*.jpg"));
                filters.put(stack.UTF8("*.gif"));
                filters.put(stack.UTF8("*.webp"));
                filters.put(stack.UTF8("*.bmp"));
                filters.flip();

                path = TinyFileDialogs.tinyfd_openFileDialog(
                        MinecraftLocaleProvider.SendImage.selectImage().getString(),
                        "",
                        filters,
                        "Image Files (png, jpg, gif, webp, bmp)",
                        false
                );
            }

            if (path == null)
                return;

            minecraft.execute(() -> {
                try {
                    Path filePath = Path.of(path);
                    if (Files.size(filePath) > MAX_DRAG_DROP_FILE_SIZE) {
                        MinecraftUtils.showTitleBarMessage(MinecraftLocaleProvider.SendImage.Error.fileTooLarge(MAX_DRAG_DROP_FILE_SIZE_MB));
                        return;
                    }

                    byte[] imageBytes = Files.readAllBytes(filePath);
                    discord_minecraft_chat$openImageSendScreen(imageBytes, filePath.getFileName().toString());
                } catch (IOException e) {
                    MinecraftUtils.showTitleBarMessage(MinecraftLocaleProvider.SendImage.Error.readFile(e.getMessage()));
                    discord_chat_mod$LOGGER.error("ReadImageFromFileError: ", e);
                }
            });
        });
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

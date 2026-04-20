package com.denisnumb.discord_chat_mod.mixin;

import com.denisnumb.discord_chat_mod.MinecraftClientEvents;
import com.denisnumb.discord_chat_mod.chat_images.model.AbstractImage;
import com.denisnumb.discord_chat_mod.chat_images.model.AnimatedImage;
import com.denisnumb.discord_chat_mod.chat_images.model.Image;
import com.denisnumb.discord_chat_mod.chat_images.model.ImageSize;
import com.denisnumb.discord_chat_mod.config.ConfigProvider;
import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.denisnumb.discord_chat_mod.LocaleProvider.getTranslateClient;
import static com.denisnumb.discord_chat_mod.ModLanguageKey.CLICK_TO_OPEN_IMAGE;
import static com.denisnumb.discord_chat_mod.ModLanguageKey.SPOILER;
import static com.denisnumb.discord_chat_mod.chat_images.ImageStorage.*;


@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private List<GuiMessage.Line> trimmedMessages;
    @Shadow private int chatScrollbarPos;
    @Shadow protected abstract int getLineHeight();
    @Shadow public abstract boolean isChatFocused();
    @Shadow public abstract double getScale();
    @Shadow @Final private List<GuiMessage> allMessages;
    @Shadow public abstract int getLinesPerPage();

    @ModifyArg(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/GuiMessage;<init>(ILnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V"
            ),
            index = 1,
            require = 0
    )
    private Component handleChatMessageClient(Component component) {
        return MinecraftClientEvents.handleChatMessage(component);
    }

    @Unique
    private List<String> discord_minecraft_chat$getComponentUrls(Component component){
        return component.toFlatList().stream().filter(comp -> {
            ClickEvent clickEvent = comp.getStyle().getClickEvent();
            return clickEvent != null && clickEvent.action() == ClickEvent.Action.OPEN_URL;
        }).filter(comp -> comp.getStyle().getClickEvent() instanceof ClickEvent.OpenUrl)
                .map(comp -> ((ClickEvent.OpenUrl)comp.getStyle().getClickEvent()).uri().toString()).toList();
    }

    @Unique
    private int discord_minecraft_chat$getImageLinesCount(int imageHeight){
        return Mth.ceil((float) imageHeight / getLineHeight()) + 1;
    }

    @Unique
    private int discord_minecraft_chat$getGuiMessageIndexByTrimmedMessageIndex(int targetIndex) {
        int messageIndex = -1;

        for (int i = 0; i <= targetIndex; i++)
            if (trimmedMessages.get(i).endOfEntry())
                messageIndex++;

        return messageIndex;
    }

    @Inject(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
            at = @At("TAIL")
    )
    private void addMessage(Component chatComponent, MessageSignature headerSignature, GuiMessageTag tag, CallbackInfo ci, @Local GuiMessage guimessage){
        List<String> componentUrls = discord_minecraft_chat$getComponentUrls(chatComponent);
        if (componentUrls.isEmpty())
            return;

        loadImagesParallel(componentUrls, () -> trimmedMessages.size(), () -> allMessages.size()).thenAccept(loadResult -> {
            int oldTrimmedSize = loadResult.trimmedMessagesSize();
            int oldAllSize = loadResult.allMessagesSize();
            int trimmedIndex = trimmedMessages.size() > oldTrimmedSize ? trimmedMessages.size() - oldTrimmedSize : 0;
            int allIndex = allMessages.size() > oldAllSize ? allMessages.size() - oldAllSize : 0;

            for (AbstractImage image : Lists.reverse(loadResult.images())){
                if (image == null)
                    continue;

                int linesCount = discord_minecraft_chat$getImageLinesCount(image.imageSize.height());
                Component imageComponent = Component.literal(" ".repeat(image.imageSize.width() / minecraft.font.width(" ")))
                        .withStyle(style ->
                                style.withHoverEvent(new HoverEvent.ShowText(Component.literal(getTranslateClient(CLICK_TO_OPEN_IMAGE))))
                                        .withClickEvent(new ClickEvent.RunCommand("open_image " + image.url))
                        );
                FormattedCharSequence imageCharSequence = imageComponent.getVisualOrderText();

                for (int i = 0; i < linesCount; i++)
                    trimmedMessages.add(trimmedIndex, new GuiMessage.Line(guimessage.addedTime(), imageCharSequence, tag, true));
                trimmedIndex += linesCount;

                for (int i = 0; i < linesCount; i++)
                    allMessages.add(allIndex, new GuiMessage(guimessage.addedTime(), imageComponent, headerSignature, tag));
                allIndex += linesCount;
            }
        });
    }

    @ModifyConstant(method = "addMessageToQueue", constant = @Constant(intValue = 100), require = 0)
    private int modifyAddMessageToQueueMessageLimit(int original) {
        return ConfigProvider.getConfig().maxChatHistory();
    }

    @ModifyConstant(method = "addMessageToDisplayQueue", constant = @Constant(intValue = 100), require = 0)
    private int modifyAddMessageToDisplayQueueMessageLimit(int original) {
        return ConfigProvider.getConfig().maxChatHistory();
    }

    @Inject(method = "addMessageToQueue",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;addFirst(Ljava/lang/Object;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void removeOldFromAllMessages(GuiMessage message, CallbackInfo ci) {
        while(allMessages.size() > ConfigProvider.getConfig().maxChatHistory()) {
            int parentAddedTime = allMessages.getLast().addedTime();

            do allMessages.removeLast();
            while (allMessages.getLast().addedTime() == parentAddedTime);
        }
    }

    @Inject(method = "addMessageToDisplayQueue",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;addFirst(Ljava/lang/Object;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void removeOldFromTrimmedMessages(GuiMessage message, CallbackInfo ci) {
        while(trimmedMessages.size() > ConfigProvider.getConfig().maxChatHistory()) {
            int parentAddedTime = trimmedMessages.getLast().addedTime();

            do trimmedMessages.removeLast();
            while (trimmedMessages.getLast().addedTime() == parentAddedTime);
        }
    }

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;IIIZZ)V", at = @At("TAIL"))
    private void render(GuiGraphics graphics, net.minecraft.client.gui.Font font, int tickCount, int mouseX, int mouseY, boolean focused, boolean focusedB, CallbackInfo ci){
        Map<Integer, List<String>> allChatUrls = new HashMap<>();
        for (int i = 0; i < allMessages.size(); ++i) {
            GuiMessage guiMessage = allMessages.get(i);
            List<String> urls = discord_minecraft_chat$getComponentUrls(guiMessage.content());
            if (!urls.isEmpty())
                allChatUrls.put(i, urls);
        }

        if (allChatUrls.isEmpty())
            return;

        int chatBottomY = Mth.floor((float) (graphics.guiHeight() - 40) / (float) this.getScale());
        int lineHeight = getLineHeight();
        int chatTopY = chatBottomY - lineHeight * getLinesPerPage();
        boolean isChatFocused = isChatFocused();

        Map<Integer, Integer> messagesY = new HashMap<>();
        for (int i = 0; i + chatScrollbarPos < trimmedMessages.size(); ++i) {
            int messageIndex = i + chatScrollbarPos;
            int guiMessageIndex = discord_minecraft_chat$getGuiMessageIndexByTrimmedMessageIndex(messageIndex);
            int messageY = chatBottomY - i * lineHeight;

            if (trimmedMessages.get(messageIndex).endOfEntry())
                messagesY.put(guiMessageIndex, messageY);
        }

        graphics.pose().pushMatrix();
        graphics.pose().translate(4.0F, 0.0F);

        for (Map.Entry<Integer, List<String>> entry : allChatUrls.entrySet()) {
            int messageIndex = entry.getKey();
            GuiMessage guiMessage = allMessages.get(messageIndex);
            List<String> urls = entry.getValue();

            if (!messagesY.containsKey(messageIndex))
                continue;
            if (tickCount - guiMessage.addedTime() >= 200 && !isChatFocused)
                continue;

            int messageY = messagesY.get(messageIndex);
            int offset = 0;

            for (String imageUrl : urls) {
                if (!IMAGE_CACHE.containsKey(imageUrl))
                    continue;

                AbstractImage abstractImage = IMAGE_CACHE.get(imageUrl);
                ImageSize imageSize = abstractImage.imageSize;

                Identifier resourceLocation = abstractImage.isSpoilerAndNotOpened()
                        ? abstractImage.spoilerIdentifier
                        : abstractImage instanceof AnimatedImage gif
                        ? gif.getCurrentFrame()
                        : ((Image) abstractImage).resourceLocation;

                int imageWidth = imageSize.width();
                int imageHeight = imageSize.height();
                int visibleHeight = imageHeight;

                int startY = messageY + offset + (lineHeight / 2);
                int startV = 0;
                if (startY < chatTopY){
                    startV = Math.abs(chatTopY - startY);
                    visibleHeight -= startV;
                    startY = chatTopY;
                }

                int endY = startY + imageHeight;
                if (endY > chatBottomY)
                    visibleHeight -= Math.abs(endY - chatBottomY);

                graphics.blit(RenderPipelines.GUI_TEXTURED, resourceLocation,
                        0, startY,                  // x, y
                        0, startV,                    // u, v
                        imageWidth, visibleHeight,    // size on screen
                        imageWidth, imageHeight       // real image size
                );

                if (abstractImage.isSpoilerAndNotOpened()) {
                    Component spoilerText = Component.literal(getTranslateClient(SPOILER))
                            .setStyle(Style.EMPTY.withBold(true));
                    float maxScale = 1.5f;
                    float scaleX = (imageWidth / MAX_WIDTH) * maxScale;
                    float scaleY = (imageHeight / MAX_HEIGHT) * maxScale;
                    float scale = Mth.clamp(Math.min(scaleX, scaleY), 0.5f, maxScale);

                    int originalTextWidth = minecraft.font.width(spoilerText);
                    int originalTextHeight = minecraft.font.lineHeight;

                    int centerX = imageWidth / 2;
                    int fullCenterY = messageY + offset + (lineHeight / 2) + imageHeight / 2;

                    if (fullCenterY >= chatTopY && fullCenterY <= chatBottomY) {
                        int textX = centerX - (int)(originalTextWidth * scale / 2);
                        int textY = fullCenterY - (int)(originalTextHeight * scale / 2);

                        graphics.pose().pushMatrix();
                        graphics.pose().translate(textX, textY);
                        graphics.pose().scale(scale, scale);

                        graphics.drawString(minecraft.font,
                                spoilerText,
                                0, 0,
                                ARGB.color(0xFFFFFF, -1),
                                true
                        );

                        graphics.pose().popMatrix();
                    }
                }

                offset += imageHeight + lineHeight;
            }
        }

        graphics.pose().popMatrix();
    }
}

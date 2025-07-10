package com.denisnumb.discord_chat_mod.mixin;

import com.denisnumb.discord_chat_mod.Config;
import com.denisnumb.discord_chat_mod.chat_images.model.AbstractImage;
import com.denisnumb.discord_chat_mod.chat_images.model.AnimatedImage;
import com.denisnumb.discord_chat_mod.chat_images.model.Image;
import com.denisnumb.discord_chat_mod.discord.CustomEmojiProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {
    @Unique private static final Pattern EMOJI_PATTERN = Pattern.compile(":([a-zA-Z0-9_]{2,}(~([1-9][0-9]*))?):");
    @Shadow public abstract int drawString(Font p_282636_, FormattedCharSequence p_281596_, float p_281586_, float p_282816_, int p_281743_, boolean p_282394_);
    @Shadow public abstract int drawString(Font p_283343_, @Nullable String p_281896_, float p_283569_, float p_283418_, int p_281560_, boolean p_282130_);
    @Shadow public abstract void blit(ResourceLocation atlasLocation, int x, int y, float uOffset, float vOffset, int width, int height, int textureWidth, int textureHeight);

    @Unique
    private static FormattedCharSequence discord_minecraft_chat$substringFormatted(FormattedCharSequence text, int start, int end) {
        if (start >= end || start < 0) {
            return FormattedCharSequence.EMPTY;
        }

        List<FormattedCharSequence> parts = new ArrayList<>();
        AtomicInteger index = new AtomicInteger();

        text.accept((i, style, codePoint) -> {
            if (index.get() >= start && index.get() < end)
                parts.add(FormattedCharSequence.codepoint(codePoint, style));
            index.getAndIncrement();
            return true;
        });

        return FormattedCharSequence.composite(parts);
    }

    @Inject(method = "drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I", at = @At("HEAD"), cancellable = true)
    public void drawString(Font font, String text, int x, int y, int color, boolean dropShadow, CallbackInfoReturnable<Integer> cir){
        Matcher matcher = EMOJI_PATTERN.matcher(text);

        if (Config.EMOJIFUL_COMPATIBILITY.get() || !matcher.find() || CustomEmojiProvider.CLIENT_EMOJI_CACHE.isEmpty()) {
            cir.setReturnValue(drawString(font, text, (float)x, (float)y, color, dropShadow));
            return;
        }

        int currentX = x;
        int lastEnd = 0;

        do {
            AbstractImage abstractImage = CustomEmojiProvider.CLIENT_EMOJI_CACHE.get(matcher.group(1));

            if (matcher.start() > lastEnd) {
                String before = text.substring(lastEnd, matcher.start());
                currentX = drawString(font, before, (float)x, (float)y, color, dropShadow);
            }

            if (abstractImage != null) {
                ResourceLocation emoji = abstractImage instanceof AnimatedImage emojiGif
                        ? emojiGif.getCurrentFrame()
                        : ((Image) abstractImage).resourceLocation;

                Minecraft.getInstance().getTextureManager().bindForSetup(emoji);
                int emojiSize = 9;
                blit(emoji, currentX, y-1, 0, 0, emojiSize, emojiSize, emojiSize, emojiSize);
                currentX += emojiSize;
            } else {
                String notFound = text.substring(matcher.start(), matcher.end());
                currentX = drawString(font, notFound, (float)x, (float)y, color, dropShadow);
            }

            lastEnd = matcher.end();
        } while (matcher.find());

        if (lastEnd < text.length()) {
            String rest = text.substring(lastEnd);
            currentX = drawString(font, rest, currentX, y, color, dropShadow);
        }

        cir.setReturnValue(currentX);
    }

    @Inject(method = "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)I", at = @At("HEAD"), cancellable = true)
    public void drawString(Font font, FormattedCharSequence text, int x, int y, int color, boolean dropShadow, CallbackInfoReturnable<Integer> cir) {
        StringBuilder rawTextBuilder = new StringBuilder();
        text.accept((index, style, codePoint) -> {
            rawTextBuilder.appendCodePoint(codePoint);
            return true;
        });

        String rawText = rawTextBuilder.toString();
        Matcher matcher = EMOJI_PATTERN.matcher(rawText);

        if (Config.EMOJIFUL_COMPATIBILITY.get() || !matcher.find() || CustomEmojiProvider.CLIENT_EMOJI_CACHE.isEmpty()) {
            cir.setReturnValue(drawString(font, text, x, y, color, dropShadow));
            return;
        }

        int currentX = x;
        int lastEnd = 0;

        do {
            AbstractImage abstractImage = CustomEmojiProvider.CLIENT_EMOJI_CACHE.get(matcher.group(1));

            if (matcher.start() > lastEnd) {
                FormattedCharSequence beforeSeq = discord_minecraft_chat$substringFormatted(text, lastEnd, matcher.start());
                currentX = drawString(font, beforeSeq, currentX, y, color, dropShadow);
            }

            if (abstractImage != null) {
                ResourceLocation emoji = abstractImage instanceof AnimatedImage emojiGif
                        ? emojiGif.getCurrentFrame()
                        : ((Image) abstractImage).resourceLocation;

                Minecraft.getInstance().getTextureManager().bindForSetup(emoji);
                int emojiSize = 9;
                blit(emoji, currentX, y-1, 0, 0, emojiSize, emojiSize, emojiSize, emojiSize);
                currentX += emojiSize;
            } else {
                FormattedCharSequence notFound = discord_minecraft_chat$substringFormatted(text, matcher.start(), matcher.end());
                currentX = drawString(font, notFound, currentX, y, color, dropShadow);
            }

            lastEnd = matcher.end();
        } while (matcher.find());

        if (lastEnd < rawText.length()) {
            FormattedCharSequence restSeq = discord_minecraft_chat$substringFormatted(text, lastEnd, Integer.MAX_VALUE);
            currentX = drawString(font, restSeq, currentX, y, color, dropShadow);
        }

        cir.setReturnValue(currentX);
    }
}

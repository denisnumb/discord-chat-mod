package com.denisnumb.discord_chat_mod.mixin;

import com.denisnumb.discord_chat_mod.chat_images.model.AbstractImage;
import com.denisnumb.discord_chat_mod.chat_images.model.AnimatedImage;
import com.denisnumb.discord_chat_mod.chat_images.model.Image;
import com.denisnumb.discord_chat_mod.config.ConfigProvider;
import com.denisnumb.discord_chat_mod.discord.data_providers.CustomEmojiProvider;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {
    @Unique private static final Pattern EMOJI_PATTERN = Pattern.compile(":([a-zA-Z0-9_]{2,}(~([1-9][0-9]*))?):");
    @Shadow public abstract void blit(RenderPipeline renderPipeline, Identifier atlasLocation, int x, int y, float uOffset, float vOffset, int width, int height, int textureWidth, int textureHeight);

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

    @ModifyArgs(
            method = "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/render/state/GuiTextRenderState;<init>(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;Lorg/joml/Matrix3x2f;IIIIZLnet/minecraft/client/gui/navigation/ScreenRectangle;)V"
            )
    )
    private void handleCustomDiscordEmojis(Args args) {
        Font font = args.get(0);
        FormattedCharSequence text = args.get(1);
        int x = args.get(3);
        int y = args.get(4);

        if (text == null || text == FormattedCharSequence.EMPTY)
            return;

        List<Integer> codepoints = new ArrayList<>();
        text.accept((index, style, codePoint) -> {
            codepoints.add(codePoint);
            return true;
        });

        if (codepoints.isEmpty())
            return;

        StringBuilder rawBuilder = new StringBuilder();
        for (int cp : codepoints)
            rawBuilder.appendCodePoint(cp);

        String rawText = rawBuilder.toString();
        Matcher matcher = EMOJI_PATTERN.matcher(rawText);

        if (ConfigProvider.getConfig().isEmojifulCompatibilityEnabled()
                || !matcher.find()
                || CustomEmojiProvider.CLIENT_EMOJI_CACHE.isEmpty()) {
            return;
        }

        List<FormattedCharSequence> result = new ArrayList<>();
        int currentX = x;
        int lastEnd = 0;

        do {
            if (matcher.start() > lastEnd) {
                FormattedCharSequence before = discord_minecraft_chat$substringFormatted(text, lastEnd, matcher.start());
                result.add(before);
                currentX += font.width(before) + 1;
            }

            AbstractImage abstractImage = CustomEmojiProvider.CLIENT_EMOJI_CACHE.get(matcher.group(1));

            if (abstractImage != null) {

                Identifier emoji =
                        abstractImage instanceof AnimatedImage gif
                                ? gif.getCurrentFrame()
                                : ((Image) abstractImage).resourceLocation;

                int emojiSize = 9;
                blit(RenderPipelines.GUI_TEXTURED, emoji, currentX, y - 1, 0, 0, emojiSize, emojiSize, emojiSize, emojiSize);
                result.add(FormattedCharSequence.codepoint(' ', Style.EMPTY)); // space for emoji; width = 4
                result.add(FormattedCharSequence.codepoint(' ', Style.EMPTY.withBold(true))); // space for emoji; width = 5
                currentX += emojiSize;

            } else {
                FormattedCharSequence notFound = discord_minecraft_chat$substringFormatted(text, matcher.start(), matcher.end());
                result.add(notFound);
                currentX += font.width(notFound) + 1;
            }

            lastEnd = matcher.end();

        } while (matcher.find());

        if (lastEnd < rawText.length())
            result.add(discord_minecraft_chat$substringFormatted(text, lastEnd, rawText.length()));

        args.set(1, FormattedCharSequence.composite(result));
    }
}

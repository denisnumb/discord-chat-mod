package com.denisnumb.discord_chat_mod.mixin;

import com.denisnumb.discord_chat_mod.emoji.EmojiLayoutCache;
import net.minecraft.client.StringSplitter;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(StringSplitter.class)
public abstract class StringSplitterMixin {
    @ModifyVariable(method = "stringWidth(Ljava/lang/String;)F", at = @At("HEAD"), argsOnly = true)
    private String handleDiscordCustomEmoji(String text) {
        return EmojiLayoutCache.replaceForWidth(text);
    }

    @ModifyVariable(method = "stringWidth(Lnet/minecraft/util/FormattedCharSequence;)F", at = @At("HEAD"), argsOnly = true)
    private FormattedCharSequence handleDiscordCustomEmoji(FormattedCharSequence text) {
        return EmojiLayoutCache.replacedOrSelf(text);
    }
}

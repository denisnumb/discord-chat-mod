package com.denisnumb.discord_chat_mod.mixin.chat_style;

import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.MOD_ID;

@Mixin(MappedRegistry.class)
public class MappedRegistryMixin<T> {
    @Inject(
            method = "validateWrite(Lnet/minecraft/resources/ResourceKey;)V",
            at = @At("HEAD"),
            require = 0,
            cancellable = true
    )
    private void validateWrite(ResourceKey<T> resourceKey, CallbackInfo ci) {
        if (resourceKey.identifier().getNamespace().equals(MOD_ID))
            ci.cancel();
    }
}

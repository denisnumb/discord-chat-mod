package com.denisnumb.discord_chat_mod.neoforge.mixin.compat;

import com.denisnumb.discord_chat_mod.MinecraftEvents;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "redstonedubstep.mods.vanishmod.VanishingHandler", remap = false)
public abstract class VanishmodVanishingHandlerMixin {
    @ModifyExpressionValue(
            method = "sendJoinOrLeaveMessageToPlayers",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/MutableComponent;withStyle(Lnet/minecraft/ChatFormatting;)Lnet/minecraft/network/chat/MutableComponent;"
            ),
            require = 0
    )
    private static MutableComponent discordChatMod$styleFakeJoinLeave(
            MutableComponent original,
            @Local(argsOnly = true) ServerPlayer sender,
            @Local(argsOnly = true, ordinal = 0) boolean leaveMessage
    ) {
        return MinecraftEvents.handleVanishFakeJoinLeave(sender, !leaveMessage)
                .map(Component::copy)
                .orElse(original);
    }
}

package com.denisnumb.discord_chat_mod.mixin.chat_style;

import com.denisnumb.discord_chat_mod.MinecraftEvents;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PlayerAdvancements.class)
public class PlayerAdvancementsMixin {
    @Shadow
    private ServerPlayer player;

    @ModifyArg(method = "method_53637(Lnet/minecraft/advancements/AdvancementHolder;Lnet/minecraft/advancements/DisplayInfo;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V",
                    ordinal = 0
            ),
            require = 0
    )
    public Component modifyAdvancementMessage(Component component, @Local(argsOnly = true) AdvancementHolder advancementHolder){
        return MinecraftEvents.handleAdvancementMade(player, advancementHolder).orElse(component);
    }
}

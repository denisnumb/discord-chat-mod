package com.denisnumb.discord_chat_mod.mixin.chat_style;

import com.denisnumb.discord_chat_mod.MinecraftEvents;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
    public ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @ModifyExpressionValue(
            method = "die",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/damagesource/CombatTracker;getDeathMessage()Lnet/minecraft/network/chat/Component;"
            ),
            require = 0
    )
    private Component modifyDeathMessage(Component original) {
        return MinecraftEvents.handlePlayerOrPetDie(((CombatTrackerAccessorMixin) getCombatTracker()).getEntries(), this)
                .orElse(original);
    }
}

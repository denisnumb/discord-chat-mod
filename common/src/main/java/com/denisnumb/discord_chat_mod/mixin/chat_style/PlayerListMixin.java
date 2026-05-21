package com.denisnumb.discord_chat_mod.mixin.chat_style;

import com.denisnumb.discord_chat_mod.MinecraftEvents;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;


@Mixin(PlayerList.class)
public class PlayerListMixin {
    @ModifyVariable(
            method = "placeNewPlayer",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;"
            ),
            require = 0
    )
    private MutableComponent modifyJoinMessage(MutableComponent value, @Local(argsOnly = true)ServerPlayer serverPlayer) {
        return MinecraftEvents.handleJoinLeave(serverPlayer, true).orElse(value).copy();
    }
}

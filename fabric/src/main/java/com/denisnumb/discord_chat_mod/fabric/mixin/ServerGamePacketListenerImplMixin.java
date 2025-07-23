package com.denisnumb.discord_chat_mod.fabric.mixin;

import com.denisnumb.discord_chat_mod.fabric.FabricEvents;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
    @Shadow public ServerPlayer player;

    @ModifyExpressionValue(
            method = "method_44900(Lnet/minecraft/network/protocol/game/ServerboundChatPacket;Ljava/util/Optional;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/PlayerChatMessage;decoratedContent()Lnet/minecraft/network/chat/Component;"
            ),
            require = 0
    )
    private Component redirectDecorate(Component original) {
        return FabricEvents.onChatMessage(player, original);
    }
}

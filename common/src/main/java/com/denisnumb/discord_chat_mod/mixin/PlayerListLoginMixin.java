package com.denisnumb.discord_chat_mod.mixin;

import com.denisnumb.discord_chat_mod.linking.features.JoinGate;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;

@Mixin(PlayerList.class)
public abstract class PlayerListLoginMixin {
    @Inject(method = "canPlayerLogin", at = @At("RETURN"), cancellable = true)
    private void discordChatMod$checkAccountLink(SocketAddress address, NameAndId nameAndId, CallbackInfoReturnable<Component> cir) {
        JoinGate.checkLogin(nameAndId.id(), nameAndId.name(), cir.getReturnValue())
                .ifPresent(cir::setReturnValue);
    }
}

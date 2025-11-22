package com.denisnumb.discord_chat_mod.mixin;

import com.denisnumb.discord_chat_mod.MinecraftClientEvents;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "handleLogin", at = @At("RETURN"))
    public void onGameJoinClient(ClientboundLoginPacket clientboundLoginPacket, CallbackInfo ci){
        MinecraftClientEvents.handleJoinServer();
    }

}

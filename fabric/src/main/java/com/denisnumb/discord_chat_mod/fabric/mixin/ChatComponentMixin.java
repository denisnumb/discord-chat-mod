package com.denisnumb.discord_chat_mod.fabric.mixin;

import com.denisnumb.discord_chat_mod.fabric.FabricClientEvents;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ChatComponent.class)
public class ChatComponentMixin {
    @ModifyArg(
            method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;ILnet/minecraft/client/GuiMessageTag;Z)V"
            ),
            index = 0,
            require = 0
    )
    private Component modifyComponent(Component component) {
        return FabricClientEvents.onChatMessage(component);
    }
}

package com.denisnumb.discord_chat_mod.mixin.chat_style;

import com.denisnumb.discord_chat_mod.MinecraftEvents;
import com.denisnumb.discord_chat_mod.chat_style.CustomChatTypeRegistry;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static com.denisnumb.discord_chat_mod.chat_style.CustomChatTypeRegistry.buildBound;
import static com.denisnumb.discord_chat_mod.chat_style.MinecraftChatStyleProvider.ChatMessageComponents;


@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin extends ServerCommonPacketListenerImpl {
    public ServerGamePacketListenerImplMixin(MinecraftServer minecraftServer, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraftServer, connection, commonListenerCookie);
    }

    @Shadow public ServerPlayer player;

    @ModifyExpressionValue(
            method = "method_44900(Lnet/minecraft/network/protocol/game/ServerboundChatPacket;Ljava/util/Optional;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/PlayerChatMessage;decoratedContent()Lnet/minecraft/network/chat/Component;"
            ),
            require = 0
    )
    private Component applyMarkdownToMessageText(Component original) {
        return MinecraftEvents.handleChatMessageText(player, original);
    }

    @ModifyArg(
            method = "removePlayerFromWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"
            ),
            require = 0
    )
    private Component modifyLeftMessage(Component original) {
        return MinecraftEvents.handleJoinLeave(player, false).orElse(original);
    }

    @ModifyArgs(
            method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V"
            ),
            require = 0
    )
    private void modifyPlayerChatMessage(Args args) {
        PlayerChatMessage originalMessage = args.get(0);
        Component playerComponent = player.getDisplayName();
        Component originalContent = originalMessage.decoratedContent();

        MinecraftEvents.handleChatMessage(
                CustomChatTypeRegistry.CHAT,
                new ChatMessageComponents(playerComponent, originalContent, null)
        ).ifPresent(styledContent -> {
            ChatType.Bound styledBound = buildBound(CustomChatTypeRegistry.CHAT, player.level().registryAccess(), playerComponent, originalContent);
            args.set(0, originalMessage.withUnsignedContent(styledContent));
            args.set(2, styledBound);
        });
    }
}

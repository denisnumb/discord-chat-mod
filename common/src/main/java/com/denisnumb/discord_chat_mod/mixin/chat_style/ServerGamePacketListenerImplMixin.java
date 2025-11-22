package com.denisnumb.discord_chat_mod.mixin.chat_style;

import com.denisnumb.discord_chat_mod.MinecraftEvents;
import com.denisnumb.discord_chat_mod.chat_style.CustomChatTypeRegistry;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

import static com.denisnumb.discord_chat_mod.chat_style.CustomChatTypeRegistry.buildBound;
import static com.denisnumb.discord_chat_mod.chat_style.MinecraftChatStyleProvider.ChatMessageComponents;


@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin  {
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
            method = "onDisconnect",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"
            ),
            require = 0
    )
    private Component modifyLeftMessage(Component original) {
        return MinecraftEvents.handleJoinLeave(player, false).orElse(original);
    }

    @Redirect(
            method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V"
            )
    )
    private void redirectBroadcastChatMessage(
            PlayerList playerList,
            PlayerChatMessage originalMessage,
            ServerPlayer player,
            ChatType.Bound bound
    ) {
        Component playerComponent = player.getDisplayName();
        Component originalContent = originalMessage.decoratedContent();

        Optional<Component> styledContentOpt = MinecraftEvents.handleChatMessage(
                CustomChatTypeRegistry.CHAT,
                new ChatMessageComponents(playerComponent, originalContent, null)
        );

        if (styledContentOpt.isPresent()){
            ChatType.Bound styledBound = buildBound(
                    CustomChatTypeRegistry.CHAT,
                    player.level().registryAccess(),
                    playerComponent,
                    originalContent
            );
            originalMessage = originalMessage.withUnsignedContent(styledContentOpt.get());
            bound = styledBound;
        }

        playerList.broadcastChatMessage(originalMessage, player, bound);
    }
}

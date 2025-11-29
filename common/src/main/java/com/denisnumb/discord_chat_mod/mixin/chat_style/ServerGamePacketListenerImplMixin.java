package com.denisnumb.discord_chat_mod.mixin.chat_style;

import com.denisnumb.discord_chat_mod.MinecraftEvents;
import com.denisnumb.discord_chat_mod.MinecraftUtils;
import com.denisnumb.discord_chat_mod.chat_style.CustomChatTypeRegistry;
import com.denisnumb.discord_chat_mod.discord.chat_style.DiscordChatStyleProvider;
import com.denisnumb.discord_chat_mod.discord.chat_style.MessageType;
import com.denisnumb.discord_chat_mod.discord.model.ChannelCategory;
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

import java.util.Map;
import java.util.Optional;

import static com.denisnumb.discord_chat_mod.MinecraftUtils.processChatMessage;
import static com.denisnumb.discord_chat_mod.chat_style.ChatStyleUtils.mergeMaps;
import static com.denisnumb.discord_chat_mod.chat_style.CustomChatTypeRegistry.buildBound;
import static com.denisnumb.discord_chat_mod.chat_style.MinecraftChatStyleProvider.ChatMessageComponents;
import static com.denisnumb.discord_chat_mod.chat_style.Parameters.MESSAGE;
import static com.denisnumb.discord_chat_mod.discord.DiscordChannelRegistry.getAllContexts;
import static com.denisnumb.discord_chat_mod.discord.DiscordUtils.handleDiscord;
import static com.denisnumb.discord_chat_mod.discord.DiscordUtils.sendMessageFromPlayer;
import static com.denisnumb.discord_chat_mod.discord.chat_style.DiscordChatStyleProvider.buildPlayerParameters;
import static com.denisnumb.discord_chat_mod.discord.chat_style.DiscordChatStyleProvider.getDiscordMessageComponents;


@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin  {
    @Shadow public ServerPlayer player;

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
            ),
            require = 0
    )
    private void redirectBroadcastChatMessage(
            PlayerList playerList,
            PlayerChatMessage originalMessage,
            ServerPlayer player,
            ChatType.Bound bound
    ) {
        Component playerComponent = player.getDisplayName();
        Component originalContent = originalMessage.decoratedContent();

        MinecraftUtils.ProcessChatMessageResult chatMessage = processChatMessage(originalContent.getString(), ChannelCategory.PLAYER_CHAT);

        handleDiscord(() -> {
            Map<String, String> parameters = mergeMaps(Map.of(MESSAGE, chatMessage.forDiscord()), buildPlayerParameters(player));
            Optional<DiscordChatStyleProvider.DiscordMessageComponents> chatComponentsOpt = getDiscordMessageComponents(MessageType.CHAT, parameters);
            Optional<DiscordChatStyleProvider.DiscordMessageComponents> webhookComponentsOpt = getDiscordMessageComponents(MessageType.CHAT_WEBHOOK, parameters);

            if (chatComponentsOpt.isPresent() && webhookComponentsOpt.isPresent())
                sendMessageFromPlayer(ChannelCategory.PLAYER_CHAT, getAllContexts(), player, webhookComponentsOpt.get(), chatComponentsOpt.get());
        });

        Component withMarkdown = chatMessage.forMinecraft();
        originalMessage = originalMessage.withUnsignedContent(withMarkdown);

        Optional<Component> styledContentOpt = MinecraftEvents.handleChatMessage(
                CustomChatTypeRegistry.CHAT,
                new ChatMessageComponents(playerComponent, withMarkdown, null)
        );

        if (styledContentOpt.isPresent()){
            ChatType.Bound styledBound = buildBound(
                    CustomChatTypeRegistry.CHAT,
                    player.level().registryAccess(),
                    playerComponent,
                    withMarkdown
            );
            originalMessage = originalMessage.withUnsignedContent(styledContentOpt.get());
            bound = styledBound;
        }

        playerList.broadcastChatMessage(originalMessage, player, bound);
    }
}

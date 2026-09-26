package com.denisnumb.discord_chat_mod.chat;

import com.denisnumb.discord_chat_mod.MinecraftEvents;
import com.denisnumb.discord_chat_mod.chat.template.MessageTemplate;
import com.denisnumb.discord_chat_mod.chat.template.MessageTypes;
import com.denisnumb.discord_chat_mod.config.ConfigDefaults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.scores.PlayerTeam;
import org.slf4j.Logger;

import java.util.List;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.server;
import static com.denisnumb.discord_chat_mod.utils.MinecraftUtils.getPlayerListBySelector;

public final class MinecraftMessageSender {
    private MinecraftMessageSender() {}

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Style TEAMMSG_SUGGEST_STYLE = Style.EMPTY
            .withHoverEvent(new HoverEvent.ShowText(Component.translatable("chat.type.team.hover")))
            .withClickEvent(new ClickEvent.SuggestCommand("/teammsg "));

    private static final MessageTemplate<MessageTypes.PlayerMessage> PLAYER_MESSAGE_TEMPLATE
            = new MessageTemplate<>(ConfigDefaults.MINECRAFT_PLAYER_MESSAGE_STYLE_DEFAULT, new MessageTypes.PlayerMessage());

    private static final MessageTemplate<MessageTypes.TellOutgoingMessage> TELL_MESSAGE_OUTGOING_TEMPLATE
            = new MessageTemplate<>(ConfigDefaults.MINECRAFT_TELL_MESSAGE_SENT_STYLE_DEFAULT, new MessageTypes.TellOutgoingMessage());

    private static final MessageTemplate<MessageTypes.TellIncomingMessage> TELL_MESSAGE_INCOMING_TEMPLATE
            = new MessageTemplate<>(ConfigDefaults.MINECRAFT_TELL_MESSAGE_RECEIVED_STYLE_DEFAULT, new MessageTypes.TellIncomingMessage());

    private static final MessageTemplate<MessageTypes.TeamMessage> TEAM_MESSAGE_OUTGOING_TEMPLATE
            = new MessageTemplate<>(ConfigDefaults.MINECRAFT_TEAM_MESSAGE_SENT_STYLE_DEFAULT, new MessageTypes.TeamMessage());

    private static final MessageTemplate<MessageTypes.TeamMessage> TEAM_MESSAGE_INCOMING_TEMPLATE
            = new MessageTemplate<>(ConfigDefaults.MINECRAFT_TEAM_MESSAGE_RECEIVED_STYLE_DEFAULT, new MessageTypes.TeamMessage());

    public static void sendSystemMessageToPlayersBySelector(Component message, String selector) {
        try {
            for (ServerPlayer player : getPlayerListBySelector(selector))
                player.sendSystemMessage(ComponentUtils.updateForEntity(null, message, player, 0), false);
        } catch (CommandSyntaxException e) {
            LOGGER.error("CommandSyntaxException", e);
        } catch (Exception ignored) {}
    }

    public static void sendSystemMessageToAllPlayers(Component message) {
        sendSystemMessageToPlayersBySelector(message, "@a");
    }

    public static void sendMessageToAllPlayersFromPlayer(ServerPlayer player, Component content){
        PlayerList playerList = server.getPlayerList();
        if (playerList == null)
            return;

        MinecraftMessageContext ctx = new MinecraftMessageContext(player.getDisplayName(), content, null, player);
        Component preparedContent = MinecraftEvents.handleChatMessage(CustomChatTypeRegistry.CHAT, ctx)
                .orElseGet(() -> PLAYER_MESSAGE_TEMPLATE.applyParameters(new MessageTypes.PlayerMessage.Params(ctx)));

        try {
            for (ServerPlayer serverPlayer : playerList.getPlayers())
                serverPlayer.sendSystemMessage(preparedContent);
        } catch (Exception ignored) {}
    }

    public static void sendTellMessageToTargetPlayersFromPlayer(
            ServerPlayer player,
            List<ServerPlayer> targetPlayers,
            Component content,
            boolean singleOutgoing
    ){
        if (targetPlayers.isEmpty())
            return;

        MinecraftMessageContext incomingCtx = new MinecraftMessageContext(player.getDisplayName(), content, null, player);
        Component preparedContent = MinecraftEvents.handleChatMessage(CustomChatTypeRegistry.MSG_COMMAND_INCOMING, incomingCtx)
                .orElseGet(() -> TELL_MESSAGE_INCOMING_TEMPLATE.applyParameters(new MessageTypes.TellIncomingMessage.Params(incomingCtx)));

        for (ServerPlayer serverPlayer : targetPlayers) {
            serverPlayer.sendSystemMessage(preparedContent);
        }

        if (singleOutgoing) {
            Component receivers = targetPlayers.stream()
                    .map(ServerPlayer::getDisplayName)
                    .reduce((a, b) -> Component.literal("").append(a).append(", ").append(b))
                    .orElse(Component.empty());

            MinecraftMessageContext outgoingCtx = new MinecraftMessageContext(receivers, content, null, player);
            Component outgoingContent = MinecraftEvents.handleChatMessage(CustomChatTypeRegistry.MSG_COMMAND_OUTGOING, outgoingCtx)
                    .orElseGet(() -> TELL_MESSAGE_OUTGOING_TEMPLATE.applyParameters(new MessageTypes.TellOutgoingMessage.Params(outgoingCtx)));

            player.sendSystemMessage(outgoingContent);
        } else {
            for (ServerPlayer serverPlayer : targetPlayers) {
                MinecraftMessageContext outgoingCtx = new MinecraftMessageContext(serverPlayer.getDisplayName(), content, null, player);
                Component outgoingContent = MinecraftEvents.handleChatMessage(CustomChatTypeRegistry.MSG_COMMAND_OUTGOING, outgoingCtx)
                        .orElseGet(() -> TELL_MESSAGE_OUTGOING_TEMPLATE.applyParameters(new MessageTypes.TellOutgoingMessage.Params(outgoingCtx)));

                player.sendSystemMessage(outgoingContent);
            }
        }
    }

    public static void sendTeamMessageFromPlayer(ServerPlayer player, PlayerTeam team, Component content){
        PlayerList playerList = server.getPlayerList();
        if (playerList == null)
            return;

        Component teamDisplayName = team.getFormattedDisplayName().withStyle(TEAMMSG_SUGGEST_STYLE);
        MinecraftMessageContext ctx = new MinecraftMessageContext(player.getDisplayName(), content, teamDisplayName, player);

        Component preparedContent = MinecraftEvents.handleChatMessage(CustomChatTypeRegistry.TEAM_MSG_COMMAND_INCOMING, ctx).orElse(
                TEAM_MESSAGE_INCOMING_TEMPLATE.applyParameters(new MessageTypes.TeamMessage.Params(ctx))
        );

        for (String playerName : team.getPlayers()){
            ServerPlayer serverPlayer = playerList.getPlayerByName(playerName);
            if (serverPlayer == null || player.equals(serverPlayer))
                continue;

            serverPlayer.sendSystemMessage(preparedContent);
        }

        Component outgoingContent = MinecraftEvents.handleChatMessage(CustomChatTypeRegistry.TEAM_MSG_COMMAND_OUTGOING, ctx).orElse(
                TEAM_MESSAGE_OUTGOING_TEMPLATE.applyParameters(new MessageTypes.TeamMessage.Params(ctx))
        );

        player.sendSystemMessage(outgoingContent);
    }
}

package com.denisnumb.discord_chat_mod.chat;

import com.denisnumb.discord_chat_mod.MinecraftEvents;
import com.denisnumb.discord_chat_mod.chat.template.TemplateFactory;
import com.denisnumb.discord_chat_mod.config.ConfigDefaults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.scores.PlayerTeam;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.server;
import static com.denisnumb.discord_chat_mod.chat.template.TemplateFactory.applyParametersToTemplate;
import static com.denisnumb.discord_chat_mod.chat.template.TemplateFactory.parseConfigTemplateMarkdown;
import static com.denisnumb.discord_chat_mod.chat.template.TemplateParameter.*;
import static com.denisnumb.discord_chat_mod.chat.template.TemplateParameter.MESSAGE;
import static com.denisnumb.discord_chat_mod.chat.template.TemplateParameter.Translatable.COMMANDS_MESSAGE_DISPLAY_INCOMING;
import static com.denisnumb.discord_chat_mod.chat.template.TemplateParameter.Translatable.COMMANDS_MESSAGE_DISPLAY_OUTGOING;
import static com.denisnumb.discord_chat_mod.utils.JavaUtils.newLinkedHashMapOf;
import static com.denisnumb.discord_chat_mod.utils.MinecraftUtils.getPlayerListBySelector;

public final class MinecraftMessageSender {
    private MinecraftMessageSender() {}

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Style TEAMMSG_SUGGEST_STYLE = Style.EMPTY
            .withHoverEvent(new HoverEvent.ShowText(Component.translatable("chat.type.team.hover")))
            .withClickEvent(new ClickEvent.SuggestCommand("/teammsg "));

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

        Component preparedContent = MinecraftEvents.handleChatMessage(
                CustomChatTypeRegistry.CHAT,
                new MinecraftMessageContext(player.getDisplayName(), content, null, player)
        ).orElseGet(() -> applyParametersToTemplate(
                parseConfigTemplateMarkdown(ConfigDefaults.MINECRAFT_PLAYER_MESSAGE_STYLE_DEFAULT),
                Map.of(PLAYER, player.getDisplayName(), MESSAGE, content)
        ));

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

        Component preparedContent = MinecraftEvents.handleChatMessage(
                CustomChatTypeRegistry.MSG_COMMAND_INCOMING,
                new MinecraftMessageContext(player.getDisplayName(), content, null, player)
        ).orElseGet(() -> TemplateFactory.getStyledTranslatableMessage(
                parseConfigTemplateMarkdown(ConfigDefaults.MINECRAFT_TELL_MESSAGE_RECEIVED_STYLE_DEFAULT),
                COMMANDS_MESSAGE_DISPLAY_INCOMING,
                newLinkedHashMapOf(
                        Map.entry(SENDER, player.getDisplayName()),
                        Map.entry(MESSAGE, content)
                ),
                Map.of()
        ));

        for (ServerPlayer serverPlayer : targetPlayers) {
            serverPlayer.sendSystemMessage(preparedContent);
        }

        if (singleOutgoing) {
            Component receivers = targetPlayers.stream()
                    .map(ServerPlayer::getDisplayName)
                    .reduce((a, b) -> Component.literal("").append(a).append(", ").append(b))
                    .orElse(Component.empty());

            Component outgoingContent = MinecraftEvents.handleChatMessage(
                    CustomChatTypeRegistry.MSG_COMMAND_OUTGOING,
                    new MinecraftMessageContext(receivers, content, null, player)
            ).orElseGet(() -> TemplateFactory.getStyledTranslatableMessage(
                    parseConfigTemplateMarkdown(ConfigDefaults.MINECRAFT_TELL_MESSAGE_SENT_STYLE_DEFAULT),
                    COMMANDS_MESSAGE_DISPLAY_OUTGOING,
                    newLinkedHashMapOf(
                            Map.entry(RECEIVER, receivers),
                            Map.entry(MESSAGE, content)
                    ),
                    Map.of()
            ));

            player.sendSystemMessage(outgoingContent);
        } else {
            for (ServerPlayer serverPlayer : targetPlayers) {
                Component outgoingContent = MinecraftEvents.handleChatMessage(
                        CustomChatTypeRegistry.MSG_COMMAND_OUTGOING,
                        new MinecraftMessageContext(serverPlayer.getDisplayName(), content, null, player)
                ).orElseGet(() -> TemplateFactory.getStyledTranslatableMessage(
                        parseConfigTemplateMarkdown(ConfigDefaults.MINECRAFT_TELL_MESSAGE_SENT_STYLE_DEFAULT),
                        COMMANDS_MESSAGE_DISPLAY_OUTGOING,
                        newLinkedHashMapOf(
                                Map.entry(RECEIVER, serverPlayer.getDisplayName()),
                                Map.entry(MESSAGE, content)
                        ),
                        Map.of()
                ));

                player.sendSystemMessage(outgoingContent);
            }
        }
    }

    public static void sendTeamMessageFromPlayer(ServerPlayer player, PlayerTeam team, Component content){
        PlayerList playerList = server.getPlayerList();
        if (playerList == null)
            return;

        Component teamDisplayName = team.getFormattedDisplayName().withStyle(TEAMMSG_SUGGEST_STYLE);
        MinecraftMessageContext chatMessageComponents
                = new MinecraftMessageContext(player.getDisplayName(), content, teamDisplayName, player);

        Component preparedContent = MinecraftEvents.handleChatMessage(CustomChatTypeRegistry.TEAM_MSG_COMMAND_INCOMING, chatMessageComponents).orElse(
                applyParametersToTemplate(
                        parseConfigTemplateMarkdown(ConfigDefaults.MINECRAFT_TEAM_MESSAGE_RECEIVED_STYLE_DEFAULT),
                        Map.of(TEAM, teamDisplayName, PLAYER, player.getDisplayName(), MESSAGE, content)
                )
        );

        for (String playerName : team.getPlayers()){
            ServerPlayer serverPlayer = playerList.getPlayerByName(playerName);
            if (serverPlayer == null || player.equals(serverPlayer))
                continue;

            serverPlayer.sendSystemMessage(preparedContent);
        }

        Component outgoingContent = MinecraftEvents.handleChatMessage(CustomChatTypeRegistry.TEAM_MSG_COMMAND_OUTGOING, chatMessageComponents).orElse(
                applyParametersToTemplate(
                        parseConfigTemplateMarkdown(ConfigDefaults.MINECRAFT_TEAM_MESSAGE_SENT_STYLE_DEFAULT),
                        Map.of(TEAM, teamDisplayName, PLAYER, player.getDisplayName(), MESSAGE, content)
                )
        );

        player.sendSystemMessage(outgoingContent);
    }
}

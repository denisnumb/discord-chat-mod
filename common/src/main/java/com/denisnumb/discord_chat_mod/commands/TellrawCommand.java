package com.denisnumb.discord_chat_mod.commands;

import com.denisnumb.discord_chat_mod.discord.DiscordChannelRegistry;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.isDiscordConnected;
import static com.denisnumb.discord_chat_mod.discord.DiscordUtils.*;

public class TellrawCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context){
        dispatcher.register(
                Commands.literal("tellraw")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("message", ComponentArgument.textComponent())
                                        .executes(ctx -> {
                                            Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "targets");
                                            Component message = ComponentArgument.getComponent(ctx, "message");

                                            if (isDiscordConnected() && ctx.getInput().split(" ", 3)[1].equals("@a")) {
                                                StringBuilder messageTextBuilder = new StringBuilder();
                                                for (Component comp : message.toFlatList())
                                                    messageTextBuilder.append(applyStyles(comp.getStyle(), comp.getString()));
                                                sendTextMessage(
                                                        DiscordChannelRegistry.tellrawChannel,
                                                        null, null,
                                                        replaceEmojiCodesToDiscordMentions(messageTextBuilder.toString())
                                                );
                                            }

                                            for (ServerPlayer player : players)
                                                player.sendSystemMessage(ComponentUtils.updateForEntity(ctx.getSource(), message, player, 0), false);
                                            return players.size();
                                        })
                                )
                        )
        );
    }

    private static String applyStyles(Style style, String translatedText){
        if (style.isBold()) translatedText = "**" + translatedText + "**";
        if (style.isItalic()) translatedText = "*" + translatedText + "*";
        if (style.isStrikethrough()) translatedText = "~~" + translatedText + "~~";
        if (style.isUnderlined()) translatedText = "__" + translatedText + "__";
        if (style.isObfuscated()) translatedText = "||" + translatedText + "||";

        return translatedText;
    }
}

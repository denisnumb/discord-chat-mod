package com.denisnumb.discord_chat_mod.commands;

import com.denisnumb.discord_chat_mod.discord.DiscordChannelRegistry;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.isDiscordConnected;
import static com.denisnumb.discord_chat_mod.discord.DiscordUtils.*;

public class SayCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(
                Commands.literal("say")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("message", MessageArgument.message())
                                .executes(context -> {
                                    MessageArgument.resolveChatMessage(context, "message", (resolvedMessage) -> {
                                        CommandSourceStack source = context.getSource();
                                        if (isDiscordConnected()){
                                            String message = resolvedMessage.decoratedContent().getString();
                                            sendTextMessage(
                                                    DiscordChannelRegistry.sayChannel,
                                                    null, null,
                                                    String.format("`[%s]` %s", source.getDisplayName().getString(), replaceEmojiCodesToDiscordMentions(message))
                                            );

                                        }

                                        source.getServer().getPlayerList().broadcastChatMessage(resolvedMessage, source, ChatType.bind(ChatType.SAY_COMMAND, source));
                                    });
                                    return 1;
                                })
                        )
        );
    }
}

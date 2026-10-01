package com.denisnumb.discord_chat_mod.commands.vanilla;

import com.denisnumb.discord_chat_mod.MinecraftEvents;
import com.denisnumb.discord_chat_mod.chat.CommonMessageFormatter;
import com.denisnumb.discord_chat_mod.chat.MinecraftMessageContext;
import com.denisnumb.discord_chat_mod.chat.CustomChatTypeRegistry;
import com.denisnumb.discord_chat_mod.discord.chat.DiscordMessageFormatter;
import com.denisnumb.discord_chat_mod.discord.chat.DiscordMessageSender;
import com.denisnumb.discord_chat_mod.discord.model.ChannelCategory;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;

import static com.denisnumb.discord_chat_mod.chat.CommonMessageFormatter.formatMessage;
import static com.denisnumb.discord_chat_mod.chat.CustomChatTypeRegistry.buildBound;
import static com.denisnumb.discord_chat_mod.discord.DiscordChannelRegistry.getAllContexts;
import static com.denisnumb.discord_chat_mod.discord.chat.DiscordMessageSender.*;

public final class SayCommand {
    private SayCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(
                Commands.literal("say")
                        .requires(Commands.hasPermission(Commands.LEVEL_MODERATORS))
                        .then(Commands.argument("message", MessageArgument.message())
                                .executes(context -> {
                                    MessageArgument.resolveChatMessage(context, "message", (resolvedMessage) -> {
                                        CommandSourceStack source = context.getSource();
                                        CommonMessageFormatter.FormattedMessage chatMessage
                                                = formatMessage(resolvedMessage.decoratedContent().getString(), ChannelCategory.SAY_COMMAND);

                                        handleDiscord(() -> DiscordMessageSender.sendMessageFromServer(
                                                ChannelCategory.SAY_COMMAND,
                                                getAllContexts(),
                                                DiscordMessageFormatter.formatSayCommandMessage(source, chatMessage.forDiscord())
                                        ));

                                        Component senderComponent = source.getDisplayName();
                                        Component messageContent = chatMessage.forMinecraft();

                                        MinecraftEvents.handleChatMessage(
                                                CustomChatTypeRegistry.SAY_COMMAND,
                                                new MinecraftMessageContext(senderComponent, messageContent, null, source.getEntity())
                                        ).ifPresentOrElse(
                                                styledContent -> {
                                                    ChatType.Bound styledBound = buildBound(CustomChatTypeRegistry.SAY_COMMAND, source.registryAccess(), senderComponent, messageContent);
                                                    PlayerChatMessage styledWithMarkdown = resolvedMessage.withUnsignedContent(styledContent);
                                                    source.getServer().getPlayerList().broadcastChatMessage(styledWithMarkdown, source, styledBound);
                                                },
                                                () -> {
                                                    PlayerChatMessage withMarkdown = resolvedMessage.withUnsignedContent(messageContent);
                                                    source.getServer().getPlayerList().broadcastChatMessage(withMarkdown, source, ChatType.bind(ChatType.SAY_COMMAND, source));
                                                }
                                        );
                                    });
                                    return 1;
                                })
                        )
        );
    }
}

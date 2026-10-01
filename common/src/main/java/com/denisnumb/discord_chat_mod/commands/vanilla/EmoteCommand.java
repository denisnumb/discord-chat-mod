package com.denisnumb.discord_chat_mod.commands.vanilla;

import com.denisnumb.discord_chat_mod.MinecraftEvents;
import com.denisnumb.discord_chat_mod.chat.CommonMessageFormatter;
import com.denisnumb.discord_chat_mod.chat.MinecraftMessageContext;
import com.denisnumb.discord_chat_mod.discord.chat.DiscordMessageFormatter;
import com.denisnumb.discord_chat_mod.chat.CustomChatTypeRegistry;
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
import static com.denisnumb.discord_chat_mod.discord.chat.DiscordMessageSender.handleDiscord;

public final class EmoteCommand {
    private EmoteCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register(Commands.literal("me").then(Commands.argument("action", MessageArgument.message()).executes((commandContext) -> {
            MessageArgument.resolveChatMessage(commandContext, "action", (playerChatMessage) -> {
                CommandSourceStack source = commandContext.getSource();

                CommonMessageFormatter.FormattedMessage chatMessage
                        = formatMessage(playerChatMessage.decoratedContent().getString(), ChannelCategory.ME_COMMAND);

                handleDiscord(() -> DiscordMessageSender.sendMessageFromPlayer(
                        ChannelCategory.ME_COMMAND,
                        getAllContexts(),
                        source.getPlayer(),
                        DiscordMessageFormatter.formatMeCommandMessage(source, chatMessage.forDiscord())
                ));

                Component senderComponent = source.getDisplayName();
                Component messageContent = chatMessage.forMinecraft();

                MinecraftEvents.handleChatMessage(
                        CustomChatTypeRegistry.EMOTE_COMMAND,
                        new MinecraftMessageContext(senderComponent, messageContent, null, source.getEntity())
                ).ifPresentOrElse(
                        styledContent -> {
                            ChatType.Bound styledBound = buildBound(CustomChatTypeRegistry.EMOTE_COMMAND, source.registryAccess(), senderComponent, messageContent);
                            PlayerChatMessage styledWithMarkdown = playerChatMessage.withUnsignedContent(styledContent);
                            source.getServer().getPlayerList().broadcastChatMessage(styledWithMarkdown, source, styledBound);
                        },
                        () -> {
                            PlayerChatMessage withMarkdown = playerChatMessage.withUnsignedContent(messageContent);
                            source.getServer().getPlayerList().broadcastChatMessage(withMarkdown, source, ChatType.bind(ChatType.EMOTE_COMMAND, source));
                        }
                );
            });
            return 1;
        })));
    }
}

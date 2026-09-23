package com.denisnumb.discord_chat_mod.commands.vanilla;

import com.denisnumb.discord_chat_mod.MinecraftEvents;
import com.denisnumb.discord_chat_mod.chat.CommonMessageFormatter;
import com.denisnumb.discord_chat_mod.chat.MinecraftMessageContext;
import com.denisnumb.discord_chat_mod.discord.chat.DiscordMessageComponents;
import com.denisnumb.discord_chat_mod.chat.CustomChatTypeRegistry;
import com.denisnumb.discord_chat_mod.discord.chat.DiscordMessageSender;
import com.denisnumb.discord_chat_mod.discord.model.MessageType;
import com.denisnumb.discord_chat_mod.discord.model.ChannelCategory;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;

import java.util.Map;
import java.util.Optional;

import static com.denisnumb.discord_chat_mod.chat.template.TemplateParameterFactory.buildPlayerParameters;
import static com.denisnumb.discord_chat_mod.discord.chat.DiscordMessageFormatter.getDiscordMessageComponents;
import static com.denisnumb.discord_chat_mod.chat.CommonMessageFormatter.formatMessage;
import static com.denisnumb.discord_chat_mod.utils.JavaUtils.mergeMaps;
import static com.denisnumb.discord_chat_mod.chat.CustomChatTypeRegistry.buildBound;
import static com.denisnumb.discord_chat_mod.discord.DiscordChannelRegistry.getAllContexts;
import static com.denisnumb.discord_chat_mod.discord.chat.DiscordMessageSender.handleDiscord;
import static com.denisnumb.discord_chat_mod.chat.template.TemplateParameters.MESSAGE;

public final class EmoteCommand {
    private EmoteCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register(Commands.literal("me").then(Commands.argument("action", MessageArgument.message()).executes((commandContext) -> {
            MessageArgument.resolveChatMessage(commandContext, "action", (playerChatMessage) -> {
                CommandSourceStack source = commandContext.getSource();

                CommonMessageFormatter.FormattedMessage chatMessage
                        = formatMessage(playerChatMessage.decoratedContent().getString(), ChannelCategory.ME_COMMAND);

                handleDiscord(() -> {
                    Map<String, String> parameters = mergeMaps(Map.of(MESSAGE, chatMessage.forDiscord()), buildPlayerParameters(source));
                    Optional<DiscordMessageComponents> chatComponentsOpt = getDiscordMessageComponents(MessageType.ME_COMMAND, parameters);
                    Optional<DiscordMessageComponents> webhookComponentsOpt = getDiscordMessageComponents(MessageType.ME_COMMAND_WEBHOOK, parameters);

                    if (chatComponentsOpt.isPresent() && webhookComponentsOpt.isPresent())
                        DiscordMessageSender.sendMessageFromPlayer(ChannelCategory.ME_COMMAND, getAllContexts(), source.getPlayer(), webhookComponentsOpt.get(), chatComponentsOpt.get());
                });

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

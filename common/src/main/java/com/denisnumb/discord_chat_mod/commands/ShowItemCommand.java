package com.denisnumb.discord_chat_mod.commands;

import com.denisnumb.discord_chat_mod.discord.chat.DiscordMessageFormatter;
import com.denisnumb.discord_chat_mod.discord.chat.model.DiscordMessageComponents;
import com.denisnumb.discord_chat_mod.discord.chat.model.DiscordMessageBody;
import com.denisnumb.discord_chat_mod.discord.model.ChannelCategory;
import com.denisnumb.discord_chat_mod.locale.MinecraftLocaleProvider;
import com.denisnumb.discord_chat_mod.utils.ItemDisplayUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import static com.denisnumb.discord_chat_mod.chat.MinecraftMessageSender.sendMessageToAllPlayersFromPlayer;
import static com.denisnumb.discord_chat_mod.discord.DiscordGuildRegistry.getAllContexts;
import static com.denisnumb.discord_chat_mod.discord.chat.DiscordMessageSender.handleDiscord;
import static com.denisnumb.discord_chat_mod.discord.chat.DiscordMessageSender.sendMessageFromPlayer;

public final class ShowItemCommand {
    private ShowItemCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("show_item")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            ItemStack stack = ItemDisplayUtils.getDisplayedItem(player);

                            if (stack.isEmpty())
                                throw new SimpleCommandExceptionType(MinecraftLocaleProvider.Command.ShowItem.Error.emptyHand()).create();

                            sendMessageToAllPlayersFromPlayer(player, ItemDisplayUtils.getHoverableName(stack));

                            handleDiscord(() -> {
                                DiscordMessageBody body = DiscordMessageFormatter.formatItemDisplayMessage(player, stack);
                                sendMessageFromPlayer(ChannelCategory.PLAYER_CHAT, getAllContexts(), player, new DiscordMessageComponents(body, body));
                            });

                            return 1;
                        })
        );
    }
}

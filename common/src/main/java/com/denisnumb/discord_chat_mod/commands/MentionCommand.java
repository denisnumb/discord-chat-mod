package com.denisnumb.discord_chat_mod.commands;

import com.denisnumb.discord_chat_mod.discord.data_providers.ChannelMembersProvider;
import com.denisnumb.discord_chat_mod.discord.utils.DiscordMessageUtils;
import com.denisnumb.discord_chat_mod.discord.chat_style.MessageType;
import com.denisnumb.discord_chat_mod.discord.model.ChannelCategory;
import com.denisnumb.discord_chat_mod.discord.model.DiscordUserData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.denisnumb.discord_chat_mod.LocaleProvider.getTranslate;
import static com.denisnumb.discord_chat_mod.DiscordChatMod.isDiscordConnected;
import static com.denisnumb.discord_chat_mod.MinecraftUtils.sendMessageToAllPlayersFromPlayer;
import static com.denisnumb.discord_chat_mod.ModLanguageKey.SERVER_IS_NOT_CONNECTED_TO_DISCORD;
import static com.denisnumb.discord_chat_mod.ModLanguageKey.UNKNOWN_MENTION;
import static com.denisnumb.discord_chat_mod.chat_style.ChatStyleUtils.mergeMaps;
import static com.denisnumb.discord_chat_mod.discord.DiscordChannelRegistry.*;
import static com.denisnumb.discord_chat_mod.discord.chat_style.DiscordChatStyleProvider.*;
import static com.denisnumb.discord_chat_mod.chat_style.Parameters.MESSAGE;

public class MentionCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("mention")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .suggests(MENTIONS_PROVIDER)
                                .executes(context -> {
                                    String name = StringArgumentType.getString(context, "name");
                                    List<DiscordUserData> memberData = ChannelMembersProvider.getMemberData(ChannelCategory.PLAYER_CHAT);

                                    if (!isDiscordConnected()) {
                                        throw new SimpleCommandExceptionType(Component.literal(
                                                getTranslate(SERVER_IS_NOT_CONNECTED_TO_DISCORD)
                                        )).create();
                                    }

                                    Optional<DiscordUserData> optionalMemberData = memberData.stream()
                                            .filter(data -> data.prettyMention.substring(1).equals(name))
                                            .findFirst();

                                    if (optionalMemberData.isEmpty()) {
                                        throw new SimpleCommandExceptionType(Component.literal(String.format(
                                                getTranslate(UNKNOWN_MENTION), name
                                        ))).create();
                                    }

                                    if (context.getSource().getEntity() instanceof ServerPlayer player) {
                                        DiscordUserData member = optionalMemberData.get();

                                        Component mentionComponent = Component.literal(String.format("@%s", name))
                                                .withStyle(style -> style.withColor(TextColor.parseColor(member.color).getOrThrow())
                                                        .withInsertion("@" + name)
                                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(member.discordName)))
                                                );

                                        sendMessageToAllPlayersFromPlayer(player, mentionComponent);

                                        Map<String, String> parameters = mergeMaps(Map.of(MESSAGE, member.mentionString), buildPlayerParameters(player));
                                        Optional<DiscordMessageComponents> chatComponentsOpt = getDiscordMessageComponents(MessageType.CHAT, parameters);
                                        Optional<DiscordMessageComponents> webhookComponentsOpt = getDiscordMessageComponents(MessageType.CHAT_WEBHOOK, parameters);

                                        if (chatComponentsOpt.isPresent() && webhookComponentsOpt.isPresent())
                                            DiscordMessageUtils.sendMessageFromPlayer(ChannelCategory.PLAYER_CHAT, getAllContexts(), player, webhookComponentsOpt.get(), chatComponentsOpt.get());
                                    }
                                    return 1;
                                })
                        )
        );
    }

    private static boolean matchesPartial(DiscordUserData member, String partial) {
        return member.displayName.toLowerCase().contains(partial)
                || member.discordName.toLowerCase().contains(partial);
    }

    private static final SuggestionProvider<CommandSourceStack> MENTIONS_PROVIDER = (context, builder) -> {
        if (isDiscordConnected()) {
            String partial = builder.getRemaining().toLowerCase();
            StringRange range = StringRange.between(builder.getStart(), builder.getInput().length());

            List<Suggestion> suggestions = ChannelMembersProvider.getMemberData(ChannelCategory.PLAYER_CHAT)
                    .stream()
                    .filter(data -> matchesPartial(data, partial))
                    .map(data -> new Suggestion(range, data.getPrettyMention().substring(1)))
                    .toList();

            return CompletableFuture.completedFuture(new Suggestions(range, suggestions));
        }

        return builder.buildFuture();
    };
}
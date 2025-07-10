package com.denisnumb.discord_chat_mod.commands;

import com.denisnumb.discord_chat_mod.discord.DiscordUtils;
import com.denisnumb.discord_chat_mod.discord.model.DiscordMemberData;
import com.denisnumb.discord_chat_mod.discord.ChannelMembersProvider;
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
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.*;
import static com.denisnumb.discord_chat_mod.MinecraftUtils.sendMessageToAllPlayers;
import static com.denisnumb.discord_chat_mod.ModLanguageKey.SERVER_IS_NOT_CONNECTED_TO_DISCORD;
import static com.denisnumb.discord_chat_mod.ModLanguageKey.UNKNOWN_MENTION;
import static com.denisnumb.discord_chat_mod.MinecraftUtils.getTranslate;
import static com.denisnumb.discord_chat_mod.discord.DiscordUtils.prepareDiscordTextMessage;

public class MentionCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("mention")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .suggests(MENTIONS_PROVIDER)
                                .executes(context -> {
                                    String name = StringArgumentType.getString(context, "name");
                                    List<DiscordMemberData> memberData = ChannelMembersProvider.getMemberData(discordChannel);

                                    if (!isDiscordConnected()){
                                        throw new SimpleCommandExceptionType(Component.literal(
                                                getTranslate(SERVER_IS_NOT_CONNECTED_TO_DISCORD, "Server is not connected to discord")
                                        )).create();
                                    }

                                    Optional<DiscordMemberData> optionalMemberData = memberData.stream()
                                            .filter(data -> data.guildNickname.equals(name))
                                            .findFirst();

                                    if (optionalMemberData.isEmpty()) {
                                        throw new SimpleCommandExceptionType(Component.literal(String.format(
                                                getTranslate(UNKNOWN_MENTION, "There is no user with name %s in the channel #%s"),
                                                name,
                                                discordChannel.getName()
                                        ))).create();
                                    }

                                    if (context.getSource().getEntity() instanceof Player player){
                                        DiscordMemberData member = optionalMemberData.get();

                                        sendMessageToAllPlayers(
                                                Component.literal(String.format("<%s> ", player.getName().getString()))
                                                        .append(Component.literal(String.format("@%s", name)).withStyle(style ->
                                                                style.withColor(TextColor.parseColor(member.color).getOrThrow())
                                                                        .withInsertion("@" + name)
                                                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(member.discordNickName)))
                                                        ))
                                        );

                                        prepareDiscordTextMessage(String.format("`<%s>` %s", player.getName().getString(), member.mentionString))
                                                .ifPresent(DiscordUtils::sendMessage);
                                    }
                                    return 1;
                                })
                        )
        );
    }

    private static boolean matchesPartial(DiscordMemberData member, String partial) {
        return member.guildNickname.toLowerCase().contains(partial)
                || member.discordNickName.toLowerCase().contains(partial)
                || member.discordName.toLowerCase().contains(partial);
    }

    private static final SuggestionProvider<CommandSourceStack> MENTIONS_PROVIDER = (context, builder) -> {
        if (isDiscordConnected()){
            String partial = builder.getRemaining().toLowerCase();
            StringRange range = StringRange.between(builder.getStart(), builder.getInput().length());

            List<Suggestion> suggestions = ChannelMembersProvider.getMemberData(discordChannel)
                    .stream()
                    .filter(data -> matchesPartial(data, partial))
                    .map(data -> new Suggestion(range, data.getGuildNickname()))
                    .toList();

            return CompletableFuture.completedFuture(new Suggestions(range, suggestions));
        }

        return builder.buildFuture();
    };
}
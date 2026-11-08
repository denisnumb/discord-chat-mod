package com.denisnumb.discord_chat_mod.linking.commands;

import com.denisnumb.discord_chat_mod.linking.AccountLinkService;
import com.denisnumb.discord_chat_mod.linking.AccountLinking;
import com.denisnumb.discord_chat_mod.linking.LinkCodeManager;
import com.denisnumb.discord_chat_mod.linking.discord.LinkingDiscordUtils;
import com.denisnumb.discord_chat_mod.linking.model.AccountLink;
import com.denisnumb.discord_chat_mod.linking.model.UnlinkReason;
import com.denisnumb.discord_chat_mod.locale.MinecraftLocaleProvider;
import com.denisnumb.discord_chat_mod.utils.NameComponentUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.isDiscordConnected;


public final class DiscordLinkCommand {
    private DiscordLinkCommand() {}

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneOffset.UTC);

    private static final SuggestionProvider<CommandSourceStack> LINKED_PLAYER_SUGGESTIONS = (context, builder) ->
            SharedSuggestionProvider.suggest(AccountLinkService.getAll().stream().map(AccountLink::name), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("discord")
                .then(Commands.literal("link").executes(ctx -> link(ctx.getSource())))
                .then(Commands.literal("unlink").executes(ctx -> unlink(ctx.getSource())))
                .then(Commands.literal("status").executes(ctx -> status(ctx.getSource())))
                .then(Commands.literal("admin")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("unlink")
                                .then(Commands.argument("player", StringArgumentType.word())
                                        .suggests(LINKED_PLAYER_SUGGESTIONS)
                                        .executes(DiscordLinkCommand::adminUnlink)))
                        .then(Commands.literal("info")
                                .then(Commands.argument("player", StringArgumentType.word())
                                        .suggests(LINKED_PLAYER_SUGGESTIONS)
                                        .executes(DiscordLinkCommand::adminInfo))))
        );
    }

    private static int link(CommandSourceStack source) throws CommandSyntaxException {
        checkAvailable();
        ServerPlayer player = source.getPlayerOrException();

        Optional<AccountLink> existing = AccountLinkService.getByUuid(player.getUUID());
        if (existing.isPresent()) {
            throw error(MinecraftLocaleProvider.Linking.Command.Link.Error.alreadyLinked(
                    LinkingDiscordUtils.describeUser(existing.get().discordId()),
                    Component.literal("/discord unlink").withStyle(style ->
                            style.withClickEvent(new ClickEvent.SuggestCommand("/discord unlink"))
                                    .withColor(ChatFormatting.AQUA)
                    )
            ));
        }

        LinkCodeManager.PendingCode code = AccountLinkService.getOrCreateCode(player.getUUID(), player.getGameProfile().name());
        String discordCommand = "/link code:" + code.code();
        String guildName = LinkingDiscordUtils.getGuildNames();
        long minutesLeft = Math.max(1, (code.secondsLeft(System.currentTimeMillis()) + 59) / 60);

        MutableComponent codeComponent = Component.literal(discordCommand).withStyle(style -> style
                .withColor(ChatFormatting.AQUA)
                .withBold(true)
                .withClickEvent(new ClickEvent.CopyToClipboard(discordCommand))
                .withHoverEvent(new HoverEvent.ShowText(MinecraftLocaleProvider.clickToCopy()))
        );

        MutableComponent message = Component.empty()
                .append(MinecraftLocaleProvider.Linking.Command.Link.instruction(guildName))
                .append("\n")
                .append(codeComponent)
                .append("\n")
                .append(MinecraftLocaleProvider.Linking.Code.expires(minutesLeft).withStyle(ChatFormatting.GRAY));

        if (!isDiscordConnected())
            message.append("\n").append(MinecraftLocaleProvider.Server.discordNotConnected().withStyle(ChatFormatting.RED));

        player.sendSystemMessage(message);
        return 1;
    }

    private static int unlink(CommandSourceStack source) throws CommandSyntaxException {
        checkAvailable();
        ServerPlayer player = source.getPlayerOrException();

        AccountLink removed = AccountLinkService.unlink(player.getUUID(), UnlinkReason.PLAYER)
                .orElseThrow(() -> error(MinecraftLocaleProvider.Linking.Command.notLinked()));

        player.sendSystemMessage(MinecraftLocaleProvider.Linking.Command.Unlink.success(
                LinkingDiscordUtils.describeUser(removed.discordId())).withStyle(ChatFormatting.YELLOW));

        return 1;
    }

    private static int status(CommandSourceStack source) throws CommandSyntaxException {
        checkAvailable();
        ServerPlayer player = source.getPlayerOrException();

        Optional<AccountLink> link = AccountLinkService.getByUuid(player.getUUID());
        if (link.isEmpty()) {
            player.sendSystemMessage(MinecraftLocaleProvider.Linking.Command.Status.notLinked(
                    Component.literal("/discord link").withStyle(style ->
                            style.withClickEvent(new ClickEvent.SuggestCommand("/discord link"))
                                    .withColor(ChatFormatting.AQUA)
                    )
            ).withStyle(ChatFormatting.YELLOW));
            return 0;
        }

        player.sendSystemMessage(MinecraftLocaleProvider.Linking.Command.Status.linked(
                LinkingDiscordUtils.describeUser(link.get().discordId())).withStyle(ChatFormatting.GREEN));

        return 1;
    }

    private static int adminUnlink(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        checkAvailable();
        String playerName = StringArgumentType.getString(ctx, "player");

        AccountLink link = findLink(ctx.getSource().getServer(), playerName)
                .orElseThrow(() -> error(MinecraftLocaleProvider.Linking.Command.Admin.Error.notLinked(NameComponentUtils.player(playerName))));
        AccountLinkService.unlink(link.uuid(), UnlinkReason.ADMIN);

        ctx.getSource().sendSuccess(() -> MinecraftLocaleProvider.Linking.Command.Admin.unlinked(
                NameComponentUtils.player(link.uuid(), link.name()), LinkingDiscordUtils.describeUser(link.discordId())), true);

        return 1;
    }

    private static int adminInfo(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        checkAvailable();
        String playerName = StringArgumentType.getString(ctx, "player");

        AccountLink link = findLink(ctx.getSource().getServer(), playerName)
                .orElseThrow(() -> error(MinecraftLocaleProvider.Linking.Command.Admin.Error.notLinked(NameComponentUtils.player(playerName))));

        Component user = LinkingDiscordUtils.describeUser(link.discordId())
                .append(Component.literal(" (" + link.discordId() + ")").withStyle(ChatFormatting.GRAY));
        Component info = MinecraftLocaleProvider.Linking.Command.Admin.info(
                NameComponentUtils.player(link.uuid(), link.name()),
                user,
                DATE_FORMAT.format(Instant.ofEpochMilli(link.linkedAt())) + " UTC"
        );
        ctx.getSource().sendSuccess(() -> info, false);

        return 1;
    }

    private static Optional<AccountLink> findLink(MinecraftServer server, String playerName) {
        ServerPlayer online = server.getPlayerList().getPlayerByName(playerName);
        if (online != null) {
            Optional<AccountLink> byUuid = AccountLinkService.getByUuid(online.getUUID());
            if (byUuid.isPresent())
                return byUuid;
        }
        return AccountLinkService.getByName(playerName);
    }

    private static void checkAvailable() throws CommandSyntaxException {
        if (!AccountLinking.isEnabled())
            throw error(MinecraftLocaleProvider.Linking.Command.disabled());
        if (!AccountLinkService.isLoaded())
            throw error(MinecraftLocaleProvider.Linking.Command.unavailable());
    }

    private static CommandSyntaxException error(Component message) {
        return new SimpleCommandExceptionType(message).create();
    }
}

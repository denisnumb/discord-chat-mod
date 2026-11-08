package com.denisnumb.discord_chat_mod.discord.slash_commands;

import com.denisnumb.discord_chat_mod.discord.model.DiscordGuildContext;
import com.denisnumb.discord_chat_mod.linking.AccountLinking;
import com.denisnumb.discord_chat_mod.linking.discord.AccountLinkSlashCommands;
import com.denisnumb.discord_chat_mod.locale.DiscordLocaleProvider;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.LOGGER;

public final class DiscordSlashCommands {
    private DiscordSlashCommands() {}

    public static final String LIST = "list";
    public static final String UPTIME = "uptime";
    public static final String TPS = "tps";
    public static final String ALLOWED_COMMANDS = "allowed_commands";
    public static final String CMD = "cmd";
    public static final Set<String> COMMAND_NAMES = Set.of(LIST, UPTIME, TPS, ALLOWED_COMMANDS, CMD);

    public static final String OPTION_COMMAND = "command";

    private static SlashCommandListener listener;
    private static AccountLinkSlashCommands accountLinkListener;

    public static void register(JDA jda, List<DiscordGuildContext> guildContexts) {
        listener = new SlashCommandListener();
        accountLinkListener = new AccountLinkSlashCommands();
        jda.addEventListener(listener, accountLinkListener);
        jda.updateCommands().queue();

        List<CommandData> commandList = List.of(
                Commands.slash(LIST, DiscordLocaleProvider.Discord.SlashCommands.List.description()),
                Commands.slash(UPTIME, DiscordLocaleProvider.Discord.SlashCommands.Uptime.description()),
                Commands.slash(TPS, DiscordLocaleProvider.Discord.SlashCommands.Tps.description()),
                Commands.slash(ALLOWED_COMMANDS, DiscordLocaleProvider.Discord.SlashCommands.AllowedCommands.description()),
                Commands.slash(CMD, DiscordLocaleProvider.Discord.SlashCommands.Cmd.description())
                        .addOption(OptionType.STRING, OPTION_COMMAND, DiscordLocaleProvider.Discord.SlashCommands.Cmd.Args.Command.description(), true, true)
        );

        for (DiscordGuildContext ctx : guildContexts) {
            List<CommandData> guildCommands = new ArrayList<>();
            if (ctx.enableSlashCommands)
                guildCommands.addAll(commandList);
            if (AccountLinking.isEnabled())
                guildCommands.addAll(AccountLinkSlashCommands.buildCommands());

            registerGuildCommands(ctx.guild, guildCommands);
        }
    }

    private static void registerGuildCommands(Guild guild, List<CommandData> commands) {
        if (commands.isEmpty()) {
            guild.updateCommands().queue();
            return;
        }

        guild.updateCommands()
                .addCommands(commands)
                .queue(
                        success -> LOGGER.info("Registered {} slash commands for guild: {}", success.size(), guild.getName()),
                        failure -> LOGGER.error("Failed to register slash commands for guild: {}", guild.getName(), failure)
                );
    }

    public static void unregister(JDA jda) {
        if (listener != null) {
            jda.removeEventListener(listener);
            listener = null;
        }

        if (accountLinkListener != null) {
            jda.removeEventListener(accountLinkListener);
            accountLinkListener = null;
        }
    }
}

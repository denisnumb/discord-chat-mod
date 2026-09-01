package com.denisnumb.discord_chat_mod.discord.slash_commands;

import com.denisnumb.discord_chat_mod.discord.model.DiscordGuildContext;
import com.denisnumb.discord_chat_mod.locale.ServerLocaleProvider;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.List;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.LOGGER;

public class DiscordSlashCommands {
    private static SlashCommandListener listener;

    public static void register(JDA jda, List<DiscordGuildContext> guildContexts) {
        listener = new SlashCommandListener();
        jda.addEventListener(listener);
        jda.updateCommands().queue();

        List<CommandData> commandList = List.of(
                Commands.slash("list", ServerLocaleProvider.Discord.SlashCommands.List.description()),
                Commands.slash("uptime", ServerLocaleProvider.Discord.SlashCommands.Uptime.description()),
                Commands.slash("tps", ServerLocaleProvider.Discord.SlashCommands.Tps.description()),
                Commands.slash("allowed_commands", ServerLocaleProvider.Discord.SlashCommands.AllowedCommands.description()),
                Commands.slash("cmd", ServerLocaleProvider.Discord.SlashCommands.Cmd.description())
                        .addOption(OptionType.STRING, "command", ServerLocaleProvider.Discord.SlashCommands.Cmd.Args.Command.description(), true, true)
        );

        for (DiscordGuildContext ctx : guildContexts) {
            if (!ctx.enableSlashCommands){
                ctx.guild.updateCommands().queue();
                continue;
            }

            ctx.guild.updateCommands()
                    .addCommands(commandList)
                    .queue(
                            success -> LOGGER.info("Registered {} slash commands for guild: {}", success.size(), ctx.guild.getName()),
                            failure -> LOGGER.error("Failed to register slash commands for guild: {}", ctx.guild.getName(), failure)
                    );
        }
    }

    public static void unregister(JDA jda) {
        if (listener != null) {
            jda.removeEventListener(listener);
            listener = null;
        }
    }
}

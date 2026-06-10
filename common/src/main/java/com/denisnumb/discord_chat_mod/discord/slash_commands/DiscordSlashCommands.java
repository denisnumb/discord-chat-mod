package com.denisnumb.discord_chat_mod.discord.slash_commands;

import com.denisnumb.discord_chat_mod.discord.model.DiscordGuildContext;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.List;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.LOGGER;
import static com.denisnumb.discord_chat_mod.LocaleProvider.getTranslate;
import static com.denisnumb.discord_chat_mod.ModLanguageKey.*;

public class DiscordSlashCommands {
    private static SlashCommandListener listener;

    public static void register(JDA jda, List<DiscordGuildContext> guildContexts) {
        listener = new SlashCommandListener();
        jda.addEventListener(listener);
        jda.updateCommands().queue();

        List<CommandData> commandList = List.of(
                Commands.slash("list", getTranslate(SLASH_COMMANDS_LIST_DESCRIPTION)),
                Commands.slash("uptime", getTranslate(SLASH_COMMANDS_UPTIME_DESCRIPTION)),
                Commands.slash("tps", getTranslate(SLASH_COMMANDS_TPS_DESCRIPTION)),
                Commands.slash("allowed_commands", getTranslate(SLASH_COMMANDS_ALLOWED_COMMANDS_DESCRIPTION)),
                Commands.slash("cmd", getTranslate(SLASH_COMMANDS_CMD_DESCRIPTION))
                        .addOption(OptionType.STRING, "command", getTranslate(SLASH_COMMANDS_CMD_ARG_COMMAND_DESCRIPTION), true, true)
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

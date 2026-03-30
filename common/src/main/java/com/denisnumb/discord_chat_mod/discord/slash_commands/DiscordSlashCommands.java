package com.denisnumb.discord_chat_mod.discord.slash_commands;

import com.denisnumb.discord_chat_mod.config.ConfigProvider;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.LOGGER;

public class DiscordSlashCommands {
    private static SlashCommandListener listener;

    public static void register(JDA jda) {
        if (!ConfigProvider.getConfig().isSlashCommandsEnabled()) {
            LOGGER.info("Discord slash commands are disabled in config");
            return;
        }

        listener = new SlashCommandListener();
        jda.addEventListener(listener);

        CommandListUpdateAction commands = jda.updateCommands();
        commands.addCommands(
                Commands.slash("list", "Show players currently online on the Minecraft server"),
                Commands.slash("uptime", "Show how long the Minecraft server has been running"),
                Commands.slash("tps", "Show the server's current TPS (ticks per second)"),
                Commands.slash("cmd", "Execute a command on the Minecraft server")
                        .addOption(OptionType.STRING, "command", "The server command to execute (without leading /)", true)
        );
        commands.queue(
                success -> LOGGER.info("Registered {} Discord slash commands", success.size()),
                failure -> LOGGER.error("Failed to register Discord slash commands", failure)
        );
    }

    public static void unregister(JDA jda) {
        if (listener != null) {
            jda.removeEventListener(listener);
            listener = null;
        }
    }
}

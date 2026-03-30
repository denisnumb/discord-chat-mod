package com.denisnumb.discord_chat_mod.discord.slash_commands;

import com.denisnumb.discord_chat_mod.config.ConfigProvider;
import com.denisnumb.discord_chat_mod.config.IConfigProvider;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.stream.Collectors;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.LOGGER;
import static com.denisnumb.discord_chat_mod.DiscordChatMod.server;

public class SlashCommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "list" -> handleList(event);
            case "uptime" -> handleUptime(event);
            case "tps" -> handleTps(event);
            case "cmd" -> handleCmd(event);
        }
    }

    private void handleList(SlashCommandInteractionEvent event) {
        MinecraftServer srv = server;
        if (srv == null) {
            event.reply("Server is not running.").setEphemeral(true).queue();
            return;
        }

        List<ServerPlayer> players = srv.getPlayerList().getPlayers();
        if (players.isEmpty()) {
            event.reply("\uD83D\uDCCB **No players online** (0/" + srv.getMaxPlayers() + ")").setEphemeral(true).queue();
            return;
        }

        String names = players.stream()
                .map(p -> p.getGameProfile().getName())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.joining("\n- ", "- ", ""));

        event.reply("\uD83D\uDCCB **Online Players** (" + players.size() + "/" + srv.getMaxPlayers() + "):\n" + names)
                .setEphemeral(true).queue();
    }

    private void handleUptime(SlashCommandInteractionEvent event) {
        MinecraftServer srv = server;
        if (srv == null) {
            event.reply("Server is not running.").setEphemeral(true).queue();
            return;
        }

        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
        String formatted = formatDuration(uptimeMs);

        event.reply("\u23F1\uFE0F **Server Uptime:** " + formatted).setEphemeral(true).queue();
    }

    private void handleTps(SlashCommandInteractionEvent event) {
        MinecraftServer srv = server;
        if (srv == null) {
            event.reply("Server is not running.").setEphemeral(true).queue();
            return;
        }

        double mspt = srv.getAverageTickTime();
        double tps = Math.min(20.0, 1000.0 / mspt);
        String tpsDisplay = String.format("%.1f", tps);
        String msptDisplay = String.format("%.1f", mspt);

        String indicator;
        if (tps >= 19.0) indicator = "\uD83D\uDFE2"; // green
        else if (tps >= 15.0) indicator = "\uD83D\uDFE1"; // yellow
        else indicator = "\uD83D\uDD34"; // red

        event.reply(indicator + " **TPS:** " + tpsDisplay + "/20.0 (" + msptDisplay + " ms/tick)")
                .setEphemeral(true).queue();
    }

    private void handleCmd(SlashCommandInteractionEvent event) {
        MinecraftServer srv = server;
        if (srv == null) {
            event.reply("Server is not running.").setEphemeral(true).queue();
            return;
        }

        if (!hasCommandPermission(event.getMember())) {
            event.reply("\u274C You don't have permission to use this command.").setEphemeral(true).queue();
            return;
        }

        String command = event.getOption("command").getAsString();

        // Defer reply since command execution might take a moment
        event.deferReply(true).queue();

        srv.execute(() -> {
            try {
                CommandOutputCapture capture = new CommandOutputCapture();
                srv.getCommands().performPrefixedCommand(CommandOutputCapture.createSourceStack(srv, capture), command);
                String output = capture.getOutput();

                String response = "\u2705 **Executed:** `" + command + "`";
                if (!output.isEmpty()) {
                    // Truncate if too long for Discord (2000 char limit)
                    if (output.length() > 1800) {
                        output = output.substring(0, 1800) + "\n... (truncated)";
                    }
                    response += "\n```\n" + output + "\n```";
                }

                String finalResponse = response;
                event.getHook().editOriginal(finalResponse).queue();
            } catch (Exception e) {
                LOGGER.error("Error executing Discord /cmd: {}", command, e);
                event.getHook().editOriginal("\u274C **Error:** " + e.getMessage()).queue();
            }
        });
    }

    private boolean hasCommandPermission(Member member) {
        if (member == null) return false;

        IConfigProvider config = ConfigProvider.getConfig();
        List<String> allowedRoles = config.slashCommandAllowedRoles();

        // If no roles configured, deny all /cmd usage
        if (allowedRoles.isEmpty()) return false;

        for (Role role : member.getRoles()) {
            if (allowedRoles.contains(role.getId()) || allowedRoles.contains(role.getName())) {
                return true;
            }
        }
        return false;
    }

    private static String formatDuration(long ms) {
        long seconds = ms / 1000;
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        sb.append(secs).append("s");
        return sb.toString();
    }
}

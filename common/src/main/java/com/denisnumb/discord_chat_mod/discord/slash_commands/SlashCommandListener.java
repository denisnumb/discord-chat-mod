package com.denisnumb.discord_chat_mod.discord.slash_commands;

import com.denisnumb.discord_chat_mod.discord.DiscordChannelRegistry;
import com.denisnumb.discord_chat_mod.discord.ServerStatusController;
import com.denisnumb.discord_chat_mod.discord.chat_style.DiscordChatStyleProvider;
import com.denisnumb.discord_chat_mod.discord.chat_style.MessageType;
import com.denisnumb.discord_chat_mod.discord.model.DiscordGuildContext;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.lang.management.ManagementFactory;
import java.util.Map;

import static com.denisnumb.discord_chat_mod.ColorUtils.Color.DISCORD_GREEN_COLOR;
import static com.denisnumb.discord_chat_mod.ColorUtils.Color.DISCORD_RED_COLOR;
import static com.denisnumb.discord_chat_mod.DiscordChatMod.LOGGER;
import static com.denisnumb.discord_chat_mod.DiscordChatMod.server;
import static com.denisnumb.discord_chat_mod.LocaleProvider.getTranslate;
import static com.denisnumb.discord_chat_mod.ModLanguageKey.*;
import static com.denisnumb.discord_chat_mod.discord.chat_style.DiscordChatStyleProvider.getDiscordMessageComponents;

public class SlashCommandListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        if (guild != null){
            DiscordGuildContext ctx = DiscordChannelRegistry.getContext(guild.getId());
            if (ctx == null || !ctx.enableSlashCommands){
                event.replyEmbeds(new EmbedBuilder()
                                .setColor(DISCORD_RED_COLOR)
                                .setDescription(getTranslate(SLASH_COMMANDS_ARE_DISABLED))
                                .build())
                        .setEphemeral(true)
                        .queue();
                return;
            }

            switch (event.getName()) {
                case "list" -> handleList(event);
                case "uptime" -> handleUptime(event);
                case "tps" -> handleTps(event);
                case "cmd" -> handleCmd(event);
            }
        }
    }
    private void replyWithDiscordMessageComponents(
            SlashCommandInteractionEvent event,
            DiscordChatStyleProvider.DiscordMessageComponents components
    ){
        if (components.hasContentAndEmbed())
            event.reply(components.getContent()).addEmbeds(components.getEmbed()).setEphemeral(true).queue();
        else if (components.hasNoEmbed())
            event.reply(components.getContent()).setEphemeral(true).queue();
        else
            event.replyEmbeds(components.getEmbed()).setEphemeral(true).queue();
    }

    private void replyServerIsUnavailable(SlashCommandInteractionEvent event){
        replyWithDiscordMessageComponents(event, getDiscordMessageComponents(MessageType.PINNED_STATUS_UNAVAILABLE, Map.of()).orElseThrow());
    }

    private void handleList(SlashCommandInteractionEvent event) {
        if (server == null)
            replyServerIsUnavailable(event);
        else
            replyWithDiscordMessageComponents(event, ServerStatusController.createServerStatusMessageComponents());
    }

    private void handleUptime(SlashCommandInteractionEvent event) {
        if (server == null) {
            replyServerIsUnavailable(event);
            return;
        }

        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
        String formatted = formatDuration(uptimeMs);

        event.reply(String.format(getTranslate(SLASH_COMMANDS_UPTIME_SUCCESS), formatted))
                .setEphemeral(true)
                .queue();
    }

    private void handleTps(SlashCommandInteractionEvent event) {
        MinecraftServer srv = server;
        if (srv == null) {
            replyServerIsUnavailable(event);
            return;
        }

        double mspt = srv.getCurrentSmoothedTickTime();
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
            replyServerIsUnavailable(event);
            return;
        }

        if (!hasCommandPermission(event.getMember())) {
            event.replyEmbeds(new EmbedBuilder()
                            .setColor(DISCORD_RED_COLOR)
                            .setDescription(getTranslate(SLASH_COMMANDS_CMD_MISSING_PERMISSION))
                            .build()
                    )
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String command = event.getOption("command").getAsString();
        event.deferReply(true).queue();

        srv.execute(() -> {
            try {
                CommandOutputCapture capture = new CommandOutputCapture();
                srv.getCommands().performPrefixedCommand(CommandOutputCapture.createSourceStack(srv, capture), command);
                String output = capture.getOutput();

                String response = String.format(getTranslate(SLASH_COMMANDS_CMD_SUCCESS), command);
                if (!output.isEmpty()) {
                    if (output.length() > 1800) {
                        output = output.substring(0, 1800) + "\n. . .";
                    }
                    response += "\n```\n" + output + "\n```";
                }

                event.getHook().editOriginalEmbeds(new EmbedBuilder()
                        .setColor(DISCORD_GREEN_COLOR)
                        .setDescription(response)
                        .build()
                ).queue();
            } catch (Exception e) {
                LOGGER.error("Error executing Discord /cmd: {}", command, e);
                event.getHook().editOriginalEmbeds(new EmbedBuilder()
                                .setColor(DISCORD_RED_COLOR)
                                .setDescription(String.format(getTranslate(SLASH_COMMANDS_CMD_EXECUTE_ERROR), e.getMessage()))
                                .build())
                        .queue();
            }
        });
    }

    private boolean hasCommandPermission(Member member) {
        if (member == null)
            return false;

        DiscordGuildContext ctx = DiscordChannelRegistry.getContext(member.getGuild().getId());
        if (ctx == null || ctx.slashCommandAllowedRoles.isEmpty())
            return false;

        return member.getRoles().stream()
                .anyMatch(r -> ctx.slashCommandAllowedRoles.contains(r.getId())
                        || ctx.slashCommandAllowedRoles.contains(r.getName()));
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

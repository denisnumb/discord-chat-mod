package com.denisnumb.discord_chat_mod.discord.slash_commands;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

/**
 * A CommandSource that captures command output text
 * so it can be relayed back to Discord.
 */
public class CommandOutputCapture implements CommandSource {
    private final List<String> outputLines = new ArrayList<>();

    @Override
    public void sendSystemMessage(Component component) {
        outputLines.add(component.getString());
    }

    @Override
    public boolean acceptsSuccess() {
        return true;
    }

    @Override
    public boolean acceptsFailure() {
        return true;
    }

    @Override
    public boolean shouldInformAdmins() {
        return false;
    }

    public String getOutput() {
        return String.join("\n", outputLines);
    }

    public static CommandSourceStack createSourceStack(MinecraftServer server, CommandOutputCapture capture) {
        return server.createCommandSourceStack().withSource(capture);
    }
}

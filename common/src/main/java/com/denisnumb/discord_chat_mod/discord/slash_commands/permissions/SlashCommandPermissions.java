package com.denisnumb.discord_chat_mod.discord.slash_commands.permissions;

import java.util.List;

public record SlashCommandPermissions(List<String> allow, List<String> deny) {
    public enum CanRunState {
        ALLOW, DENY, NOT_SET
    }

    public CanRunState canRun(String commandName) {
        if (deny.contains(commandName)) return CanRunState.DENY;
        if (allow.contains(commandName)) return CanRunState.ALLOW;
        if (deny.contains("*")) return CanRunState.DENY;
        if (allow.contains("*")) return CanRunState.ALLOW;
        return CanRunState.NOT_SET;
    }
}
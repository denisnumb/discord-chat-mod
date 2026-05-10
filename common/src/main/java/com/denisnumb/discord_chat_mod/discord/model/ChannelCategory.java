package com.denisnumb.discord_chat_mod.discord.model;

import org.jetbrains.annotations.Nullable;

import static com.denisnumb.discord_chat_mod.config.ConfigComments.*;

public enum ChannelCategory {
    PINNED_STATUS("pinnedStatusMessageChannelId"),
    DEATHS("deathsChannelId"),
    ADVANCEMENTS("advancementsChannelId"),
    SERVER_START_STOP("serverStartStopChannelId"),
    PLAYER_JOIN_LEAVE("playerJoinLeaveChannelId"),
    PLAYER_CHAT("playerChatMessagesChannelId"),
    SCREENSHOTS("screenshotsChannelId"),
    TELLRAW_COMMAND("tellrawChannelId"),
    SAY_COMMAND("sayChannelId"),
    ME_COMMAND("meChannelId"),
    COMMAND_LOG("commandLogChannelId");

    private final String configName;

    ChannelCategory(String configName) {
        this.configName = configName;
    }

    public String getConfigName() {
        return configName;
    }

    public String getConfigComment(){
        return switch (this) {
            case PINNED_STATUS -> PINNED_STATUS_MESSAGE_CHANNEL_ID_COMMENT;
            case DEATHS -> DEATHS_CHANNEL_ID_COMMENT;
            case ADVANCEMENTS -> ADVANCEMENTS_CHANNEL_ID_COMMENT;
            case SERVER_START_STOP -> SERVER_START_STOP_CHANNEL_ID_COMMENT;
            case PLAYER_JOIN_LEAVE -> PLAYER_JOIN_LEAVE_CHANNEL_ID_COMMENT;
            case PLAYER_CHAT -> PLAYER_CHAT_MESSAGES_CHANNEL_ID_COMMENT;
            case SCREENSHOTS -> SCREENSHOTS_CHANNEL_ID_COMMENT;
            case TELLRAW_COMMAND -> TELLRAW_CHANNEL_ID_COMMENT;
            case SAY_COMMAND -> SAY_CHANNEL_ID_COMMENT;
            case ME_COMMAND -> ME_CHANNEL_ID_COMMENT;
            case COMMAND_LOG -> COMMAND_LOG_CHANNEL_ID_COMMENT;
        };
    }

    @Nullable
    public static ChannelCategory fromConfigName(String name) {
        for (ChannelCategory cat : values()) {
            if (cat.configName.equalsIgnoreCase(name)) {
                return cat;
            }
        }
        return null;
    }
}

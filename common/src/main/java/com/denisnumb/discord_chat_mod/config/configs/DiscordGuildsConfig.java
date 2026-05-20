package com.denisnumb.discord_chat_mod.config.configs;

import com.denisnumb.discord_chat_mod.discord.model.ChannelCategory;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.denisnumb.discord_chat_mod.config.ConfigComments.*;
import static com.denisnumb.discord_chat_mod.config.ConfigDefaults.*;

public class DiscordGuildsConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static List<DiscordGuildConfig> discordGuildConfigs;

    public record DiscordGuildConfig(
            String guildId,
            String defaultChannelId,
            String serverLogsChannelId,
            boolean duplicateMessages,
            boolean enablePinnedStatusMessage,
            boolean enableSlashCommands,
            List<String> slashCommandAllowedRoles,
            Map<String, String> channelOverrides
    ) { }

    public static List<CommentedConfig> loadDiscordGuildsConfig(CommentedConfig commonConfig){
        List<CommentedConfig> guildList = commonConfig.getOrElse("guilds", getDefaultGuildConfig(commonConfig));
        discordGuildConfigs = new ArrayList<>();

        for (CommentedConfig guildConfig : guildList) {
            String guildId = guildConfig.getOrElse("guildId", GUILD_ID_DEFAULT);
            if (discordGuildConfigs.stream().map(DiscordGuildConfig::guildId).toList().contains(guildId))
                continue;

            migrateGuildConfig(guildConfig, guildId);

            String mainChannelId = guildConfig.get("defaultChannelId");
            String serverLogsChannelId = guildConfig.get("serverLogsChannelId");
            boolean enablePinnedStatusMessage = guildConfig.get("enablePinnedStatusMessage");

            CommentedConfig slashCommands = guildConfig.get("slashCommands");
            boolean enableSlashCommands = slashCommands.get("enableSlashCommands");
            List<String> slashCommandAllowedRoles = slashCommands.get("slashCommandAllowedRoles");

            CommentedConfig overrides = guildConfig.get("channelOverrides");
            boolean duplicateMessages = overrides.get("duplicateMessages");
            Map<String, String> channelOverrides = new HashMap<>();

            for (CommentedConfig.Entry entry : overrides.entrySet()) {
                if (entry.getKey().equals("duplicateMessages"))
                    continue;

                String key = entry.getKey();
                String value = entry.getValue();

                boolean valid = false;
                for (ChannelCategory cat : ChannelCategory.values()) {
                    if (cat.getConfigName().equalsIgnoreCase(key)) {
                        valid = true;
                        channelOverrides.put(cat.getConfigName(), value);
                        break;
                    }
                }

                if (!valid)
                    LOGGER.error("Unknown channel key \"{}\" in channel overrides for guildId: {}", key, guildId);
            }

            discordGuildConfigs.add(
                    new DiscordGuildConfig(
                            guildId,
                            mainChannelId,
                            serverLogsChannelId,
                            duplicateMessages,
                            enablePinnedStatusMessage,
                            enableSlashCommands,
                            slashCommandAllowedRoles,
                            channelOverrides
                    )
            );
        }

        return guildList;
    }

    private static void migrateGuildConfig(CommentedConfig guildConfig, String guildId) {
        migrateField(guildConfig, guildId, "defaultChannelId", DEFAULT_CHANNEL_ID_DEFAULT, DEFAULT_CHANNEL_ID_COMMENT);
        migrateField(guildConfig, guildId, "serverLogsChannelId", SERVER_LOGS_CHANNEL_ID_DEFAULT, SERVER_LOGS_CHANNEL_ID_COMMENT);
        migrateField(guildConfig, guildId, "enablePinnedStatusMessage", ENABLE_PINNED_STATUS_MESSAGE_DEFAULT, ENABLE_PINNED_STATUS_MESSAGE_COMMENT);

        if (!guildConfig.contains("slashCommands")) {
            LOGGER.info("[guildId: {}] Adding missing section \"slashCommands\"", guildId);
            guildConfig.set("slashCommands", guildConfig.createSubConfig());
        }

        CommentedConfig slashCommands = guildConfig.get("slashCommands");
        migrateField(slashCommands, guildId, "enableSlashCommands", ENABLE_SLASH_COMMANDS_DEFAULT, ENABLE_SLASH_COMMANDS_COMMENT);
        migrateField(slashCommands, guildId, "slashCommandAllowedRoles", SLASH_COMMAND_ALLOWED_ROLES_DEFAULT, SLASH_COMMAND_ALLOWED_ROLES_COMMENT);

        if (!guildConfig.contains("channelOverrides")) {
            LOGGER.info("[guildId: {}] Adding missing section \"channelOverrides\"", guildId);
            guildConfig.set("channelOverrides", guildConfig.createSubConfig());
        }

        CommentedConfig overrides = guildConfig.get("channelOverrides");
        migrateField(overrides, guildId, "duplicateMessages", DUPLICATE_MESSAGES_DEFAULT, DUPLICATE_MESSAGES_COMMENT);
        for (ChannelCategory category : ChannelCategory.values())
            migrateField(overrides, guildId, category.getConfigName(), OVERRIDE_CHANNEL_ID_DEFAULT, category.getConfigComment());
    }

    private static <T> void migrateField(CommentedConfig config, String guildId, String key, T defaultValue, String comment) {
        if (!config.contains(key)) {
            LOGGER.info("[guildId: {}] Adding missing field \"{}\"", guildId, key);
            config.set(key, defaultValue);
            config.setComment(key, comment);
        }
    }

    private static List<CommentedConfig> getDefaultGuildConfig(CommentedConfig commonConfig){
        CommentedConfig exampleServer = commonConfig.createSubConfig();
        exampleServer.set("guildId", GUILD_ID_DEFAULT);
        exampleServer.setComment("guildId", GUILD_ID_COMMENT);
        exampleServer.set("defaultChannelId", DEFAULT_CHANNEL_ID_DEFAULT);
        exampleServer.setComment("defaultChannelId", DEFAULT_CHANNEL_ID_COMMENT);
        exampleServer.set("serverLogsChannelId", SERVER_LOGS_CHANNEL_ID_DEFAULT);
        exampleServer.setComment("serverLogsChannelId", SERVER_LOGS_CHANNEL_ID_COMMENT);
        exampleServer.set("enablePinnedStatusMessage", ENABLE_PINNED_STATUS_MESSAGE_DEFAULT);
        exampleServer.setComment("enablePinnedStatusMessage", ENABLE_PINNED_STATUS_MESSAGE_COMMENT);

        CommentedConfig slashCommands = exampleServer.createSubConfig();
        slashCommands.set("enableSlashCommands", ENABLE_SLASH_COMMANDS_DEFAULT);
        slashCommands.setComment("enableSlashCommands", ENABLE_SLASH_COMMANDS_COMMENT);
        slashCommands.set("slashCommandAllowedRoles", SLASH_COMMAND_ALLOWED_ROLES_DEFAULT);
        slashCommands.setComment("slashCommandAllowedRoles", SLASH_COMMAND_ALLOWED_ROLES_COMMENT);

        CommentedConfig overrides = exampleServer.createSubConfig();
        overrides.set("duplicateMessages", DUPLICATE_MESSAGES_DEFAULT);
        overrides.setComment("duplicateMessages", DUPLICATE_MESSAGES_COMMENT);

        for (ChannelCategory category : ChannelCategory.values()){
            overrides.set(category.getConfigName(), OVERRIDE_CHANNEL_ID_DEFAULT);
            overrides.setComment(category.getConfigName(), category.getConfigComment());
        }

        exampleServer.set("slashCommands", slashCommands);
        exampleServer.set("channelOverrides", overrides);
        List<CommentedConfig> guildList = List.of(exampleServer);

        commonConfig.add("guilds", guildList);

        return guildList;
    }
}

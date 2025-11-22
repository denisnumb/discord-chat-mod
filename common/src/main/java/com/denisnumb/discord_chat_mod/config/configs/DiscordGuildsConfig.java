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
            boolean duplicateMessages,
            boolean enablePinnedStatusMessage,
            Map<String, String> channelOverrides
    ) { }

    public static List<CommentedConfig> loadDiscordGuildsConfig(CommentedConfig commonConfig){
        List<CommentedConfig> guildList = commonConfig.getOrElse("guilds", setDefaultGuildConfig(commonConfig));
        discordGuildConfigs = new ArrayList<>();

        for (CommentedConfig guild : guildList) {
            String guildId = guild.getOrElse("guildId", GUILD_ID_DEFAULT);
            if (discordGuildConfigs.stream().map(DiscordGuildConfig::guildId).toList().contains(guildId))
                continue;

            String mainChannelId = guild.getOrElse("defaultChannelId", DEFAULT_CHANNEL_ID_DEFAULT);
            boolean enablePinnedStatusMessage = guild.getOrElse("enablePinnedStatusMessage", ENABLE_PINNED_STATUS_MESSAGE_DEFAULT);

            CommentedConfig overrides = guild.get("channelOverrides");
            boolean duplicateMessages = overrides.getOrElse("duplicateMessages", DUPLICATE_MESSAGES_DEFAULT);

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

            discordGuildConfigs.add(new DiscordGuildConfig(guildId, mainChannelId, duplicateMessages, enablePinnedStatusMessage, channelOverrides));
        }

        return guildList;
    }

    private static List<CommentedConfig> setDefaultGuildConfig(CommentedConfig commonConfig){
        CommentedConfig exampleServer = commonConfig.createSubConfig();
        exampleServer.set("guildId", GUILD_ID_DEFAULT);
        exampleServer.setComment("guildId", GUILD_ID_COMMENT);
        exampleServer.set("defaultChannelId", DEFAULT_CHANNEL_ID_DEFAULT);
        exampleServer.setComment("defaultChannelId", DEFAULT_CHANNEL_ID_COMMENT);
        exampleServer.set("enablePinnedStatusMessage", ENABLE_PINNED_STATUS_MESSAGE_DEFAULT);
        exampleServer.setComment("enablePinnedStatusMessage", ENABLE_PINNED_STATUS_MESSAGE_COMMENT);

        CommentedConfig overrides = exampleServer.createSubConfig();
        overrides.set("duplicateMessages", DUPLICATE_MESSAGES_DEFAULT);
        overrides.setComment("duplicateMessages", DUPLICATE_MESSAGES_COMMENT);

        for (ChannelCategory category : ChannelCategory.values()){
            overrides.set(category.getConfigName(), OVERRIDE_CHANNEL_ID_DEFAULT);
            overrides.setComment(category.getConfigName(), category.getConfigComment());
        }

        exampleServer.set("channelOverrides", overrides);
        List<CommentedConfig> guildList = List.of(exampleServer);

        commonConfig.add("guilds", guildList);

        return guildList;
    }
}

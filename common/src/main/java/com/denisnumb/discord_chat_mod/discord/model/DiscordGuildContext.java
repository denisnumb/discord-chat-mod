package com.denisnumb.discord_chat_mod.discord.model;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.denisnumb.discord_chat_mod.discord.DiscordChannelRegistry.isChannelCategoryDisabled;

public class DiscordGuildContext {
    public final Guild guild;
    public GuildMessageChannel defaultChannel;
    public GuildMessageChannel serverLogsChannel;
    public final boolean enablePinnedStatusMessage;
    public boolean enableSlashCommands;
    public List<String> slashCommandAllowedRoles;
    public final boolean duplicateMessages;

    private final Map<ChannelCategory, @Nullable GuildMessageChannel> channels = new EnumMap<>(ChannelCategory.class);
    private final Map<GuildMessageChannel, Webhook> webhooks = new HashMap<>();
    public final Map<String, @Nullable GuildMessageChannel> CHANNEL_CACHE = new HashMap<>();

    public DiscordGuildContext(
            Guild guild,
            boolean duplicateMessages,
            boolean enablePinnedStatusMessage,
            boolean enableSlashCommands,
            List<String> slashCommandAllowedRoles
    ) {
        this.guild = guild;
        this.duplicateMessages = duplicateMessages;
        this.enablePinnedStatusMessage = enablePinnedStatusMessage;
        this.enableSlashCommands = enableSlashCommands;
        this.slashCommandAllowedRoles = slashCommandAllowedRoles;
    }

    public void setChannel(ChannelCategory category, @Nullable GuildMessageChannel channel) {
        channels.put(category, channel);
    }

    @Nullable
    public GuildMessageChannel getChannel(ChannelCategory category) {
        return channels.getOrDefault(category, defaultChannel);
    }

    public GuildMessageChannel getDefaultChannelIfCategoryDisabled(ChannelCategory category){
        GuildMessageChannel categoryChannel = getChannel(category);

        if (isChannelCategoryDisabled(categoryChannel))
            return defaultChannel;

        return categoryChannel;
    }

    public Guild getGuild(){
        return guild;
    }

    public GuildMessageChannel getDefaultChannel() {
        return defaultChannel;
    }

    public void setDefaultChannel(@Nullable GuildMessageChannel defaultChannel) throws IllegalStateException {
        if (defaultChannel == null)
            throw new IllegalStateException();

        this.defaultChannel = defaultChannel;
    }

    public void setServerLogsChannel(@Nullable GuildMessageChannel serverLogsChannel) {
        this.serverLogsChannel = serverLogsChannel;
    }

    @Nullable
    public GuildMessageChannel getServerLogsChannel() {
        return serverLogsChannel;
    }

    public Optional<Webhook> getWebhook(GuildMessageChannel channel) {
        return Optional.ofNullable(webhooks.get(channel));
    }

    public void registerWebhook(GuildMessageChannel channel, Webhook webhook) {
        webhooks.put(channel, webhook);
    }
}


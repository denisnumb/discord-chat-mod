package com.denisnumb.discord_chat_mod.api_impl;

import com.denisnumb.discord_chat_mod.api.ChannelFallback;
import com.denisnumb.discord_chat_mod.api.ChannelKey;
import com.denisnumb.discord_chat_mod.discord.DiscordGuildRegistry;
import com.denisnumb.discord_chat_mod.discord.chat.DiscordMessageSender;
import com.denisnumb.discord_chat_mod.discord.model.ChannelCategory;
import com.denisnumb.discord_chat_mod.discord.model.DiscordGuildContext;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


final class ApiRoute {
    private static final String DISABLED_CHANNEL_ID = "-1";

    private enum Kind { DEFAULT, SERVER_LOGS, CATEGORY, MOD }

    record Target(@Nullable GuildMessageChannel channel, @Nullable String unresolvedChannelId) {}

    private final Kind kind;
    private final ChannelKey key;
    private final @Nullable ChannelCategory category;

    private ApiRoute(Kind kind, ChannelKey key, @Nullable ChannelCategory category) {
        this.kind = kind;
        this.key = key;
        this.category = category;
    }

    static @Nullable ApiRoute of(ChannelKey key) {
        if (!key.isBuiltin())
            return ApiRegistry.getChannel(key) == null ? null : new ApiRoute(Kind.MOD, key, null);

        return switch (key.name()) {
            case "default" -> new ApiRoute(Kind.DEFAULT, key, null);
            case "server_logs" -> new ApiRoute(Kind.SERVER_LOGS, key, null);
            case "chat" -> category(key, ChannelCategory.PLAYER_CHAT);
            case "join_leave" -> category(key, ChannelCategory.PLAYER_JOIN_LEAVE);
            case "deaths" -> category(key, ChannelCategory.DEATHS);
            case "advancements" -> category(key, ChannelCategory.ADVANCEMENTS);
            case "server_start_stop" -> category(key, ChannelCategory.SERVER_START_STOP);
            case "command_log" -> category(key, ChannelCategory.COMMAND_LOG);
            default -> null;
        };
    }

    private static ApiRoute category(ChannelKey key, ChannelCategory category) {
        return new ApiRoute(Kind.CATEGORY, key, category);
    }

    List<Target> resolve(DiscordGuildContext context) {
        return switch (kind) {
            case DEFAULT -> List.of(new Target(context.getDefaultChannel(), null));
            case SERVER_LOGS -> context.getServerLogsChannel() == null
                    ? List.of()
                    : List.of(new Target(context.getServerLogsChannel(), null));
            case CATEGORY -> DiscordMessageSender.getCategoryTargets(context, category)
                    .map(targets -> toTargets(targets.all()))
                    .orElse(List.of());
            case MOD -> resolveModChannel(context);
        };
    }

    boolean isEnabled(DiscordGuildContext context) {
        return switch (kind) {
            case DEFAULT -> true;
            case SERVER_LOGS -> context.getServerLogsChannel() != null;
            case CATEGORY -> !DiscordGuildRegistry.isChannelCategoryDisabled(context.getChannel(category));
            case MOD -> {
                String channelId = getModChannelId(context);
                if (channelId == null)
                    yield getFallback() == ChannelFallback.DEFAULT_CHANNEL;
                yield !channelId.equals(DISABLED_CHANNEL_ID) && !DiscordGuildRegistry.isChannelResolutionFailed(context, channelId);
            }
        };
    }

    private static List<Target> toTargets(List<GuildMessageChannel> channels) {
        List<Target> targets = new ArrayList<>(channels.size());
        for (GuildMessageChannel channel : channels)
            targets.add(new Target(channel, null));

        return targets;
    }

    private List<Target> resolveModChannel(DiscordGuildContext context) {
        String channelId = getModChannelId(context);
        if (channelId == null) {
            return getFallback() == ChannelFallback.DEFAULT_CHANNEL
                    ? List.of(new Target(context.getDefaultChannel(), null))
                    : List.of();
        }

        if (channelId.equals(DISABLED_CHANNEL_ID))
            return List.of();

        GuildMessageChannel channel = DiscordGuildRegistry.resolveChannel(context, channelId);
        return List.of(channel != null ? new Target(channel, null) : new Target(null, channelId));
    }

    private @Nullable String getModChannelId(DiscordGuildContext context) {
        String channelId = context.getModChannelIds().get(key.toString());
        return channelId == null || channelId.isBlank() ? null : channelId.trim();
    }

    private ChannelFallback getFallback() {
        ApiRegistry.ChannelDefinition definition = ApiRegistry.getChannel(key);
        return definition == null ? ChannelFallback.DISABLED : definition.fallback();
    }
}

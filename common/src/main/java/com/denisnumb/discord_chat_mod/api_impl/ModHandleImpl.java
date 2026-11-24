package com.denisnumb.discord_chat_mod.api_impl;

import com.denisnumb.discord_chat_mod.api.ChannelFallback;
import com.denisnumb.discord_chat_mod.api.ChannelKey;
import com.denisnumb.discord_chat_mod.api.ConnectionState;
import com.denisnumb.discord_chat_mod.api.DiscordMessage;
import com.denisnumb.discord_chat_mod.api.LinkedAccounts;
import com.denisnumb.discord_chat_mod.api.ModHandle;
import com.denisnumb.discord_chat_mod.api.Registration;
import com.denisnumb.discord_chat_mod.api.SendResult;
import com.denisnumb.discord_chat_mod.config.ConfigManager;
import com.denisnumb.discord_chat_mod.config.configs.DiscordGuildsConfig;
import com.denisnumb.discord_chat_mod.discord.DiscordGuildRegistry;
import com.denisnumb.discord_chat_mod.discord.model.DiscordGuildContext;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

final class ModHandleImpl implements ModHandle {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final String modId;
    private final LinkedAccounts accounts;

    ModHandleImpl(String modId) {
        this.modId = modId;
        this.accounts = new ApiLinkedAccounts(modId);
    }

    @Override
    public String modId() {
        return modId;
    }

    @Override
    public boolean isConnected() {
        return DiscordChatApiImpl.isConnected();
    }

    @Override
    public Registration addConnectionListener(Consumer<ConnectionState> listener) {
        return DiscordChatApiImpl.addConnectionListener(modId, listener);
    }

    @Override
    public ChannelKey registerChannel(String name, ChannelFallback fallback, String description) {
        ChannelKey key = ChannelKey.of(modId, name);
        Objects.requireNonNull(fallback, "fallback");
        Objects.requireNonNull(description, "description");

        ApiRegistry.registerChannel(new ApiRegistry.ChannelDefinition(key, fallback, description));

        editConfig(key.toString(), config -> {
            if (!(config.get("guilds") instanceof List<?> guilds))
                return;

            for (Object guild : guilds) {
                if (guild instanceof CommentedConfig guildConfig) {
                    Object guildId = guildConfig.get("guildId");
                    DiscordGuildsConfig.ensureModChannels(guildConfig, String.valueOf(guildId));
                }
            }
        });

        return key;
    }

    @Override
    public boolean isChannelEnabled(ChannelKey channel) {
        Objects.requireNonNull(channel, "channel");

        ApiRoute route = ApiRoute.of(channel);
        if (route == null || !isConnected())
            return false;

        for (DiscordGuildContext context : DiscordGuildRegistry.getAllContexts()) {
            if (route.isEnabled(context))
                return true;
        }
        return false;
    }

    private void editConfig(String key, Consumer<CommentedConfig> editor) {
        try {
            ConfigManager.editCommon(editor);
        } catch (RuntimeException e) {
            LOGGER.error("[api] Failed to add \"{}\" of mod \"{}\" to the config", key, modId, e);
        }
    }
    
    @Override
    public CompletableFuture<SendResult> send(ChannelKey channel, DiscordMessage message) {
        Objects.requireNonNull(channel, "channel");
        Objects.requireNonNull(message, "message");
        return ApiMessageSender.send(modId, channel, message);
    }

    @Override
    public LinkedAccounts accounts() {
        return accounts;
    }
}

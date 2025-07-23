package com.denisnumb.discord_chat_mod.neoforge.config;

import com.denisnumb.discord_chat_mod.DiscordChatMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import static com.denisnumb.discord_chat_mod.config.ConfigComments.*;

@EventBusSubscriber(modid = DiscordChatMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<String> DISCORD_BOT_TOKEN = COMMON_BUILDER
            .comment(DISCORD_BOT_TOKEN_COMMENT)
            .define("discordBotToken", "");

    public static final ModConfigSpec.ConfigValue<String> DISCORD_CHANNEL_ID = COMMON_BUILDER
            .comment(DISCORD_CHANNEL_ID_COMMENT)
            .define("discordChannelId", "");

    public static final ModConfigSpec.BooleanValue LOG_DISCORD_MESSAGES = COMMON_BUILDER
            .comment(LOG_DISCORD_MESSAGES_COMMENT)
            .define("logDiscordMessages", true);

    public static final ModConfigSpec.BooleanValue ENABLE_PINNED_STATUS_MESSAGE = COMMON_BUILDER
            .comment(ENABLE_PINNED_STATUS_MESSAGE_COMMENT)
            .define("enablePinnedStatusMessage", true);

    public static final ModConfigSpec.BooleanValue LOG_DISCORD_ERRORS_TO_SERVER_CHAT = COMMON_BUILDER
            .comment(LOG_DISCORD_ERRORS_TO_SERVER_CHAT_COMMENT)
            .define("logDiscordErrorsToServerChat", true);

    public static final ModConfigSpec.ConfigValue<String> DISCORD_ERRORS_CHAT_PLAYER_SELECTOR = COMMON_BUILDER
            .comment(DISCORD_ERRORS_CHAT_PLAYER_SELECTOR_COMMENT)
            .define("discordErrorsChatPlayerSelector", "@a");

    public static final ModConfigSpec.ConfigValue<String> MOD_LOCALE = COMMON_BUILDER
            .comment(MOD_LOCALE_COMMENT)
            .define("modLocale", "en_us");

    public static final ModConfigSpec.BooleanValue ENABLE_WEBHOOK_MODE = COMMON_BUILDER
            .push("webhookModeConfig")
            .comment(ENABLE_WEBHOOK_MODE_COMMENT)
            .define("enableWebhookMode", false);

    public static final ModConfigSpec.ConfigValue<String> WEBHOOK_SERVER_NAME = COMMON_BUILDER
            .comment(WEBHOOK_SERVER_NAME_COMMENT)
            .define("webhookServerName", "Minecraft Server");

    public static final ModConfigSpec.ConfigValue<String> WEBHOOK_SERVER_AVATAR_URL = COMMON_BUILDER
            .comment(WEBHOOK_SERVER_AVATAR_URL_COMMENT)
            .define("webhookServerAvatarUrl", "");

    public static final ModConfigSpec.BooleanValue ENABLE_SET_AVATAR_URL_COMMAND = COMMON_BUILDER
            .comment(ENABLE_SET_AVATAR_URL_COMMAND_COMMENT)
            .define("enableSetAvatarUrlCommand", true);

    public static final ModConfigSpec.ConfigValue<String> WEBHOOK_PLAYER_AVATAR_URL = COMMON_BUILDER
            .comment(WEBHOOK_PLAYER_AVATAR_URL_COMMENT)
            .define("webhookPlayerAvatarUrl", "https://mc-heads.net/avatar/<name>.png");

    public static final ModConfigSpec.ConfigValue<String> WEBHOOK_PLAYER_DEFAULT_AVATAR_URL = COMMON_BUILDER
            .comment(WEBHOOK_PLAYER_DEFAULT_AVATAR_URL_COMMENT)
            .define("webhookPlayerDefaultAvatarUrl", "https://mc-heads.net/avatar.png");
    
    public static final ModConfigSpec.ConfigValue<String> PROXY_HOSTNAME = COMMON_BUILDER
            .pop()
            .push("discordProxyConfig")
            .comment(PROXY_HOSTNAME_COMMENT)
            .define("proxyHostname", "");

    public static final ModConfigSpec.IntValue PROXY_PORT = COMMON_BUILDER
            .defineInRange("proxyPort", 1234, 0, 65535);

    public static final ModConfigSpec.ConfigValue<String> PROXY_USER = COMMON_BUILDER
            .comment(PROXY_USER_COMMENT)
            .define("proxyUser", "");

    public static final ModConfigSpec.ConfigValue<String> PROXY_PASSWORD = COMMON_BUILDER
            .define("proxyPassword", "");

    public static final ModConfigSpec.BooleanValue DUPLICATE_MESSAGES = COMMON_BUILDER
            .pop()
            .push("channelOverrides")
            .comment(DUPLICATE_MESSAGES_COMMENT)
            .define("duplicateMessages", false);

    public static final ModConfigSpec.ConfigValue<String> PINNED_STATUS_MESSAGE_CHANNEL_ID = COMMON_BUILDER
            .comment(PINNED_STATUS_MESSAGE_CHANNEL_ID_COMMENT)
            .define("pinnedStatusMessageChannelId", "");

    public static final ModConfigSpec.ConfigValue<String> DEATHS_CHANNEL_ID = COMMON_BUILDER
            .comment(DEATHS_CHANNEL_ID_COMMENT)
            .define("deathsChannelId", "");

    public static final ModConfigSpec.ConfigValue<String> ADVANCEMENTS_CHANNEL_ID = COMMON_BUILDER
            .comment(ADVANCEMENTS_CHANNEL_ID_COMMENT)
            .define("advancementsChannelId", "");

    public static final ModConfigSpec.ConfigValue<String> SERVER_START_STOP_CHANNEL_ID = COMMON_BUILDER
            .comment(SERVER_START_STOP_CHANNEL_ID_COMMENT)
            .define("serverStartStopChannelId", "");

    public static final ModConfigSpec.ConfigValue<String> PLAYER_JOIN_LEAVE_CHANNEL_ID = COMMON_BUILDER
            .comment(PLAYER_JOIN_LEAVE_CHANNEL_ID_COMMENT)
            .define("playerJoinLeaveChannelId", "");

    public static final ModConfigSpec.ConfigValue<String> PLAYER_CHAT_MESSAGES_CHANNEL_ID = COMMON_BUILDER
            .comment(PLAYER_CHAT_MESSAGES_CHANNEL_ID_COMMENT)
            .define("playerChatMessagesChannelId", "");

    public static final ModConfigSpec.ConfigValue<String> SCREENSHOTS_CHANNEL_ID = COMMON_BUILDER
            .comment(SCREENSHOTS_CHANNEL_ID_COMMENT)
            .define("screenshotsChannelId", "");

    public static final ModConfigSpec.ConfigValue<String> TELLRAW_CHANNEL_ID = COMMON_BUILDER
            .comment(TELLRAW_CHANNEL_ID_COMMENT)
            .define("tellrawChannelId", "");

    public static final ModConfigSpec.ConfigValue<String> SAY_CHANNEL_ID = COMMON_BUILDER
            .comment(SAY_CHANNEL_ID_COMMENT)
            .define("sayChannelId", "");

    public static final ModConfigSpec.BooleanValue EMOJIFUL_COMPATIBILITY = CLIENT_BUILDER
            .comment(EMOJIFUL_COMPATIBILITY_COMMENT)
            .define("emojifulCompatibility", false);

    public static final ModConfigSpec.IntValue MAX_CHAT_HISTORY = CLIENT_BUILDER
            .comment(MAX_CHAT_HISTORY_COMMENT)
            .defineInRange("maxChatHistory", 500, 20, Integer.MAX_VALUE);

    public static final ModConfigSpec COMMON_SPEC = COMMON_BUILDER.build();
    public static final ModConfigSpec CLIENT_SPEC = CLIENT_BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {}
}

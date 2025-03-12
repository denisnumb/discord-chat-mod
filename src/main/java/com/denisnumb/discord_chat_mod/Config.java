package com.denisnumb.discord_chat_mod;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = DiscordChatMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.ConfigValue<String> DISCORD_BOT_TOKEN = BUILDER
            .comment(" Bot access token" +
                    "\n [!] Make sure all Privileged Gateway Intents are enabled on https://discord.com/developers/applications/<your_app_id>/bot" +
                    "\n [!] Make sure the bot has the following rights on the server:" +
                    "\n [!] - VIEW_CHANNEL" +
                    "\n [!] - MESSAGE_SEND" +
                    "\n [!] - MESSAGE_SEND_IN_THREADS" +
                    "\n [!] - MESSAGE_EMBED_LINKS" +
                    "\n [!] - MESSAGE_ATTACH_FILES" +
                    "\n [!] - MESSAGE_MANAGE" +
                    "\n [!] - MESSAGE_HISTORY")
            .define("discordBotToken", "");

    private static final ModConfigSpec.ConfigValue<String> DISCORD_CHANNEL_ID = BUILDER
            .comment(" Discord channel ID for messaging with MineCraft\n [!] Make sure the bot has access to the channel and all the rights listed above.")
            .define("discordChannelId", "");

    private static final ModConfigSpec.BooleanValue LOG_DISCORD_MESSAGES = BUILDER
            .comment(" Do logging to the server console messages from discord")
            .define("logDiscordMessages", true);

    private static final ModConfigSpec.BooleanValue ENABLE_PINNED_STATUS_MESSAGE = BUILDER
            .comment(" Create a pinned message with the current server status and player list")
            .define("enablePinnedStatusMessage", true);

    private static final ModConfigSpec.BooleanValue LOG_DISCORD_ERRORS_TO_SERVER_CHAT = BUILDER
            .comment(" Notify about internal Discord interaction errors in the server's in-game chat")
            .define("logDiscordErrorsToServerChat", true);

    private static final ModConfigSpec.ConfigValue<String> DISCORD_ERRORS_CHAT_PLAYER_SELECTOR = BUILDER
            .comment(" If logDiscordErrorsToServerChat=true, then the errors in the chat will be seen by players with the specified selector" +
                    "\n By default, \"@a\" — all players. You can specify a specific nickname or attribute, for example, \"@a[tag=admin]\"")
            .define("discordErrorsChatPlayerSelector", "@a");

    private static final ModConfigSpec.ConfigValue<String> MOD_LOCALE = BUILDER
            .comment(" Mod locale")
            .define("modLocale", "en_us");

    static final ModConfigSpec SPEC = BUILDER.build();
    public static String discordBotToken;
    public static String discordChannelId;
    public static boolean logDiscordMessages;
    public static boolean enablePinnedStatusMessage;
    public static boolean logDiscordErrorsToServerChat;
    public static String discordErrorsChatPlayerSelector;
    public static String modLocale;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        discordBotToken = DISCORD_BOT_TOKEN.get();
        discordChannelId = DISCORD_CHANNEL_ID.get();
        logDiscordMessages = LOG_DISCORD_MESSAGES.get();
        enablePinnedStatusMessage = ENABLE_PINNED_STATUS_MESSAGE.get();
        logDiscordErrorsToServerChat = LOG_DISCORD_ERRORS_TO_SERVER_CHAT.get();
        discordErrorsChatPlayerSelector = DISCORD_ERRORS_CHAT_PLAYER_SELECTOR.get();
        modLocale = MOD_LOCALE.get();
    }
}

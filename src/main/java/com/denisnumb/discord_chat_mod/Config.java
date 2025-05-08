package com.denisnumb.discord_chat_mod;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = DiscordChatMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<String> DISCORD_BOT_TOKEN = COMMON_BUILDER
            .comment(" Bot access token" +
                    "\n [!] Make sure all Privileged Gateway Intents are enabled on https://discord.com/developers/applications/<your_app_id>/bot" +
                    "\n [!] Make sure the bot has the following permissions on the server:" +
                    "\n [!] - VIEW_CHANNEL" +
                    "\n [!] - MESSAGE_SEND" +
                    "\n [!] - MESSAGE_SEND_IN_THREADS" +
                    "\n [!] - MESSAGE_EMBED_LINKS" +
                    "\n [!] - MESSAGE_ATTACH_FILES" +
                    "\n [!] - MESSAGE_MANAGE" +
                    "\n [!] - MESSAGE_HISTORY")
            .define("discordBotToken", "");

    public static final ModConfigSpec.ConfigValue<String> DISCORD_CHANNEL_ID = COMMON_BUILDER
            .comment(" Discord channel ID for messaging with MineCraft\n [!] Make sure the bot has access to the channel and all the permissions listed above.")
            .define("discordChannelId", "");

    public static final ModConfigSpec.BooleanValue LOG_DISCORD_MESSAGES = COMMON_BUILDER
            .comment(" Do logging to the server console messages from discord")
            .define("logDiscordMessages", true);

    public static final ModConfigSpec.BooleanValue ENABLE_PINNED_STATUS_MESSAGE = COMMON_BUILDER
            .comment(" Create a pinned message with the current server status and player list")
            .define("enablePinnedStatusMessage", true);

    public static final ModConfigSpec.BooleanValue LOG_DISCORD_ERRORS_TO_SERVER_CHAT = COMMON_BUILDER
            .comment(" Notify about internal Discord interaction errors in the server's in-game chat")
            .define("logDiscordErrorsToServerChat", true);

    public static final ModConfigSpec.ConfigValue<String> DISCORD_ERRORS_CHAT_PLAYER_SELECTOR = COMMON_BUILDER
            .comment(" If logDiscordErrorsToServerChat=true, then the errors in the chat will be seen by players with the specified selector" +
                    "\n By default, \"@a\" — all players. You can specify a specific nickname or attribute, for example, \"@a[tag=admin]\"")
            .define("discordErrorsChatPlayerSelector", "@a");

    public static final ModConfigSpec.ConfigValue<String> MOD_LOCALE = COMMON_BUILDER
            .comment(" Mod locale")
            .define("modLocale", "en_us");

    public static final ModConfigSpec.BooleanValue TRANSLATE_UNICODE_EMOJIS_TO_ALIASES = CLIENT_BUILDER
            .comment(" Convert emoji sent from Discord to their names. For example, \"\uD83D\uDE03\" will be converted to \":smiley:\"" +
                    "\n Can be used for correct compatibility with the Emojiful mod")
            .define("translateUnicodeEmojisToTextAliases", false);

    public static final ModConfigSpec.IntValue MAX_CHAT_HISTORY = CLIENT_BUILDER
            .comment(" Maximum number of messages in the chat." +
                    "\n When the number of messages exceeds this value, old messages are automatically deleted." +
                    "\n By default, this value is 100 in the vanilla game.")
            .defineInRange("maxChatHistory", 500, 20, Integer.MAX_VALUE);

    static final ModConfigSpec COMMON_SPEC = COMMON_BUILDER.build();
    static final ModConfigSpec CLIENT_SPEC = CLIENT_BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {}
}

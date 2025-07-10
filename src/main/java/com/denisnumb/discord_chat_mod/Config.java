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
            .comment("""
                     Bot access token\
                    
                     [!] Make sure all Privileged Gateway Intents are enabled on https://discord.com/developers/applications/<your_app_id>/bot\
                    
                     [!] Make sure the bot has the following permissions on the server:\
                    
                     [!] - VIEW_CHANNEL\
                    
                     [!] - MESSAGE_SEND\
                    
                     [!] - MESSAGE_SEND_IN_THREADS\
                    
                     [!] - MESSAGE_EMBED_LINKS\
                    
                     [!] - MESSAGE_ATTACH_FILES\
                    
                     [!] - MESSAGE_MANAGE\
                    
                     [!] - MESSAGE_HISTORY\
                    """)
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

    public static final ModConfigSpec.BooleanValue EMOJIFUL_COMPATIBILITY = CLIENT_BUILDER
            .comment("""
                     Can be set to "true" for correct compatibility with the Emojiful mod. Enabling this option:\
                    
                     1) Disables Discord Chat Mod's emoji suggestions to avoid conflicts with Emojiful\
                    
                     2) Disables Discord Chat Mod's rendering of custom Discord Emoji\
                    
                     3) Convert emoji sent from Discord to their names. For example, "\uD83D\uDE03" will be converted to ":smiley:"\
                    """)
            .define("emojifulCompatibility", false);

    public static final ModConfigSpec.IntValue MAX_CHAT_HISTORY = CLIENT_BUILDER
            .comment("""
                     Maximum number of messages in the chat.\
                    
                     When the number of messages exceeds this value, old messages are automatically deleted.\
                    
                     By default, this value is 100 in the vanilla game.\
                    """)
            .defineInRange("maxChatHistory", 500, 20, Integer.MAX_VALUE);

    static final ModConfigSpec COMMON_SPEC = COMMON_BUILDER.build();
    static final ModConfigSpec CLIENT_SPEC = CLIENT_BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {}
}

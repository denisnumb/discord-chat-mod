package com.denisnumb.discord_chat_mod.config;

public class ConfigComments {
    public static final String DISCORD_BOT_TOKEN_COMMENT = """
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
                    """;
    public static final String DISCORD_CHANNEL_ID_COMMENT
            = " Discord channel ID for messaging with MineCraft\n [!] Make sure the bot has access to the channel and all the permissions listed above.";

    public static final String LOG_DISCORD_MESSAGES_COMMENT = " Do logging to the server console messages from discord";

    public static final String ENABLE_PINNED_STATUS_MESSAGE_COMMENT = " Create a pinned message with the current server status and player list";

    public static final String LOG_DISCORD_ERRORS_TO_SERVER_CHAT_COMMENT = " Notify about internal Discord interaction errors in the server's in-game chat";

    public static final String DISCORD_ERRORS_CHAT_PLAYER_SELECTOR_COMMENT
            = " If logDiscordErrorsToServerChat=true, then the errors in the chat will be seen by players with the specified selector" +
            "\n By default, \"@a\" — all players. You can specify a specific nickname or attribute, for example, \"@a[tag=admin]\"";

    public static final String MOD_LOCALE_COMMENT = " Mod locale";

    public static final String ENABLE_WEBHOOK_MODE_COMMENT
            = """
             If true, server and player messages will be sent in webhook mode.\
            
             [!] Webhook mode works only in regular channels (Threads does not work)\
            
             [!] The bot must have Manage Webhooks permission\
            """;

    public static final String WEBHOOK_SERVER_NAME_COMMENT = " Webhook name for displaying server messages";

    public static final String WEBHOOK_SERVER_AVATAR_URL_COMMENT
            = " Avatar url for the webhook for displaying server messages" +
            "\n Leave blank to display the connected bot's avatar";

    public static final String ENABLE_SET_AVATAR_URL_COMMAND_COMMENT
            = " If true, then all players will have access to the /set_avatar_url <url> command to set a custom avatar, which will be used instead of the webhookPlayerAvatarUrl parameter." +
            "\n The /remove_avatar_url command will also be available to remove an avatar.";

    public static final String WEBHOOK_PLAYER_AVATAR_URL_COMMENT
            = """
             Url to get the player's display avatar\
            
             You specify a link with dynamic parameters <uuid> and <name>, which will be automatically substituted when requesting an image\
            
             <uuid> — player's UUID\
            
             <name> — player's nickname\
            
             Default: https://mc-heads.net/avatar/<name>.png\
            """;

    public static final String WEBHOOK_PLAYER_DEFAULT_AVATAR_URL_COMMENT
            = " Url to get the player's display avatar if dynamic link is invalid" +
            "\n Default: https://mc-heads.net/avatar.png";

    public static final String PROXY_HOSTNAME_COMMENT
            = " Configuring the HTTP proxy when connecting with Discord. Do not modify if you don't understand what this is.";

    public static final String PROXY_USER_COMMENT
            = " Leave blank if no certificate required.";

    public static final String DUPLICATE_MESSAGES_COMMENT
            = " If true, messages will still be sent to the main channel, but will also be duplicated to the specified channels.";

    public static final String PINNED_STATUS_MESSAGE_CHANNEL_ID_COMMENT
            = " Overrides the default channel for pinned server status message. If empty, uses discordChannelId.";

    public static final String DEATHS_CHANNEL_ID_COMMENT
            = " Overrides the default channel for player death messages. If empty, uses discordChannelId.";

    public static final String ADVANCEMENTS_CHANNEL_ID_COMMENT
            = " Overrides the default channel for player advancement messages. If empty, uses discordChannelId.";

    public static final String SERVER_START_STOP_CHANNEL_ID_COMMENT
            = " Overrides the default channel for server started/closed messages. If empty, uses discordChannelId.";

    public static final String PLAYER_JOIN_LEAVE_CHANNEL_ID_COMMENT
            = " Overrides the default channel for player join/leave messages. If empty, uses discordChannelId.";

    public static final String PLAYER_CHAT_MESSAGES_CHANNEL_ID_COMMENT
            = " Overrides the default channel for messages from players in Minecraft chat. If empty, uses discordChannelId.";

    public static final String SCREENSHOTS_CHANNEL_ID_COMMENT
            = " Overrides the default channel for screenshots sent from Minecraft. If empty, uses discordChannelId.";

    public static final String TELLRAW_CHANNEL_ID_COMMENT
            = " Overrides the default channel for messages sent using /tellraw @a command. If empty, uses discordChannelId.";

    public static final String SAY_CHANNEL_ID_COMMENT
            = " Overrides the default channel for messages sent using /say command. If empty, uses discordChannelId.";

    public static final String EMOJIFUL_COMPATIBILITY_COMMENT = """
                     Can be set to "true" for correct compatibility with the Emojiful mod. Enabling this option:\
                    
                     1) Disables Discord Chat Mod's emoji suggestions to avoid conflicts with Emojiful\
                    
                     2) Disables Discord Chat Mod's rendering of custom Discord Emoji\
                    
                     3) Convert emoji sent from Discord to their names. For example, "\uD83D\uDE03" will be converted to ":smiley:"\
                    """;

    public static final String MAX_CHAT_HISTORY_COMMENT = """
                     Maximum number of messages in the chat.\
                    
                     When the number of messages exceeds this value, old messages are automatically deleted.\
                    
                     By default, this value is 100 in the vanilla game.\
                    """;
}

package com.denisnumb.discord_chat_mod.config;

public class ConfigDefaults {
    public static final String DISCORD_BOT_TOKEN_DEFAULT = "";
    public static final String SERVER_LOGS_CHANNEL_ID_DEFAULT = "";
    public static final String SERVER_LOGS_TO_DISCORD_LOGGING_LEVEL_DEFAULT = "INFO";
    public static final String SERVER_LOGS_PATTERN_DEFAULT = "[%d{HH:mm:ss}] [%t/%level] (%logger{1}) %msg%n";
    public static final boolean SERVER_LOGS_COMMANDS_ONLY_DEFAULT = false;
    public static final boolean LOG_DISCORD_MESSAGES_DEFAULT = true;
    public static final boolean LOG_DISCORD_ERRORS_TO_SERVER_CHAT_DEFAULT = true;
    public static final String DISCORD_ERRORS_CHAT_PLAYER_SELECTOR_DEFAULT = "@a";
    public static final String MOD_LOCALE_DEFAULT = "en_us";
    public static final int UTC_OFFSET_HOURS_DEFAULT = 0;

    // =================================================================================================================
    //                                          DISCORD SLASH COMMANDS DEFAULTS
    // =================================================================================================================

    public static final boolean ENABLE_SLASH_COMMANDS_DEFAULT = true;
    public static final java.util.List<String> SLASH_COMMAND_ALLOWED_ROLES_DEFAULT = java.util.List.of();

    // =================================================================================================================
    //                                              WEBHOOK MODE DEFAULTS
    // =================================================================================================================

    public static final boolean ENABLE_WEBHOOK_MODE_DEFAULT = false;
    public static final String WEBHOOK_SERVER_NAME_DEFAULT = "Minecraft Server";
    public static final String WEBHOOK_SERVER_AVATAR_URL_DEFAULT = "";
    public static final boolean ENABLE_SET_AVATAR_URL_COMMAND_DEFAULT = true;
    public static final String WEBHOOK_PLAYER_AVATAR_URL_DEFAULT = "https://mc-heads.net/avatar/<name>.png";
    public static final String WEBHOOK_PLAYER_DEFAULT_AVATAR_URL_DEFAULT = "https://mc-heads.net/avatar/steve_head_png";

    // =================================================================================================================
    //                                              DISCORD PROXY DEFAULTS
    // =================================================================================================================

    public static final String PROXY_HOSTNAME_DEFAULT = "";
    public static final int PROXY_PORT_DEFAULT = 1234;
    public static final String  PROXY_USER_DEFAULT = "";
    public static final String  PROXY_PASSWORD_DEFAULT = "";

    // =================================================================================================================
    //                                          DISCORD GUILD CONFIG DEFAULTS
    // =================================================================================================================

    public static final String GUILD_ID_DEFAULT = "";
    public static final String DEFAULT_CHANNEL_ID_DEFAULT = "";
    public static final boolean DUPLICATE_MESSAGES_DEFAULT = false;
    public static final String OVERRIDE_CHANNEL_ID_DEFAULT = "";
    public static final boolean ENABLE_PINNED_STATUS_MESSAGE_DEFAULT = true;

    // =================================================================================================================
    //                                      MINECRAFT CHAT CUSTOMIZATION DEFAULTS
    // =================================================================================================================

    public static final boolean ENABLE_MINECRAFT_CHAT_CUSTOMIZATION_DEFAULT = false;
    public static final String MINECRAFT_CHAT_LINK_COLOR_DEFAULT = "#00b7ff";
    public static final String MINECRAFT_DISCORD_MESSAGES_STYLE_DEFAULT = "**<#5f78d9>[discord]<#5f78d9/>** <{member}> {message}";
    public static final String MINECRAFT_PLAYER_MESSAGE_STYLE_DEFAULT = "<{player}> {message}";
    public static final String MINECRAFT_PLAYER_JOINED_STYLE_DEFAULT = "<yellow>{player} {multiplayer.player.joined}<yellow/>";
    public static final String MINECRAFT_PLAYER_LEFT_STYLE_DEFAULT = "<yellow>{player} {multiplayer.player.left}<yellow/>";
    public static final String MINECRAFT_PLAYER_DEATH_CAUSE_STYLE_DEFAULT = "{death_cause}";
    public static final String MINECRAFT_PLAYER_DEATH_NAME_STYLE_DEFAULT = "{player}";
    public static final String MINECRAFT_PLAYER_DEATH_SECOND_ENTITY_STYLE_DEFAULT = "{second_entity}";
    public static final String MINECRAFT_PLAYER_DEATH_WEAPON_STYLE_DEFAULT = "{item}";
    public static final String MINECRAFT_PLAYER_ADVANCEMENT_TASK_STYLE_DEFAULT = "{player} {chat.type.advancement.task} <green>{advancement}<green/>";
    public static final String MINECRAFT_PLAYER_ADVANCEMENT_GOAL_STYLE_DEFAULT = "{player} {chat.type.advancement.goal} <green>{advancement}<green/>";
    public static final String MINECRAFT_PLAYER_ADVANCEMENT_CHALLENGE_STYLE_DEFAULT = "{player} {chat.type.advancement.challenge} <darkpurple>{advancement}<darkpurple/>";
    public static final String MINECRAFT_TEAM_MESSAGE_SENT_STYLE_DEFAULT = "-> {team} <{player}> {message}";
    public static final String MINECRAFT_TEAM_MESSAGE_RECEIVED_STYLE_DEFAULT = "{team} <{player}> {message}";
    public static final String MINECRAFT_TELL_MESSAGE_SENT_STYLE_DEFAULT = "<grey>*{commands.message.display.outgoing} {receiver}: {message}*<grey/>";
    public static final String MINECRAFT_TELL_MESSAGE_RECEIVED_STYLE_DEFAULT = "<grey>*{sender} {commands.message.display.incoming}: {message}*<grey/>";
    public static final String MINECRAFT_SAY_COMMAND_STYLE_DEFAULT = "[{player}] {message}";
    public static final String MINECRAFT_ME_COMMAND_STYLE_DEFAULT = "* {player} {message}";

    // =================================================================================================================
    //                                      DISCORD CHAT CUSTOMIZATION DEFAULTS
    // =================================================================================================================

    public static final String DISCORD_PLAYER_MESSAGE_STYLE_DEFAULT = 
            """
            {
                "content": "`<{player}>` {message}"
            }
            """;
    
    public static final String DISCORD_PLAYER_MESSAGE_WEBHOOK_STYLE_DEFAULT = 
            """
            {
                "content": "{message}"
            }
            """;
    
    public static final String DISCORD_PLAYER_JOINED_STYLE_DEFAULT =
            """
            {
                "embed": {
                    "author": {
                        "name": "{player} {multiplayer.player.joined}",
                        "icon_url": "{player_avatar_url}"
                    },
                    "color": "#2ECC71"
                }
            }
            """;
    
    public static final String DISCORD_PLAYER_LEFT_STYLE_DEFAULT =
            """
            {
                "embed": {
                    "author": {
                        "name": "{player} {multiplayer.player.left}",
                        "icon_url": "{player_avatar_url}"
                    },
                    "color": "#E74C3C"
                }
            }
            """;

    public static final String DISCORD_PLAYER_DEATH_CAUSE_STYLE_DEFAULT = "{death_cause}";
    public static final String DISCORD_PLAYER_DEATH_NAME_STYLE_DEFAULT = "{player}";
    public static final String DISCORD_PLAYER_DEATH_SECOND_ENTITY_STYLE_DEFAULT = "{second_entity}";
    public static final String DISCORD_PLAYER_DEATH_WEAPON_STYLE_DEFAULT = "{item}";
    public static final String DISCORD_PLAYER_DEATH_MESSAGE_STYLE_DEFAULT =
            """
            {
                "embed": {
                    "author": {
                        "name": "{death_message}",
                        "icon_url": "{player_avatar_url}"
                    }
                }
            }
            """;

    public static final String DISCORD_PLAYER_ADVANCEMENT_TASK_STYLE_DEFAULT =
            """
            {
                "embed": {
                    "title": "**{advancement}**",
                    "description": "{description}",
                    "color": "#F1C40F",
                    "author": {
                        "name": "{player} {chat.type.advancement.task}",
                        "icon_url": "{player_avatar_url}"
                    },
                    "thumbnail": {
                        "url": "{icon_url}"
                    }
                }
            }
            """;

    public static final String DISCORD_PLAYER_ADVANCEMENT_GOAL_STYLE_DEFAULT =
            """
            {
                "embed": {
                    "title": "**{advancement}**",
                    "description": "{description}",
                    "color": "#F1C40F",
                    "author": {
                        "name": "{player} {chat.type.advancement.goal}",
                        "icon_url": "{player_avatar_url}"
                    },
                    "thumbnail": {
                        "url": "{icon_url}"
                    }
                }
            }
            """;

    public static final String DISCORD_PLAYER_ADVANCEMENT_CHALLENGE_STYLE_DEFAULT =
            """
            {
                "embed": {
                    "title": "**{advancement}**",
                    "description": "{description}",
                    "color": "#A700A7",
                    "author": {
                        "name": "{player} {chat.type.advancement.challenge}",
                        "icon_url": "{player_avatar_url}"
                    },
                    "thumbnail": {
                        "url": "{icon_url}"
                    }
                }
            }
            """;

    public static final String DISCORD_SAY_COMMAND_STYLE_DEFAULT =
            """
            {
                "content": "`[{player}]` {message}"
            }
            """;

    public static final String DISCORD_ME_COMMAND_STYLE_DEFAULT =
            """
            {
                "content": "* {player} {message}"
            }
            """;

    public static final String DISCORD_ME_COMMAND_WEBHOOK_STYLE_DEFAULT =
            """
            {
                "content": "> {message}"
            }
            """;

    public static final String DISCORD_TELLRAW_COMMAND_STYLE_DEFAULT =
            """
            {
                "content": "{message}"
            }
            """;

    public static final String DISCORD_SCREENSHOT_MESSAGE_STYLE_DEFAULT =
            """
            {
                "content": "`<{player}>`"
            }
            """;

    public static final String DISCORD_SCREENSHOT_MESSAGE_WEBHOOK_STYLE_DEFAULT =
            """
            {
                "content": ""
            }
            """;

    public static final String DISCORD_SERVER_STARTED_MESSAGE_STYLE_DEFAULT =
            """
            {
                "embed": {
                    "description": "{discord_chat_mod.server.started} <t:{timestamp}:R>",
                    "color": "#2ECC71"
                }
            }
            """;

    public static final String DISCORD_LOCAL_SERVER_STARTED_MESSAGE_STYLE_DEFAULT =
            """
            {
                "embed": {
                    "description": "{discord_chat_mod.server.local_started} <t:{timestamp}:R>",
                    "color": "#2ECC71"
                }
            }
            """;

    public static final String DISCORD_SERVER_CLOSED_MESSAGE_STYLE_DEFAULT =
            """
            {
                "embed": {
                    "description": "{discord_chat_mod.server.closed} <t:{timestamp}:R>",
                    "color": "#E74C3C"
                }
            }
            """;

    public static final String DISCORD_PINNED_STATUS_MESSAGE_SERVER_UNAVAILABLE_STYLE_DEFAULT =
            """
            {
                "embed": {
                    "description": "{discord_chat_mod.server.status.unavailable}",
                    "color": "#E74C3C"
                }
            }
            """;

    public static final String DISCORD_PINNED_STATUS_MESSAGE_SERVER_AVAILABLE_STYLE_DEFAULT =
            """
            {
                "embed": {
                    "description": "{discord_chat_mod.server.status.available}",
                    "color": "#1F8B4C"
                }
            }
            """;

    public static final String DISCORD_PINNED_STATUS_MESSAGE_PLAYER_LIST_DELIMITER_DEFAULT = "\n";

    public static final String DISCORD_PINNED_STATUS_MESSAGE_PLAYER_LIST_NICKNAME_STYLE_DEFAULT = "{player}";

    public static final String DISCORD_PINNED_STATUS_MESSAGE_STYLE_DEFAULT =
            """
            {
                "embed": {
                    "title": "{discord_chat_mod.server.status.online_players}",
                    "description": "{player_list}",
                    "color": "#2ECC71"
                }
            }
            """;

    public static final String DISCORD_GUILD_FORWARDED_MESSAGE_USERNAME_STYLE_DEFAULT = "{user} ({guild})";

    // =================================================================================================================
    //                                             CLIENT CONFIG DEFAULTS
    // =================================================================================================================

    public static final boolean EMOJIFUL_COMPATIBILITY_DEFAULT = false;
    public static final int MAX_CHAT_HISTORY_DEFAULT = 500;
}

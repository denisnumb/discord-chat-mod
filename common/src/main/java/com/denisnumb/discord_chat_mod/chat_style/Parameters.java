package com.denisnumb.discord_chat_mod.chat_style;

public final class Parameters {
    private Parameters() {}
    public static final String MESSAGE = "{message}";
    public static final String PLAYER = "{player}";
    public static final String SENDER = "{sender}";
    public static final String RECEIVER = "{receiver}";
    public static final String TEAM = "{team}";
    public static final String MEMBER = "{member}";
    public static final String USER = "{user}";
    public static final String PLAYER_AVATAR_URL = "{player_avatar_url}";
    public static final String X = "{x}";
    public static final String Y = "{y}";
    public static final String Z = "{z}";
    public static final String HH = "{HH}";
    public static final String MM = "{MM}";
    public static final String SS = "{SS}";
    public static final String DIMENSION = "{dimension}";
    public static final String DEATH_CAUSE = "{death_cause}";
    public static final String SECOND_ENTITY = "{second_entity}";
    public static final String ITEM = "{item}";
    public static final String DEATH_MESSAGE = "{death_message}";
    public static final String ADVANCEMENT = "{advancement}";
    public static final String DESCRIPTION = "{description}";
    public static final String ICON_URL = "{icon_url}";
    public static final String SERVER_PORT = "{server_port}";
    public static final String PLAYER_LIST = "{player_list}";
    public static final String PLAYER_COUNT = "{player_count}";
    public static final String MAX_PLAYERS = "{max_players}";
    public static final String IMAGE_URL = "{image_url}";
    public static final String TIMESTAMP = "{timestamp}";
    public static final String DATETIME = "{datetime}";
    public static final String GUILD = "{guild}";
    public static final String COUNTER = "{counter}";
    public static final String COMMAND = "{command}";

    public static final class Translatable {
        private Translatable() {}
        public static final String SERVER_UNAVAILABLE = "{discord_chat_mod.server.status.unavailable}";
        public static final String SERVER_AVAILABLE = "{discord_chat_mod.server.status.available}";
        public static final String ONLINE_PLAYERS = "{discord_chat_mod.server.status.online_players}";
        public static final String SERVER_STARTED = "{discord_chat_mod.server.started}";
        public static final String LOCAL_SERVER_STARTED = "{discord_chat_mod.server.local_started}";
        public static final String SERVER_CLOSED = "{discord_chat_mod.server.closed}";
        public static final String ADVANCEMENT_TASK = "{chat.type.advancement.task}";
        public static final String ADVANCEMENT_GOAL = "{chat.type.advancement.goal}";
        public static final String ADVANCEMENT_CHALLENGE = "{chat.type.advancement.challenge}";
        public static final String COMMANDS_MESSAGE_DISPLAY_INCOMING = "{commands.message.display.incoming}";
        public static final String COMMANDS_MESSAGE_DISPLAY_OUTGOING = "{commands.message.display.outgoing}";
        public static final String PLAYER_JOINED = "{multiplayer.player.joined}";
        public static final String PLAYER_LEFT = "{multiplayer.player.left}";

        public static String unwrapBraces(String param){
            if (param.length() >= 2 && param.charAt(0) == '{' && param.charAt(param.length() - 1) == '}') {
                return param.substring(1, param.length() - 1);
            }

            return param;
        }
    }
}

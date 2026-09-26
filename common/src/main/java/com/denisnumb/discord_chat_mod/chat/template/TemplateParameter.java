package com.denisnumb.discord_chat_mod.chat.template;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum TemplateParameter implements TemplatePlaceholder {
    MESSAGE("{message}"),
    PLAYER("{player}"),
    SENDER("{sender}"),
    RECEIVER("{receiver}"),
    TEAM("{team}"),
    MEMBER("{member}"),
    USER("{user}"),
    PLAYER_AVATAR_URL("{player_avatar_url}"),
    X("{x}"),
    Y("{y}"),
    Z("{z}"),
    HH("{HH}"),
    MM("{MM}"),
    SS("{SS}"),
    DIMENSION("{dimension}"),
    DEATH_CAUSE("{death_cause}"),
    SECOND_ENTITY("{second_entity}"),
    ITEM("{item}"),
    DEATH_MESSAGE("{death_message}"),
    ADVANCEMENT("{advancement}"),
    DESCRIPTION("{description}"),
    ICON_URL("{icon_url}"),
    AVATAR_URL("{avatar_url}"),
    SERVER_PORT("{server_port}"),
    PLAYER_LIST("{player_list}"),
    PLAYER_COUNT("{player_count}"),
    MAX_PLAYERS("{max_players}"),
    IMAGE_URL("{image_url}"),
    TIMESTAMP("{timestamp}"),
    DATETIME("{datetime}"),
    GUILD("{guild}"),
    COUNTER("{counter}"),
    COMMAND("{command}");

    private final String placeholder;
    private final String markdownSafePlaceholder;
    private static final Map<String, TemplatePlaceholder> BY_PLACEHOLDER = new HashMap<>();

    static {
        for (TemplateParameter value : values()) {
            BY_PLACEHOLDER.put(value.placeholder, value);
            BY_PLACEHOLDER.put(value.markdownSafePlaceholder, value);
        }
    }

    TemplateParameter(String placeholder) {
        this.placeholder = placeholder;
        this.markdownSafePlaceholder = placeholder.replace("_", ".");
    }

    @Override
    public String getPlaceholder() {
        return placeholder;
    }

    @Override
    public String getMarkdownSafePlaceholder() {
        return markdownSafePlaceholder;
    }

    @Override
    public String toString() {
        return placeholder;
    }

    public static TemplatePlaceholder fromString(String value) {
        TemplatePlaceholder found = BY_PLACEHOLDER.get(value);
        if (found != null) {
            return found;
        }

        TemplatePlaceholder translatable = Translatable.BY_PLACEHOLDER.get(value);
        if (translatable != null) {
            return translatable;
        }

        throw new IllegalArgumentException("Unknown placeholder: " + value);
    }

    public enum Translatable implements TemplatePlaceholder {
        SERVER_UNAVAILABLE("{discord_chat_mod.server.status.unavailable}"),
        SERVER_AVAILABLE("{discord_chat_mod.server.status.available}"),
        ONLINE_PLAYERS("{discord_chat_mod.server.status.online_players}", TemplateParameter.PLAYER_COUNT, TemplateParameter.MAX_PLAYERS),
        SERVER_STARTED("{discord_chat_mod.server.started}"),
        LOCAL_SERVER_STARTED("{discord_chat_mod.server.local_started}", TemplateParameter.SERVER_PORT),
        SERVER_CLOSED("{discord_chat_mod.server.closed}"),
        FORWARDED_MESSAGE("{discord_chat_mod.discord.forwarded_guild_message}", TemplateParameter.MEMBER, TemplateParameter.GUILD),
        ADVANCEMENT_TASK("{chat.type.advancement.task}", TemplateParameter.PLAYER, TemplateParameter.ADVANCEMENT),
        ADVANCEMENT_GOAL("{chat.type.advancement.goal}", TemplateParameter.PLAYER, TemplateParameter.ADVANCEMENT),
        ADVANCEMENT_CHALLENGE("{chat.type.advancement.challenge}", TemplateParameter.PLAYER, TemplateParameter.ADVANCEMENT),
        COMMANDS_MESSAGE_DISPLAY_INCOMING("{commands.message.display.incoming}", TemplateParameter.SENDER, TemplateParameter.MESSAGE),
        COMMANDS_MESSAGE_DISPLAY_OUTGOING("{commands.message.display.outgoing}", TemplateParameter.RECEIVER, TemplateParameter.MESSAGE),
        PLAYER_JOINED("{multiplayer.player.joined}", TemplateParameter.PLAYER),
        PLAYER_LEFT("{multiplayer.player.left}", TemplateParameter.PLAYER);

        private final String placeholder;
        private final String markdownSafePlaceholder;
        private final List<TemplatePlaceholder> orderedArgs;
        private static final Map<String, Translatable> BY_PLACEHOLDER = new HashMap<>();

        static {
            for (Translatable value : values()) {
                BY_PLACEHOLDER.put(value.placeholder, value);
                BY_PLACEHOLDER.put(value.markdownSafePlaceholder, value);
            }
        }

        Translatable(String placeholder, TemplateParameter... orderedArgs) {
            this.placeholder = placeholder;
            this.markdownSafePlaceholder = placeholder.replace("_", ".");
            this.orderedArgs = List.of(orderedArgs);
        }

        @Override
        public String getPlaceholder() {
            return placeholder;
        }

        @Override
        public String getMarkdownSafePlaceholder() {
            return markdownSafePlaceholder;
        }

        @Override
        public String toString() {
            return placeholder;
        }

        public List<TemplatePlaceholder> getOrderedArgs() {
            return orderedArgs;
        }

        public String unwrapBraces() {
            return placeholder.substring(1, placeholder.length() - 1);
        }
    }
}

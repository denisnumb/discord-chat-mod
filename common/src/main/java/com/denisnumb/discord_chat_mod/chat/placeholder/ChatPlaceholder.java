package com.denisnumb.discord_chat_mod.chat.placeholder;

public enum ChatPlaceholder {
    ITEM("[i]");

    private static final char FIRST_MARKER = '';

    private final String token;

    ChatPlaceholder(String token) {
        this.token = token;
    }

    public String token() {
        return token;
    }

    public char marker() {
        return (char) (FIRST_MARKER + ordinal());
    }

    static boolean isMarker(char c) {
        return c >= FIRST_MARKER && c < FIRST_MARKER + values().length;
    }
}

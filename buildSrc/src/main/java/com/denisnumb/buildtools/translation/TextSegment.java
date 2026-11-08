package com.denisnumb.buildtools.translation;

public record TextSegment(String text, int token) {
    public static TextSegment text(String text) {
        return new TextSegment(text, -1);
    }

    public static TextSegment token(int index) {
        return new TextSegment(null, index);
    }

    public boolean isToken() {
        return token >= 0;
    }
}

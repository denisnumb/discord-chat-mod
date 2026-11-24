package com.denisnumb.discord_chat_mod.api;

import java.util.Objects;
import java.util.regex.Pattern;

final class Names {
    private Names() {}

    private static final Pattern NAMESPACE = Pattern.compile("[a-z][a-z0-9_-]{1,63}");
    private static final Pattern NAME = Pattern.compile("[a-z0-9_-]{1,64}");

    static String namespace(String value) {
        Objects.requireNonNull(value, "namespace");
        if (!NAMESPACE.matcher(value).matches())
            throw new IllegalArgumentException("Invalid mod id \"" + value + "\": expected [a-z][a-z0-9_-]{1,63}");
        return value;
    }

    static String name(String value) {
        Objects.requireNonNull(value, "name");
        if (!NAME.matcher(value).matches())
            throw new IllegalArgumentException("Invalid name \"" + value + "\": expected [a-z0-9_-]{1,64}");
        return value;
    }
}

package com.denisnumb.discord_chat_mod.compat;

public class VanishCompatProvider {
    private static IVanishCompat instance = IVanishCompat.NOOP;

    public static void set(IVanishCompat compat) {
        instance = compat;
    }

    public static IVanishCompat get() {
        return instance;
    }
}

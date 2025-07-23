package com.denisnumb.discord_chat_mod.config;

public class PlatformConfig {
    private static IPlatformConfig config;

    public static void setConfigProvider(IPlatformConfig cfg) {
        config = cfg;
    }

    public static IPlatformConfig getConfig() {
        return config;
    }
}

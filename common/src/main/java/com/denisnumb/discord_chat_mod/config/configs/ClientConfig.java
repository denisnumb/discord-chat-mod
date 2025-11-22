package com.denisnumb.discord_chat_mod.config.configs;

import com.electronwill.nightconfig.core.CommentedConfig;

import static com.denisnumb.discord_chat_mod.config.ConfigComments.EMOJIFUL_COMPATIBILITY_COMMENT;
import static com.denisnumb.discord_chat_mod.config.ConfigComments.MAX_CHAT_HISTORY_COMMENT;
import static com.denisnumb.discord_chat_mod.config.ConfigDefaults.EMOJIFUL_COMPATIBILITY_DEFAULT;
import static com.denisnumb.discord_chat_mod.config.ConfigDefaults.MAX_CHAT_HISTORY_DEFAULT;

public class ClientConfig {
    public static boolean emojifulCompatibility;
    public static int maxChatHistory;

    public static void loadClientConfig(CommentedConfig clientConfig){
        emojifulCompatibility = clientConfig.getOrElse("emojifulCompatibility", EMOJIFUL_COMPATIBILITY_DEFAULT);
        clientConfig.set("emojifulCompatibility", emojifulCompatibility);
        clientConfig.setComment("emojifulCompatibility", EMOJIFUL_COMPATIBILITY_COMMENT);

        maxChatHistory = clientConfig.getOrElse("maxChatHistory", MAX_CHAT_HISTORY_DEFAULT);
        if (maxChatHistory < 20)
            maxChatHistory = 20;
        clientConfig.set("maxChatHistory", maxChatHistory);
        clientConfig.setComment("maxChatHistory", MAX_CHAT_HISTORY_COMMENT + String.format("\n Default: %d\n Range: > 20", MAX_CHAT_HISTORY_DEFAULT));

    }
}

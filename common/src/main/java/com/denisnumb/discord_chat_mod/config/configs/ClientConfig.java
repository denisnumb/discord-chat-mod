package com.denisnumb.discord_chat_mod.config.configs;

import com.denisnumb.discord_chat_mod.config.core.ConfigParameter;
import com.denisnumb.discord_chat_mod.config.core.ConfigSection;
import com.electronwill.nightconfig.core.CommentedConfig;

import static com.denisnumb.discord_chat_mod.config.ConfigComments.*;
import static com.denisnumb.discord_chat_mod.config.ConfigDefaults.*;

public final class ClientConfig extends ConfigSection {
    private ClientConfig() {}

    public static final ConfigParameter<Boolean, Boolean> EMOJIFUL_COMPATIBILITY =
            ConfigParameter.ofBoolean("emojifulCompatibility")
                    .defaultValue(EMOJIFUL_COMPATIBILITY_DEFAULT)
                    .comment(EMOJIFUL_COMPATIBILITY_COMMENT)
                    .build();

    public static final ConfigParameter<Integer, Integer> MAX_CHAT_HISTORY =
            ConfigParameter.ofInt("maxChatHistory")
                    .defaultValue(MAX_CHAT_HISTORY_DEFAULT)
                    .comment(MAX_CHAT_HISTORY_COMMENT)
                    .rangeWithComment(20, Integer.MAX_VALUE)
                    .build();

    public static final ConfigParameter<Integer, Integer> MAX_IMAGE_CACHE_SIZE =
            ConfigParameter.ofInt("maxImageCacheSize")
                    .defaultValue(MAX_IMAGE_CACHE_SIZE_DEFAULT)
                    .comment(MAX_IMAGE_CACHE_SIZE_COMMENT)
                    .rangeWithComment(50, 10000)
                    .build();

    public static final ConfigParameter<Integer, Integer> IMAGE_LOAD_TIMEOUT_MS =
            ConfigParameter.ofInt("imageLoadTimeout")
                    .defaultValue(IMAGE_LOAD_TIMEOUT_DEFAULT)
                    .comment(IMAGE_LOAD_TIMEOUT_COMMENT)
                    .rangeWithComment(5, 300)
                    .transform(seconds -> seconds * 1000)
                    .build();

    public static final ConfigParameter<Boolean, Boolean> ENABLE_ATTACH_IMAGE_BUTTON =
            ConfigParameter.ofBoolean("enableAttachImageButton")
                    .defaultValue(ENABLE_ATTACH_IMAGE_BUTTON_DEFAULT)
                    .comment(ENABLE_ATTACH_IMAGE_BUTTON_COMMENT)
                    .build();

    public static final ConfigParameter<Boolean, Boolean> ENABLE_CLIPBOARD_IMAGE_PASTE =
            ConfigParameter.ofBoolean("enableClipboardImagePaste")
                    .defaultValue(ENABLE_CLIPBOARD_IMAGE_PASTE_DEFAULT)
                    .comment(ENABLE_CLIPBOARD_IMAGE_PASTE_COMMENT)
                    .build();

    public static void load(CommentedConfig clientConfig) {
        ConfigSection.loadAll(ClientConfig.class, clientConfig, clientConfig);
    }
}
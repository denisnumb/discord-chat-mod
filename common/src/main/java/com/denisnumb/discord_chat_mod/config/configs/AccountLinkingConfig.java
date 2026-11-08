package com.denisnumb.discord_chat_mod.config.configs;

import com.denisnumb.discord_chat_mod.config.core.ConfigParameter;
import com.denisnumb.discord_chat_mod.config.core.ConfigSection;
import com.denisnumb.discord_chat_mod.linking.model.RoleSyncDirection;
import com.denisnumb.discord_chat_mod.utils.JavaUtils;
import com.electronwill.nightconfig.core.CommentedConfig;

import java.util.Locale;

import static com.denisnumb.discord_chat_mod.config.ConfigComments.*;
import static com.denisnumb.discord_chat_mod.config.ConfigDefaults.*;

public final class AccountLinkingConfig extends ConfigSection {
    private AccountLinkingConfig() {}

    public static final ConfigParameter<Boolean, Boolean> ENABLED =
            ConfigParameter.ofBoolean("enabled")
                    .defaultValue(ACCOUNT_LINKING_ENABLED_DEFAULT)
                    .comment(ACCOUNT_LINKING_ENABLED_COMMENT)
                    .build();

    public static final ConfigParameter<Integer, Integer> LINK_CODE_TTL_SECONDS =
            ConfigParameter.ofInt("linkCodeTtlSeconds")
                    .defaultValue(ACCOUNT_LINKING_LINK_CODE_TTL_SECONDS_DEFAULT)
                    .comment(ACCOUNT_LINKING_LINK_CODE_TTL_SECONDS_COMMENT)
                    .rangeWithComment(60, 3600)
                    .build();

    public static final ConfigParameter<Integer, Integer> MAX_MINECRAFT_ACCOUNTS_PER_DISCORD_USER =
            ConfigParameter.ofInt("maxMinecraftAccountsPerDiscordUser")
                    .defaultValue(ACCOUNT_LINKING_MAX_ACCOUNTS_PER_DISCORD_USER_DEFAULT)
                    .comment(ACCOUNT_LINKING_MAX_ACCOUNTS_PER_DISCORD_USER_COMMENT)
                    .rangeWithComment(1, 10)
                    .build();

    public static final ConfigParameter<Boolean, Boolean> WHITELIST_LINKED_PLAYERS =
            ConfigParameter.ofBoolean("whitelistLinkedPlayers")
                    .defaultValue(ACCOUNT_LINKING_WHITELIST_LINKED_PLAYERS_DEFAULT)
                    .comment(ACCOUNT_LINKING_WHITELIST_LINKED_PLAYERS_COMMENT)
                    .build();

    public static final ConfigParameter<Boolean, Boolean> UNLINK_ON_GUILD_LEAVE =
            ConfigParameter.ofBoolean("unlinkOnGuildLeave")
                    .defaultValue(ACCOUNT_LINKING_UNLINK_ON_GUILD_LEAVE_DEFAULT)
                    .comment(ACCOUNT_LINKING_UNLINK_ON_GUILD_LEAVE_COMMENT)
                    .build();

    public static final ConfigParameter<String, RoleSyncDirection> ROLE_SYNC_DIRECTION =
            ConfigParameter.ofString("roleSyncDirection")
                    .defaultValue(ACCOUNT_LINKING_ROLE_SYNC_DIRECTION_DEFAULT)
                    .comment(ACCOUNT_LINKING_ROLE_SYNC_DIRECTION_COMMENT)
                    .normalize(value -> value.trim().toUpperCase(Locale.ROOT))
                    .validator((value, fallback) -> JavaUtils.isEnumValue(RoleSyncDirection.class, value) ? value : fallback)
                    .transform(RoleSyncDirection::valueOf)
                    .build();

    public static CommentedConfig load(CommentedConfig commonConfig) {
        CommentedConfig section = commonConfig.getOrElse("accountLinking", commonConfig.createSubConfig());
        ConfigSection.loadAll(AccountLinkingConfig.class, section, section);
        return section;
    }
}

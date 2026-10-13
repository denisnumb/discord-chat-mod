package com.denisnumb.discord_chat_mod.config.configs;

import com.denisnumb.discord_chat_mod.discord.model.ChannelCategory;
import com.denisnumb.discord_chat_mod.discord.slash_commands.permissions.SlashCommandPermissions;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.*;

import static com.denisnumb.discord_chat_mod.config.ConfigComments.*;
import static com.denisnumb.discord_chat_mod.config.ConfigDefaults.*;

public final class DiscordGuildsConfig {
    private DiscordGuildsConfig() {}

    private static final Logger LOGGER = LogUtils.getLogger();
    public static List<DiscordGuildConfig> discordGuildConfigs;
    public static final String SLASH_COMMAND_PERMISSIONS_EVERYONE_KEY = "@everyone";

    public record DiscordGuildConfig(
            String guildId,
            String defaultChannelId,
            String serverLogsChannelId,
            boolean duplicateMessages,
            boolean enablePinnedStatusMessage,
            boolean enableSlashCommands,
            Map<String, SlashCommandPermissions> slashCommandPermissions,
            Map<String, String> channelOverrides
    ) { }

    public static List<CommentedConfig> load(CommentedConfig commonConfig){
        List<CommentedConfig> guildList = commonConfig.getOrElse("guilds", getDefaultGuildConfig(commonConfig));
        discordGuildConfigs = new ArrayList<>();

        for (CommentedConfig guildConfig : guildList) {
            String guildId = guildConfig.getOrElse("guildId", GUILD_ID_DEFAULT);
            if (discordGuildConfigs.stream().map(DiscordGuildConfig::guildId).toList().contains(guildId))
                continue;

            prepareGuildConfig(guildConfig, guildId);

            String mainChannelId = guildConfig.get("defaultChannelId");
            String serverLogsChannelId = guildConfig.get("serverLogsChannelId");
            boolean enablePinnedStatusMessage = guildConfig.get("enablePinnedStatusMessage");
            boolean enableSlashCommands = guildConfig.get("enableSlashCommands");

            Map<String, SlashCommandPermissions> slashCommandPermissions
                    = loadSlashCommandPermissions(guildConfig.get("slashCommandPermissions"), guildId);

            CommentedConfig overrides = guildConfig.get("channelOverrides");
            boolean duplicateMessages = overrides.get("duplicateMessages");
            Map<String, String> channelOverrides = loadChannelOverrides(overrides, guildId);

            discordGuildConfigs.add(
                    new DiscordGuildConfig(
                            guildId,
                            mainChannelId,
                            serverLogsChannelId,
                            duplicateMessages,
                            enablePinnedStatusMessage,
                            enableSlashCommands,
                            slashCommandPermissions,
                            channelOverrides
                    )
            );
        }

        return guildList;
    }

    private static void prepareGuildConfig(CommentedConfig guildConfig, String guildId) {
        createFieldIfNotExists(guildConfig, guildId, "defaultChannelId", DEFAULT_CHANNEL_ID_DEFAULT, DEFAULT_CHANNEL_ID_COMMENT);
        createFieldIfNotExists(guildConfig, guildId, "serverLogsChannelId", SERVER_LOGS_CHANNEL_ID_DEFAULT, SERVER_LOGS_CHANNEL_ID_COMMENT);
        createFieldIfNotExists(guildConfig, guildId, "enablePinnedStatusMessage", ENABLE_PINNED_STATUS_MESSAGE_DEFAULT, ENABLE_PINNED_STATUS_MESSAGE_COMMENT);
        createFieldIfNotExists(guildConfig, guildId, "enableSlashCommands", ENABLE_SLASH_COMMANDS_DEFAULT, ENABLE_SLASH_COMMANDS_COMMENT);

        boolean addExampleRole = false;
        if (!guildConfig.contains("slashCommandPermissions")) {
            LOGGER.info("[guildId: {}] Adding missing section \"slashCommandPermissions\"", guildId);
            guildConfig.set("slashCommandPermissions", guildConfig.createSubConfig());
            guildConfig.setComment("slashCommandPermissions", SLASH_COMMAND_PERMISSIONS_DEFAULT_COMMENT);
            addExampleRole = true;
        }

        // Must run before anything reads the section: removes the legacy mode/allow/deny keys
        migrateSlashCommandsConfig(guildConfig, guildId); // <= 2.7.0
        migrateSlashCommandPermissionsToRolesOnly(guildConfig, guildId); // <= 2.8.0

        CommentedConfig slashCommandPermissions = guildConfig.get("slashCommandPermissions");
        if (addExampleRole){
            CommentedConfig exampleRole = slashCommandPermissions.createSubConfig();
            createFieldIfNotExists(exampleRole, guildId, "allow", SLASH_COMMAND_PERMISSIONS_EXAMPLE_ROLE_ALLOW_DEFAULT, SLASH_COMMAND_PERMISSIONS_EXAMPLE_ROLE_ALLOW_COMMENT);
            createFieldIfNotExists(exampleRole, guildId, "deny", SLASH_COMMAND_PERMISSIONS_EXAMPLE_ROLE_DENY_DEFAULT, SLASH_COMMAND_PERMISSIONS_EXAMPLE_ROLE_DENY_COMMENT);
            slashCommandPermissions.set("ExampleRole", exampleRole);
            slashCommandPermissions.setComment("ExampleRole", SLASH_COMMAND_PERMISSIONS_EXAMPLE_ROLE_COMMENT);
        }

        if (!guildConfig.contains("channelOverrides")) {
            LOGGER.info("[guildId: {}] Adding missing section \"channelOverrides\"", guildId);
            guildConfig.set("channelOverrides", guildConfig.createSubConfig());
        }

        CommentedConfig overrides = guildConfig.get("channelOverrides");
        createFieldIfNotExists(overrides, guildId, "duplicateMessages", DUPLICATE_MESSAGES_DEFAULT, DUPLICATE_MESSAGES_COMMENT);
        for (ChannelCategory category : ChannelCategory.values())
            createFieldIfNotExists(overrides, guildId, category.getConfigName(), OVERRIDE_CHANNEL_ID_DEFAULT, category.getConfigComment());

        // Migrate from old config versions
        migrateScreenshotChannelId(guildConfig, guildId);
    }

    private static <T> void createFieldIfNotExists(CommentedConfig config, String guildId, String key, T defaultValue, String comment) {
        if (!config.contains(key)) {
            LOGGER.info("[guildId: {}] Adding missing field \"{}\"", guildId, key);
            config.set(key, defaultValue);
            config.setComment(key, comment);
        }
    }

    private static List<CommentedConfig> getDefaultGuildConfig(CommentedConfig commonConfig){
        CommentedConfig exampleServer = commonConfig.createSubConfig();
        exampleServer.set("guildId", GUILD_ID_DEFAULT);
        exampleServer.setComment("guildId", GUILD_ID_COMMENT);
        exampleServer.set("defaultChannelId", DEFAULT_CHANNEL_ID_DEFAULT);
        exampleServer.setComment("defaultChannelId", DEFAULT_CHANNEL_ID_COMMENT);
        exampleServer.set("serverLogsChannelId", SERVER_LOGS_CHANNEL_ID_DEFAULT);
        exampleServer.setComment("serverLogsChannelId", SERVER_LOGS_CHANNEL_ID_COMMENT);
        exampleServer.set("enablePinnedStatusMessage", ENABLE_PINNED_STATUS_MESSAGE_DEFAULT);
        exampleServer.setComment("enablePinnedStatusMessage", ENABLE_PINNED_STATUS_MESSAGE_COMMENT);
        exampleServer.set("enableSlashCommands", ENABLE_SLASH_COMMANDS_DEFAULT);
        exampleServer.setComment("enableSlashCommands",ENABLE_SLASH_COMMANDS_COMMENT);

        CommentedConfig slashCommandPermissions = exampleServer.createSubConfig();
        CommentedConfig exampleRole = slashCommandPermissions.createSubConfig();
        exampleRole.set("allow", SLASH_COMMAND_PERMISSIONS_EXAMPLE_ROLE_ALLOW_DEFAULT);
        exampleRole.setComment("allow", SLASH_COMMAND_PERMISSIONS_EXAMPLE_ROLE_ALLOW_COMMENT);
        exampleRole.set("deny", SLASH_COMMAND_PERMISSIONS_EXAMPLE_ROLE_DENY_DEFAULT);
        exampleRole.setComment("deny", SLASH_COMMAND_PERMISSIONS_EXAMPLE_ROLE_DENY_COMMENT);
        slashCommandPermissions.set("ExampleRole", exampleRole);
        slashCommandPermissions.setComment("ExampleRole", SLASH_COMMAND_PERMISSIONS_EXAMPLE_ROLE_COMMENT);

        CommentedConfig overrides = exampleServer.createSubConfig();
        overrides.set("duplicateMessages", DUPLICATE_MESSAGES_DEFAULT);
        overrides.setComment("duplicateMessages", DUPLICATE_MESSAGES_COMMENT);

        for (ChannelCategory category : ChannelCategory.values()){
            overrides.set(category.getConfigName(), OVERRIDE_CHANNEL_ID_DEFAULT);
            overrides.setComment(category.getConfigName(), category.getConfigComment());
        }

        exampleServer.set("slashCommandPermissions", slashCommandPermissions);
        exampleServer.setComment("slashCommandPermissions", SLASH_COMMAND_PERMISSIONS_DEFAULT_COMMENT);

        exampleServer.set("channelOverrides", overrides);

        List<CommentedConfig> guildList = List.of(exampleServer);
        commonConfig.add("guilds", guildList);

        return guildList;
    }

    private static boolean isEveryoneKey(String key, String guildId) {
        return key.equals(guildId) || key.equalsIgnoreCase(SLASH_COMMAND_PERMISSIONS_EVERYONE_KEY);
    }

    private static Map<String, SlashCommandPermissions> loadSlashCommandPermissions(CommentedConfig section, String guildId) {
        Map<String, SlashCommandPermissions> permissions = new LinkedHashMap<>();

        for (CommentedConfig.Entry entry : section.entrySet()) {
            if (!(entry.getValue() instanceof CommentedConfig roleConfig)) {
                LOGGER.warn("[guildId: {}] Ignoring \"slashCommandPermissions.{}\": permissions are defined only inside role sections, e.g. [guilds.slashCommandPermissions.RoleName]", guildId, entry.getKey());
                continue;
            }

            String key = isEveryoneKey(entry.getKey(), guildId) ? guildId : entry.getKey();
            List<String> allow = roleConfig.getOrElse("allow", SLASH_COMMAND_PERMISSIONS_EXAMPLE_ROLE_ALLOW_DEFAULT);
            List<String> deny = roleConfig.getOrElse("deny", SLASH_COMMAND_PERMISSIONS_EXAMPLE_ROLE_DENY_DEFAULT);

            if (permissions.putIfAbsent(key, new SlashCommandPermissions(allow, deny)) != null)
                LOGGER.warn("[guildId: {}] Section \"{}\" duplicates another @everyone section (guild ID / \"@everyone\"). Only the first one is used", guildId, entry.getKey());
        }

        return permissions;
    }

    private static Map<String, String> loadChannelOverrides(CommentedConfig overrides, String guildId) {
        Map<String, String> channelOverrides = new HashMap<>();

        for (CommentedConfig.Entry entry : overrides.entrySet()) {
            if (entry.getKey().equals("duplicateMessages"))
                continue;

            String key = entry.getKey();
            String value = entry.getValue();

            boolean valid = false;
            for (ChannelCategory cat : ChannelCategory.values()) {
                if (cat.getConfigName().equalsIgnoreCase(key)) {
                    valid = true;
                    channelOverrides.put(cat.getConfigName(), value);
                    break;
                }
            }

            if (!valid)
                LOGGER.error("Unknown channel key \"{}\" in channel overrides for guildId: {}", key, guildId);
        }

        return channelOverrides;
    }

    // ==============================================================================
    //                                  MIGRATIONS
    // ==============================================================================

    private static final List<String> LEGACY_SLASH_PERMISSION_KEYS = List.of("mode", "allow", "deny");

    private static boolean hasLegacyKeys(CommentedConfig permissions) {
        return permissions != null && LEGACY_SLASH_PERMISSION_KEYS.stream().anyMatch(key -> isLegacyValue(permissions, key));
    }

    private static boolean isLegacyValue(CommentedConfig permissions, String key) {
        Object value = permissions.get(key);
        return value != null && !(value instanceof CommentedConfig);
    }

    private static List<String> readLegacyList(CommentedConfig permissions, String key) {
        Object value = permissions.get(key);
        return value instanceof List<?> list ? list.stream().map(String::valueOf).toList() : List.of();
    }

    /**
     * Migration method for configs generated with version 2.8.0 or less.
     * @since 2.9.0
     */
    private static void migrateSlashCommandPermissionsToRolesOnly(CommentedConfig guildConfig, String guildId) {
        CommentedConfig permissions = guildConfig.get("slashCommandPermissions");
        if (!hasLegacyKeys(permissions))
            return;

        Object modeRaw = permissions.get("mode");
        boolean topRole = modeRaw instanceof String mode && mode.trim().equalsIgnoreCase("top-role");
        List<String> legacyAllow = readLegacyList(permissions, "allow");
        List<String> legacyDeny = readLegacyList(permissions, "deny");

        for (String key : LEGACY_SLASH_PERMISSION_KEYS)
            if (isLegacyValue(permissions, key))
                permissions.remove(key);

        boolean hasEveryoneSection = permissions.entrySet().stream()
                .anyMatch(e -> e.getValue() instanceof CommentedConfig && isEveryoneKey(e.getKey(), guildId));

        if (!legacyAllow.isEmpty() || !legacyDeny.isEmpty()) {
            if (hasEveryoneSection) {
                LOGGER.warn("[guildId: {}] Legacy \"slashCommandPermissions.allow/deny\" were removed without migration: they were never applied because an @everyone section already exists", guildId);
            } else {
                CommentedConfig everyone = permissions.createSubConfig();
                everyone.set("allow", legacyAllow);
                everyone.set("deny", legacyDeny);
                permissions.set(SLASH_COMMAND_PERMISSIONS_EVERYONE_KEY, everyone);

                LOGGER.info("[guildId: {}] Migrating \"slashCommandPermissions.allow/deny\" → \"slashCommandPermissions.{}\"", guildId, SLASH_COMMAND_PERMISSIONS_EVERYONE_KEY);
                if (!topRole)
                    LOGGER.warn("[guildId: {}] These permissions now apply to ALL members, including those who have other configured roles. Please review the \"{}\" section", guildId, SLASH_COMMAND_PERMISSIONS_EVERYONE_KEY);
            }
        }

        if (topRole) {
            List<String> patchedRoles = new ArrayList<>();

            for (CommentedConfig.Entry entry : permissions.entrySet()) {
                if (!(entry.getValue() instanceof CommentedConfig roleConfig))
                    continue;
                if (isEveryoneKey(entry.getKey(), guildId))
                    continue;

                List<String> allow = roleConfig.getOrElse("allow", SLASH_COMMAND_PERMISSIONS_EXAMPLE_ROLE_ALLOW_DEFAULT);
                List<String> deny = roleConfig.getOrElse("deny", SLASH_COMMAND_PERMISSIONS_EXAMPLE_ROLE_DENY_DEFAULT);
                if (allow.contains("*") || deny.contains("*"))
                    continue;

                List<String> newDeny = new ArrayList<>(deny);
                newDeny.add("*");
                roleConfig.set("deny", newDeny);

                patchedRoles.add(entry.getKey());
            }

            LOGGER.info("[guildId: {}] Option \"slashCommandPermissions.mode\" was removed (permissions are always merged by role priority). To keep the former \"top-role\" behaviour, \"*\" was added to deny of roles: {}", guildId, patchedRoles);
        }

        LOGGER.info("[guildId: {}] Legacy keys \"slashCommandPermissions.mode/allow/deny\" were removed. Permissions are now defined only in role sections", guildId);
    }

    /**
     * Migration method for configs generated with version 2.7.0 or less
     * @since 2.8.0
     */
    private static void migrateSlashCommandsConfig(CommentedConfig guildConfig, String guildId){
        CommentedConfig slashCommands = guildConfig.getOrElse("slashCommands", CommentedConfig.inMemory());

        if (slashCommands.contains("enableSlashCommands")) {
            boolean oldValue = slashCommands.get("enableSlashCommands");
            LOGGER.info("[guildId: {}] Migrating \"slashCommands.enableSlashCommands\" → \"enableSlashCommands\"", guildId);
            guildConfig.set("enableSlashCommands", oldValue);
            guildConfig.setComment("enableSlashCommands", ENABLE_SLASH_COMMANDS_COMMENT);
        }

        if (slashCommands.contains("slashCommandAllowedRoles")) {
            List<String> oldRoles = slashCommands.getOrElse("slashCommandAllowedRoles", List.of());
            if (!oldRoles.isEmpty()) {
                LOGGER.info("[guildId: {}] Migrating \"slashCommandAllowedRoles\" → \"slashCommandPermissions\"", guildId);
                CommentedConfig slashCommandPermissions = guildConfig.get("slashCommandPermissions");

                for (String role : oldRoles) {
                    CommentedConfig roleConfig = slashCommandPermissions.createSubConfig();
                    roleConfig.set("allow", List.of("*"));
                    slashCommandPermissions.set(role, roleConfig);
                }
            }
        }

        guildConfig.remove("slashCommands");
    }

    /**
     * Migration method for configs generated with version 2.7.0 or less
     * @since 2.8.0
     */
    private static void migrateScreenshotChannelId(CommentedConfig guildConfig, String guildId){
        CommentedConfig channelOverrides = guildConfig.getOrElse("channelOverrides", CommentedConfig.inMemory());

        if (channelOverrides.contains("screenshotsChannelId")){
            String oldValue = channelOverrides.get("screenshotsChannelId");
            channelOverrides.set(ChannelCategory.IMAGES.getConfigName(), oldValue);
            channelOverrides.remove("screenshotsChannelId");
            LOGGER.info("[guildId: {}] Migrating \"channelOverrides.screenshotsChannelId\" → \"channelOverrides.imagesChannelId\"", guildId);
        }
    }
}

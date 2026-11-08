package com.denisnumb.discord_chat_mod.linking.features;

import com.denisnumb.discord_chat_mod.compat.permissions.IPermissionsCompat;
import com.denisnumb.discord_chat_mod.compat.permissions.PermissionsCompatProvider;
import com.denisnumb.discord_chat_mod.config.configs.AccountLinkingConfig;
import com.denisnumb.discord_chat_mod.linking.AccountLinkListener;
import com.denisnumb.discord_chat_mod.linking.AccountLinkService;
import com.denisnumb.discord_chat_mod.linking.AccountLinking;
import com.denisnumb.discord_chat_mod.linking.discord.LinkingDiscordUtils;
import com.denisnumb.discord_chat_mod.discord.model.DiscordGuildContext;
import com.denisnumb.discord_chat_mod.linking.model.AccountLink;
import com.denisnumb.discord_chat_mod.linking.model.RoleSyncDirection;
import com.denisnumb.discord_chat_mod.linking.model.UnlinkReason;
import com.denisnumb.discord_chat_mod.locale.MinecraftLocaleProvider;
import com.denisnumb.discord_chat_mod.utils.NameComponentUtils;
import com.mojang.logging.LogUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import static com.denisnumb.discord_chat_mod.utils.JavaUtils.containsIgnoreCase;
import static com.denisnumb.discord_chat_mod.utils.MinecraftUtils.logErrorToServer;

/**
 * Synchronizes Discord roles with player groups according to [guilds.accountLinking.roleSyncMapping] of each guild
 * as a full mirror: every linked account of a user has the same mapped groups, and the user has the corresponding roles in every guild.
 * <ul>
 *     <li>While the server is running, a role or a group added/removed anywhere is added/removed everywhere else:
 *     on all linked accounts and in all guilds.</li>
 *     <li>Full synchronization (Discord connection, config reload, player join, joining/leaving a guild) takes the set
 *     from the primary side ({@code roleSyncDirection}): roles held in at least one guild or groups held by at least
 *     one account, and applies it everywhere.</li>
 *     <li>A new link merges both sides: nothing is removed.</li>
 * </ul>
 */
public final class RoleSyncFeature implements AccountLinkListener {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static AutoCloseable groupChangesSubscription;
    private static IPermissionsCompat subscribedProvider;

    private static boolean hasMapping() {
        return AccountLinking.isEnabled() && LinkingDiscordUtils.anyGuildConfig(config -> !config.roleSyncMapping().isEmpty());
    }

    private static boolean isActive() {
        return hasMapping() && permissions().supportsGroups();
    }

    private static RoleSyncDirection direction() {
        return AccountLinkingConfig.ROLE_SYNC_DIRECTION.get();
    }

    private static IPermissionsCompat permissions() {
        return PermissionsCompatProvider.get();
    }

    private record MappedRole(Role role, String group) {
    }

    // =================================================================================================================
    //                                                LINK EVENTS
    // =================================================================================================================

    @Override
    public void onLinked(AccountLink link) {
        if (isActive())
            syncUser(link.discordId(), true);
    }

    @Override
    public void onUnlinked(AccountLink link, UnlinkReason reason) {
        if (!isActive())
            return;

        switch (direction()) {
            case DISCORD_TO_MINECRAFT -> permissions().getGroups(link.uuid(), link.name())
                    .thenAccept(groups -> applyGroups(link, List.of(), groups, RoleSyncFeature::isConfiguredGroup))
                    .exceptionally(error -> logError(link, error));
            case MINECRAFT_TO_DISCORD -> syncUser(link.discordId(), false);
        }
    }

    // =================================================================================================================
    //                                               RECONCILIATION
    // =================================================================================================================

    public static void reconcileAll() {
        if (!hasMapping())
            return;

        if (!permissions().supportsGroups()) {
            LinkingDiscordUtils.errorOnce("no_permissions_provider", MinecraftLocaleProvider.Linking.Error.noPermissionsProvider());
            return;
        }

        for (DiscordGuildContext context : LinkingDiscordUtils.getGuilds()) {
            for (MappedRole mapped : getMapping(context)) {
                if (mapped.role().isManaged())
                    LinkingDiscordUtils.warnOnce("managed_role:" + mapped.role().getId(),
                            MinecraftLocaleProvider.Linking.Warn.managedRole(mapped.role().getName(), context.guild.getName()));
                checkGroupExists(mapped.group());
            }
        }

        AccountLinkService.getAllDiscordIds().forEach(discordId -> syncUser(discordId, false));
    }

    /**
     * Synchronization of the user of the link (on player join)
     */
    public static void reconcile(AccountLink link) {
        if (isActive())
            syncUser(link.discordId(), false);
    }

    /**
     * The user joined or left one of the guilds, but the accounts stay linked
     */
    public static void onMembershipChanged(String discordId) {
        if (isActive())
            syncUser(discordId, false);
    }

    /**
     * Gives every linked account of the user the same mapped groups and the user the same mapped roles in every guild.
     * The set is taken from the primary side or from both sides when merging.
     */
    private static void syncUser(String discordId, boolean merge) {
        Set<String> syncedGroups = getSyncedGroups();
        if (syncedGroups.isEmpty())
            return;

        List<AccountLink> links = AccountLinkService.getByDiscordId(discordId);
        getGroups(links)
                .thenAccept(groupsByLink -> {
                    Set<String> fromRoles = getGroupsFromRoles(discordId);
                    Set<String> fromAccounts = new LinkedHashSet<>();
                    for (String group : union(groupsByLink.values()))
                        if (containsIgnoreCase(syncedGroups, group))
                            fromAccounts.add(group);

                    Set<String> groups = new LinkedHashSet<>();
                    if (merge || direction() == RoleSyncDirection.DISCORD_TO_MINECRAFT)
                        groups.addAll(fromRoles);
                    if (merge || direction() == RoleSyncDirection.MINECRAFT_TO_DISCORD)
                        fromAccounts.stream().filter(group -> !containsIgnoreCase(groups, group)).forEach(groups::add);

                    groupsByLink.forEach((link, current) ->
                            applyGroups(link, groups, current, group -> containsIgnoreCase(syncedGroups, group)));

                    for (DiscordGuildContext context : LinkingDiscordUtils.getGuilds()) {
                        Member member = context.getMember(discordId);
                        if (member != null)
                            applyRoles(context, member, groups);
                    }
                })
                .exceptionally(error -> logError(links.isEmpty() ? null : links.getFirst(), error));
    }

    // =================================================================================================================
    //                                               LIVE CHANGES
    // =================================================================================================================

    /**
     * Roles of a member were changed in a guild
     */
    public static void onMemberRolesChanged(DiscordGuildContext context, Member member, Collection<Role> addedRoles, Collection<Role> removedRoles) {
        if (!isActive())
            return;

        Guild guild = context.guild;
        Set<String> addedGroups = getMappedGroups(context, addedRoles.stream()
                .filter(role -> !EchoGuard.consume(EchoGuard.roleKey(guild, member.getId(), role, true)))
                .toList());
        Set<String> removedGroups = getMappedGroups(context, removedRoles.stream()
                .filter(role -> !EchoGuard.consume(EchoGuard.roleKey(guild, member.getId(), role, false)))
                .toList());

        propagate(member.getId(), addedGroups, removedGroups, guild, null);
    }

    /**
     * Player groups were changed (tracked by LuckPerms and FTB Ranks)
     */
    private static void onGroupsChanged(UUID uuid, Set<String> addedGroups, Set<String> removedGroups) {
        if (!isActive())
            return;

        Set<String> syncedGroups = getSyncedGroups();
        Set<String> added = new HashSet<>();
        for (String group : addedGroups)
            if (containsIgnoreCase(syncedGroups, group) && !EchoGuard.consume(EchoGuard.groupKey(uuid, group, true)))
                added.add(group);
        Set<String> removed = new HashSet<>();
        for (String group : removedGroups)
            if (containsIgnoreCase(syncedGroups, group) && !EchoGuard.consume(EchoGuard.groupKey(uuid, group, false)))
                removed.add(group);

        AccountLinkService.getByUuid(uuid).ifPresent(link -> propagate(link.discordId(), added, removed, null, uuid));
    }

    /**
     * Applies a change made on one account or in one guild to all other accounts and guilds of the user.
     * The source already has the change and is skipped
     */
    private static void propagate(
            String discordId,
            Set<String> added,
            Set<String> removed,
            @Nullable Guild sourceGuild,
            @Nullable UUID sourceAccount
    ) {
        if (added.isEmpty() && removed.isEmpty())
            return;

        for (AccountLink link : AccountLinkService.getByDiscordId(discordId)) {
            boolean isSource = link.uuid().equals(sourceAccount);
            Predicate<String> affected = group -> !isSource || EchoGuard.pendingState(EchoGuard.groupTarget(link.uuid(), group)).isPresent();
            if (isSource && added.stream().noneMatch(affected) && removed.stream().noneMatch(affected))
                continue;

            permissions().getGroups(link.uuid(), link.name())
                    .thenAccept(groups -> {
                        for (String group : added)
                            if (affected.test(group) && !hasGroup(link, groups, group) && checkGroupExists(group))
                                addGroup(link, group);
                        for (String group : removed)
                            if (affected.test(group) && hasGroup(link, groups, group))
                                removeGroup(link, group);
                    })
                    .exceptionally(error -> logError(link, error));
        }

        for (DiscordGuildContext context : LinkingDiscordUtils.getGuilds()) {
            Guild guild = context.guild;
            Member member = context.getMember(discordId);
            if (member == null)
                continue;

            boolean isSource = guild.equals(sourceGuild);
            for (MappedRole mapped : getMapping(context)) {
                if (isSource && EchoGuard.pendingState(EchoGuard.roleTarget(guild, member.getId(), mapped.role())).isEmpty())
                    continue;
                if (containsIgnoreCase(added, mapped.group()))
                    addRole(guild, member, mapped.role());
                else if (containsIgnoreCase(removed, mapped.group()))
                    removeRole(guild, member, mapped.role());
            }
        }
    }

    private static void applyGroups(AccountLink link, Collection<String> desiredGroups, Set<String> currentGroups, Predicate<String> canRemove) {
        for (String group : desiredGroups)
            if (!hasGroup(link, currentGroups, group) && checkGroupExists(group))
                addGroup(link, group);

        for (String group : currentGroups)
            if (canRemove.test(group) && !containsIgnoreCase(desiredGroups, group) && hasGroup(link, currentGroups, group))
                removeGroup(link, group);
    }

    /**
     * The account has the group, taking into account changes requested by the synchronization that are not applied yet
     */
    private static boolean hasGroup(AccountLink link, Collection<String> currentGroups, String group) {
        return EchoGuard.pendingState(EchoGuard.groupTarget(link.uuid(), group))
                .orElseGet(() -> containsIgnoreCase(currentGroups, group));
    }

    /**
     * The member has the role, taking into account changes requested by the synchronization that are not applied yet
     */
    private static boolean hasRole(Guild guild, Member member, Role role) {
        return EchoGuard.pendingState(EchoGuard.roleTarget(guild, member.getId(), role))
                .orElseGet(() -> member.getRoles().contains(role));
    }

    /**
     * Makes the mapped roles of the member in the guild match the groups
     */
    private static void applyRoles(DiscordGuildContext context, Member member, Collection<String> groups) {
        for (MappedRole mapped : getMapping(context)) {
            if (containsIgnoreCase(groups, mapped.group()))
                addRole(context.guild, member, mapped.role());
            else
                removeRole(context.guild, member, mapped.role());
        }
    }

    // =================================================================================================================
    //                                                  ROLES
    // =================================================================================================================

    /**
     * Resolved roleSyncMapping of the guild, in config order. The mapping is one-to-one within a guild:
     * an entry whose role or group is already mapped by a previous entry is reported and ignored
     */
    private static List<MappedRole> getMapping(DiscordGuildContext context) {
        List<MappedRole> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : context.accountLinking.roleSyncMapping().entrySet()) {
            Optional<Role> role = LinkingDiscordUtils.resolveConfiguredRole(context, entry.getKey());
            if (role.isEmpty())
                continue;

            String group = entry.getValue();
            boolean duplicate = result.stream().anyMatch(mapped -> mapped.role().equals(role.get()) || mapped.group().equalsIgnoreCase(group));
            if (duplicate) {
                LinkingDiscordUtils.errorOnce("duplicate_mapping:" + context.guild.getId() + ":" + entry.getKey(),
                        MinecraftLocaleProvider.Linking.Error.duplicateMapping(entry.getKey(), group, context.guild.getName()));
                continue;
            }
            result.add(new MappedRole(role.get(), group));
        }
        return result;
    }

    private static void addRole(Guild guild, Member member, Role role) {
        if (role.isManaged() || hasRole(guild, member, role) || !LinkingDiscordUtils.canManageRole(guild, role))
            return;

        String echoKey = EchoGuard.expect(EchoGuard.roleKey(guild, member.getId(), role, true));
        guild.addRoleToMember(member, role).queue(null, error -> {
            EchoGuard.cancel(echoKey);
            logErrorToServer(MinecraftLocaleProvider.Linking.Error.addRole(role.getName(), NameComponentUtils.discordMember(member), error.getMessage()));
        });
    }

    private static void removeRole(Guild guild, Member member, Role role) {
        if (role.isManaged() || !hasRole(guild, member, role) || !LinkingDiscordUtils.canManageRole(guild, role))
            return;

        String echoKey = EchoGuard.expect(EchoGuard.roleKey(guild, member.getId(), role, false));
        guild.removeRoleFromMember(member, role).queue(null, error -> {
            EchoGuard.cancel(echoKey);
            logErrorToServer(MinecraftLocaleProvider.Linking.Error.removeRole(role.getName(), NameComponentUtils.discordMember(member), error.getMessage()));
        });
    }

    // =================================================================================================================
    //                                                  GROUPS
    // =================================================================================================================

    /**
     * Groups mapped from the user's roles in all guilds from the config
     */
    private static Set<String> getGroupsFromRoles(String discordId) {
        Set<String> result = new LinkedHashSet<>();
        for (DiscordGuildContext context : LinkingDiscordUtils.getGuilds()) {
            Member member = context.getMember(discordId);
            if (member != null)
                result.addAll(getMappedGroups(context, member.getRoles()));
        }
        return result;
    }

    private static Set<String> getMappedGroups(DiscordGuildContext context, Collection<Role> roles) {
        Set<String> result = new LinkedHashSet<>();
        for (MappedRole mapped : getMapping(context))
            if (roles.contains(mapped.role()))
                result.add(mapped.group());
        return result;
    }

    /**
     * Groups of the valid roleSyncMapping entries of the connected guilds: only they are synchronized
     */
    private static Set<String> getSyncedGroups() {
        Set<String> result = new LinkedHashSet<>();
        for (DiscordGuildContext context : LinkingDiscordUtils.getGuilds())
            for (MappedRole mapped : getMapping(context))
                result.add(mapped.group());
        return result;
    }

    /**
     * The group is listed in roleSyncMapping of at least one guild (checks the config only, used on unlinking)
     */
    private static boolean isConfiguredGroup(String group) {
        return LinkingDiscordUtils.anyGuildConfig(config -> containsIgnoreCase(config.roleSyncMapping().values(), group));
    }

    /**
     * Reports a group from roleSyncMapping that does not exist (once)
     */
    private static boolean checkGroupExists(String group) {
        IPermissionsCompat permissions = permissions();
        if (permissions.groupExists(group))
            return true;

        LinkingDiscordUtils.errorOnce("group_not_found:" + permissions.id() + ":" + group,
                MinecraftLocaleProvider.Linking.Error.groupNotFound(group, permissions.id()));

        return false;
    }

    private static CompletableFuture<Map<AccountLink, Set<String>>> getGroups(List<AccountLink> links) {
        Map<AccountLink, CompletableFuture<Set<String>>> futures = new LinkedHashMap<>();
        for (AccountLink link : links)
            futures.put(link, permissions().getGroups(link.uuid(), link.name()));

        return CompletableFuture.allOf(futures.values().toArray(CompletableFuture[]::new))
                .thenApply(ignored -> {
                    Map<AccountLink, Set<String>> result = new LinkedHashMap<>();
                    futures.forEach((link, future) -> result.put(link, future.join()));
                    return result;
                });
    }

    private static void addGroup(AccountLink link, String group) {
        String echoKey = EchoGuard.expect(EchoGuard.groupKey(link.uuid(), group, true));
        handleGroupResult(link, echoKey, permissions().addGroup(link.uuid(), link.name(), group));
    }

    private static void removeGroup(AccountLink link, String group) {
        String echoKey = EchoGuard.expect(EchoGuard.groupKey(link.uuid(), group, false));
        handleGroupResult(link, echoKey, permissions().removeGroup(link.uuid(), link.name(), group));
    }

    private static void handleGroupResult(AccountLink link, @Nullable String echoKey, CompletableFuture<Boolean> result) {
        result.whenComplete((changed, error) -> {
            if (error != null || !Boolean.TRUE.equals(changed))
                EchoGuard.cancel(echoKey);
            if (error != null)
                logError(link, error);
        });
    }

    // =================================================================================================================
    //                                              SUBSCRIPTION
    // =================================================================================================================

    /**
     * Subscribes to group changes of the current permissions provider
     */
    public static synchronized void ensureSubscribed() {
        IPermissionsCompat provider = permissions();
        if (groupChangesSubscription != null && provider == subscribedProvider)
            return;

        unsubscribe();
        provider.subscribeGroupChanges(RoleSyncFeature::onGroupsChanged).ifPresent(subscription -> {
            groupChangesSubscription = subscription;
            subscribedProvider = provider;
        });
    }

    public static synchronized void unsubscribe() {
        if (groupChangesSubscription != null) {
            try {
                groupChangesSubscription.close();
            } catch (Exception ignored) {}
        }
        groupChangesSubscription = null;
        subscribedProvider = null;
    }

    // =================================================================================================================
    //                                                ECHO GUARD
    // =================================================================================================================

    /**
     * Changes made by the synchronization produce events on the side that was changed.
     * Such events are recognized by the expected keys and are not synchronized back,
     * otherwise a delayed event could undo a newer change (e.g. a role added and quickly removed would come back).
     * <p>
     * It also keeps the state a role/group will have after the requested changes: the Discord member cache
     * is updated only when the event arrives, so decisions made before that must not rely on it.
     */
    static final class EchoGuard {
        private static final long TTL_MILLIS = 15_000;
        private static final int CLEANUP_THRESHOLD = 256;

        /**
         * Expected event key → number of expected events
         */
        private static final Map<String, Expected> EXPECTED = new ConcurrentHashMap<>();
        /**
         * Role/group target → state after the requested changes
         */
        private static final Map<String, Pending> PENDING = new ConcurrentHashMap<>();

        private record Expected(int count, long expiresAt) {}

        private record Pending(boolean present, int count, long expiresAt) {}

        private EchoGuard() {}

        static String groupTarget(UUID uuid, String group) {
            return "group|" + uuid + "|" + group.toLowerCase(Locale.ROOT);
        }

        static String roleTarget(Guild guild, String memberId, Role role) {
            return "role|" + guild.getId() + "|" + memberId + "|" + role.getId();
        }

        static String groupKey(UUID uuid, String group, boolean added) {
            return groupTarget(uuid, group) + (added ? "|+" : "|-");
        }

        static String roleKey(Guild guild, String memberId, Role role, boolean added) {
            return roleTarget(guild, memberId, role) + (added ? "|+" : "|-");
        }

        /**
         * Registers a requested change and its expected event, returns the key to cancel it if the change fails
         */
        static String expect(String key) {
            long now = System.currentTimeMillis();
            if (EXPECTED.size() > CLEANUP_THRESHOLD) {
                EXPECTED.values().removeIf(expected -> expected.expiresAt() < now);
                PENDING.values().removeIf(pending -> pending.expiresAt() < now);
            }

            EXPECTED.compute(key, (k, old) -> new Expected(old == null || old.expiresAt() < now ? 1 : old.count() + 1, now + TTL_MILLIS));
            PENDING.compute(target(key), (k, old) -> new Pending(key.endsWith("+"),
                    old == null || old.expiresAt() < now ? 1 : old.count() + 1, now + TTL_MILLIS));

            return key;
        }

        /**
         * The change failed or did nothing: no event will come
         */
        static void cancel(@Nullable String key) {
            if (key != null && take(key))
                release(key);
        }

        /**
         * @return true if the event was caused by the synchronization itself
         */
        static boolean consume(String key) {
            if (!take(key))
                return false;

            release(key);
            return true;
        }

        /**
         * State of the role/group after the requested changes, if any are in progress
         */
        static Optional<Boolean> pendingState(String target) {
            Pending pending = PENDING.get(target);
            return pending == null || pending.expiresAt() < System.currentTimeMillis()
                    ? Optional.empty()
                    : Optional.of(pending.present());
        }

        private static boolean take(String key) {
            long now = System.currentTimeMillis();
            boolean[] taken = {false};
            EXPECTED.computeIfPresent(key, (k, expected) -> {
                if (expected.expiresAt() < now)
                    return null;
                taken[0] = true;

                return expected.count() > 1 ? new Expected(expected.count() - 1, expected.expiresAt()) : null;
            });

            return taken[0];
        }

        private static void release(String key) {
            PENDING.computeIfPresent(target(key), (k, pending) ->
                    pending.count() > 1 ? new Pending(pending.present(), pending.count() - 1, pending.expiresAt()) : null);
        }

        private static String target(String key) {
            return key.substring(0, key.length() - 2);
        }
    }

    // =================================================================================================================
    //                                                   UTILS
    // =================================================================================================================

    private static Void logError(@Nullable AccountLink link, Throwable error) {
        Component target = link == null ? Component.literal("?") : NameComponentUtils.player(link.uuid(), link.name());
        logErrorToServer(MinecraftLocaleProvider.Linking.Error.roleSync(target, String.valueOf(error.getMessage())));
        LOGGER.error("", error);
        return null;
    }

    private static Set<String> union(Collection<Set<String>> sets) {
        Set<String> result = new LinkedHashSet<>();
        sets.forEach(result::addAll);
        return result;
    }
}

package com.denisnumb.discord_chat_mod.compat.permissions;

import com.denisnumb.discord_chat_mod.utils.LegacyTextUtils;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.node.NodeMutateEvent;
import net.luckperms.api.model.data.DataMutateResult;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;


final class LuckPermsCompat implements IPermissionsCompat {
    @Override
    public String id() {
        return "luckperms";
    }

    private static Optional<LuckPerms> api() {
        try {
            return Optional.of(LuckPermsProvider.get());
        } catch (IllegalStateException notLoadedYet) {
            return Optional.empty();
        }
    }

    private static Optional<CachedMetaData> getMetaData(Player player) {
        return api()
                .map(lp -> lp.getUserManager().getUser(player.getUUID()))
                .map(user -> user.getCachedData().getMetaData());
    }

    private static Optional<Component> toComponent(Function<CachedMetaData, String> getter, Player player) {
        return getMetaData(player)
                .map(getter)
                .filter(value -> !value.isEmpty())
                .map(LegacyTextUtils::toComponent);
    }

    @Override
    public Optional<Component> getPrefix(Player player) {
        return toComponent(CachedMetaData::getPrefix, player);
    }

    @Override
    public Optional<Component> getSuffix(Player player) {
        return toComponent(CachedMetaData::getSuffix, player);
    }

    @Override
    public boolean supportsGroups() {
        return true;
    }

    @Override
    public boolean groupExists(String group) {
        return api().map(lp -> lp.getGroupManager().getGroup(group) != null).orElse(false);
    }

    @Override
    public CompletableFuture<Set<String>> getGroups(UUID uuid, String playerName) {
        Optional<LuckPerms> lp = api();
        if (lp.isEmpty())
            return CompletableFuture.completedFuture(Set.of());

        User loadedUser = lp.get().getUserManager().getUser(uuid);
        if (loadedUser != null)
            return CompletableFuture.completedFuture(getGroupNames(loadedUser));

        return lp.get().getUserManager().loadUser(uuid, playerName)
                .thenApply(LuckPermsCompat::getGroupNames);
    }

    private static Set<String> getGroupNames(User user) {
        return user.getNodes(NodeType.INHERITANCE).stream()
                .map(InheritanceNode::getGroupName)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public CompletableFuture<Boolean> addGroup(UUID uuid, String playerName, String group) {
        return modifyGroup(uuid, group, true);
    }

    @Override
    public CompletableFuture<Boolean> removeGroup(UUID uuid, String playerName, String group) {
        return modifyGroup(uuid, group, false);
    }

    private CompletableFuture<Boolean> modifyGroup(UUID uuid, String group, boolean add) {
        Optional<LuckPerms> lp = api();
        if (lp.isEmpty())
            return CompletableFuture.completedFuture(false);

        if (add && !groupExists(group))
            return CompletableFuture.completedFuture(false);

        InheritanceNode node = InheritanceNode.builder(group).build();
        UserManager userManager = lp.get().getUserManager();

        User loadedUser = userManager.getUser(uuid);
        if (loadedUser != null) {
            boolean changed = apply(loadedUser, node, add);
            return changed
                    ? userManager.saveUser(loadedUser).thenApply(ignored -> true)
                    : CompletableFuture.completedFuture(false);
        }

        AtomicBoolean changed = new AtomicBoolean();
        return userManager
                .modifyUser(uuid, user -> changed.set(apply(user, node, add)))
                .thenApply(ignored -> changed.get());
    }

    private static boolean apply(User user, InheritanceNode node, boolean add) {
        DataMutateResult result = add ? user.data().add(node) : user.data().remove(node);
        return result.wasSuccessful();
    }

    @Override
    public Optional<AutoCloseable> subscribeGroupChanges(GroupChangeListener listener) {
        Optional<LuckPerms> lp = api();
        if (lp.isEmpty())
            return Optional.empty();

        EventSubscription<NodeMutateEvent> subscription = lp.get().getEventBus().subscribe(NodeMutateEvent.class, event -> {
            if (!event.isUser() || !(event.getTarget() instanceof User user))
                return;

            Set<String> before = getGroupNames(event.getDataBefore());
            Set<String> after = getGroupNames(event.getDataAfter());

            Set<String> added = new HashSet<>(after);
            added.removeAll(before);
            Set<String> removed = new HashSet<>(before);
            removed.removeAll(after);

            if (!added.isEmpty() || !removed.isEmpty())
                listener.onGroupsChanged(user.getUniqueId(), added, removed);
        });

        return Optional.of(subscription::close);
    }

    private static Set<String> getGroupNames(Collection<Node> nodes) {
        return nodes.stream()
                .filter(NodeType.INHERITANCE::matches)
                .map(NodeType.INHERITANCE::cast)
                .map(InheritanceNode::getGroupName)
                .collect(Collectors.toSet());
    }
}

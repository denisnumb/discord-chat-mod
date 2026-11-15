package com.denisnumb.discord_chat_mod.compat.vanish;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class VanishCompatProvider {
    private VanishCompatProvider() {}

    private static final Logger LOGGER = LogUtils.getLogger();
    private static volatile IVanishCompat instance = IVanishCompat.NOOP;

    public static void set(IVanishCompat compat) {
        instance = compat == null ? IVanishCompat.NOOP : compat;
        LOGGER.info("Vanish provider: {}", instance.id());
    }

    public static IVanishCompat get() {
        return instance;
    }

    public static boolean isVanished(@Nullable Entity entity) {
        if (!(entity instanceof ServerPlayer player))
            return false;

        IVanishCompat compat = instance;
        if (compat == IVanishCompat.NOOP)
            return false;

        try {
            return compat.isVanished(player);
        } catch (LinkageError e) {
            disable(compat, e);
        } catch (RuntimeException e) {
            LOGGER.debug("Vanish provider '{}' failed to check {}", compat.id(), player.getGameProfile().name(), e);
        }
        return false;
    }

    /**
     * Whether {@code observer} may see {@code actor} (e.g. receive a message authored by them).
     */
    public static boolean canSeePlayer(ServerPlayer actor, ServerPlayer observer) {
        IVanishCompat compat = instance;
        if (compat == IVanishCompat.NOOP)
            return true;

        try {
            return compat.canSeePlayer(actor, observer);
        } catch (LinkageError e) {
            disable(compat, e);
        } catch (RuntimeException e) {
            LOGGER.debug("Vanish provider '{}' failed to check visibility", compat.id(), e);
        }
        return true;
    }

    public static boolean announcesFakeJoinLeave() {
        IVanishCompat compat = instance;
        try {
            return compat.announcesFakeJoinLeave();
        } catch (LinkageError e) {
            disable(compat, e);
        } catch (RuntimeException ignored) {
        }
        return true;
    }

    public static Optional<Component> getPrivateMessageDenial(ServerPlayer sender) {
        IVanishCompat compat = instance;
        if (compat == IVanishCompat.NOOP)
            return Optional.empty();

        try {
            return compat.getPrivateMessageDenial(sender);
        } catch (LinkageError e) {
            disable(compat, e);
        } catch (RuntimeException ignored) {
        }
        return Optional.empty();
    }

    public static List<ServerPlayer> getVisiblePlayers(@Nullable MinecraftServer server) {
        if (server == null || server.getPlayerList() == null)
            return List.of();

        return server.getPlayerList().getPlayers().stream()
                .filter(player -> !isVanished(player))
                .toList();
    }

    public static int getVisiblePlayerCount(@Nullable MinecraftServer server) {
        if (server == null || server.getPlayerList() == null)
            return 0;

        if (instance == IVanishCompat.NOOP)
            return server.getPlayerCount();

        return getVisiblePlayers(server).size();
    }

    public static String[] getVisiblePlayerNames(@Nullable MinecraftServer server) {
        if (server == null || server.getPlayerList() == null)
            return new String[0];

        if (instance == IVanishCompat.NOOP)
            return server.getPlayerNames();

        return getVisiblePlayers(server).stream()
                .map(player -> player.getGameProfile().name())
                .toArray(String[]::new);
    }

    /**
     * Lower-cased names of currently vanished players.
     */
    public static Set<String> getVanishedPlayerNames(@Nullable MinecraftServer server) {
        Set<String> result = new HashSet<>();
        if (server == null || server.getPlayerList() == null || instance == IVanishCompat.NOOP)
            return result;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (isVanished(player))
                result.add(player.getGameProfile().name().toLowerCase(Locale.ROOT));
        }
        return result;
    }

    private static synchronized void disable(IVanishCompat compat, Throwable error) {
        if (instance != compat)
            return;

        instance = IVanishCompat.NOOP;
        LOGGER.error("Vanish integration '{}' is not compatible with the installed version of the mod and has been disabled", compat.id(), error);
    }
}

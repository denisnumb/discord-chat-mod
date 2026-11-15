package com.denisnumb.discord_chat_mod.neoforge.compat.vanish;

import com.denisnumb.discord_chat_mod.MinecraftEvents;
import com.denisnumb.discord_chat_mod.compat.vanish.IVanishCompat;
import com.denisnumb.discord_chat_mod.compat.vanish.VanishCompatProvider;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;
import redstonedubstep.mods.vanishmod.VanishingHandler;
import redstonedubstep.mods.vanishmod.api.PlayerVanishEvent;

import java.lang.reflect.Field;
import java.util.Set;

final class VanishmodCompat implements IVanishCompat {
    static final String MOD_ID = "vmod";

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PERSISTED_VANISHED_KEY = "Vanished";
    private static final String QUEUE_FIELD = "vanishingQueue";

    @Nullable
    private static Set<?> vanishingQueue;

    static void register() {
        vanishingQueue = lookupVanishingQueue();
        NeoForge.EVENT_BUS.addListener(PlayerVanishEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player)
                MinecraftEvents.handleVanishStatusChange(player, event.isVanished());
        });
        VanishCompatProvider.set(new VanishmodCompat());
    }

    @Override
    public String id() {
        return MOD_ID;
    }

    @Override
    public boolean isVanished(ServerPlayer player) {
        return VanishUtil.isVanished(player) || isMarkedVanished(player) || isQueued(player);
    }

    @Override
    public boolean canSeePlayer(ServerPlayer actor, ServerPlayer observer) {
        return !VanishUtil.isVanished(actor, observer);
    }

    @Override
    public boolean announcesFakeJoinLeave() {
        try {
            return VanishConfig.CONFIG.sendFakeJoinLeaveMessages.get();
        } catch (LinkageError | RuntimeException e) {
            return true;
        }
    }

    private static boolean isMarkedVanished(ServerPlayer player) {
        return player.getPersistentData()
                .getCompoundOrEmpty(Player.PERSISTED_NBT_TAG)
                .getBooleanOr(PERSISTED_VANISHED_KEY, false);
    }

    private static boolean isQueued(ServerPlayer player) {
        Set<?> queue = vanishingQueue;
        if (queue == null || queue.isEmpty() || !queue.contains(player.getGameProfile().name()))
            return false;

        var server = player.level().getServer();
        return server == null || !server.getPlayerList().getPlayers().contains(player);
    }

    @Nullable
    private static Set<?> lookupVanishingQueue() {
        try {
            Field field = VanishingHandler.class.getDeclaredField(QUEUE_FIELD);
            field.setAccessible(true);
            return field.get(null) instanceof Set<?> set ? set : null;
        } catch (Throwable e) {
            LOGGER.debug("Vanishmod vanishing queue is not accessible, queued players are detected after joining", e);
            return null;
        }
    }
}

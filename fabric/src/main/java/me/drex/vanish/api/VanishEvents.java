package me.drex.vanish.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;

/**
 * Compile-time stub for the Vanish mod Events API.
 * At runtime, the real Vanish mod provides this class.
 * This stub is excluded from the final JAR.
 */
public class VanishEvents {
    public static final Event<VanishEvent> VANISH_EVENT = EventFactory.createArrayBacked(VanishEvent.class, callbacks -> (player, vanish) -> {
        for (var callback : callbacks) {
            callback.onVanish(player, vanish);
        }
    });

    public interface VanishEvent {
        void onVanish(ServerPlayer player, boolean vanish);
    }
}

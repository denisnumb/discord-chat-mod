package me.drex.vanish.api;

import net.minecraft.world.entity.Entity;

/**
 * Compile-time stub for the Vanish mod API.
 * At runtime, the real Vanish mod provides this class.
 * This stub is excluded from the final JAR.
 */
public interface VanishAPI {
    static boolean isVanished(Entity entity) {
        throw new UnsupportedOperationException("Vanish mod not loaded");
    }
}

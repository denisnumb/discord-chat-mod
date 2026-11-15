package com.denisnumb.discord_chat_mod.neoforge.compat.vanish;

import com.denisnumb.discord_chat_mod.compat.vanish.IVanishCompat;
import com.denisnumb.discord_chat_mod.compat.vanish.VanishCompatProvider;
import com.mojang.logging.LogUtils;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

public final class NeoForgeVanishCompat {
    private NeoForgeVanishCompat() {}

    private static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        if (!ModList.get().isLoaded(VanishmodCompat.MOD_ID))
            return;

        try {
            VanishmodCompat.register();
        } catch (LinkageError e) {
            VanishCompatProvider.set(IVanishCompat.NOOP);
            LOGGER.error("Installed Vanishmod is not compatible with this version of the mod. Vanish integration is disabled", e);
        }
    }
}

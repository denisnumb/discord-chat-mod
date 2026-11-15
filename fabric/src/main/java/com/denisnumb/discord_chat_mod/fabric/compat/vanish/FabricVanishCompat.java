package com.denisnumb.discord_chat_mod.fabric.compat.vanish;

import com.denisnumb.discord_chat_mod.compat.vanish.IVanishCompat;
import com.denisnumb.discord_chat_mod.compat.vanish.VanishCompatProvider;
import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

public final class FabricVanishCompat {
    private FabricVanishCompat() {}

    private static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        if (!FabricLoader.getInstance().isModLoaded(MeliusVanishCompat.MOD_ID))
            return;

        try {
            MeliusVanishCompat.register();
        } catch (LinkageError e) {
            VanishCompatProvider.set(IVanishCompat.NOOP);
            LOGGER.error("Installed Melius Vanish is not compatible with this version of the mod. Vanish integration is disabled", e);
        }
    }
}

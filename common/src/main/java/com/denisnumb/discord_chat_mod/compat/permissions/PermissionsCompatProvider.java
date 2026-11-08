package com.denisnumb.discord_chat_mod.compat.permissions;

import com.denisnumb.discord_chat_mod.config.configs.CommonConfig;
import com.denisnumb.discord_chat_mod.locale.MinecraftLocaleProvider;
import com.denisnumb.discord_chat_mod.utils.MinecraftUtils;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.Locale;

public final class PermissionsCompatProvider {
    private PermissionsCompatProvider() {}

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String LUCKPERMS_API_CLASS = "net.luckperms.api.LuckPermsProvider";
    private static final String FTB_RANKS_API_CLASS = "dev.ftb.mods.ftbranks.api.FTBRanksAPI";

    private static volatile IPermissionsCompat instance;
    private static volatile String resolvedFor;

    public static IPermissionsCompat get() {
        String mode = CommonConfig.PERMISSIONS_PROVIDER.get();
        if (instance == null || !mode.equals(resolvedFor)) {
            synchronized (PermissionsCompatProvider.class) {
                if (instance == null || !mode.equals(resolvedFor)) {
                    instance = resolve(mode);
                    resolvedFor = mode;
                    LOGGER.info("Permissions provider: {}", instance.id());
                }
            }
        }
        return instance;
    }

    private static IPermissionsCompat resolve(String mode) {
        return switch (mode.toLowerCase(Locale.ROOT)) {
            case "none" -> IPermissionsCompat.NOOP;
            case "luckperms" -> isClassPresent(LUCKPERMS_API_CLASS)
                    ? new LuckPermsCompat()
                    : notInstalled(mode, "LuckPerms");
            case "ftbranks" -> isClassPresent(FTB_RANKS_API_CLASS)
                    ? new FtbRanksCompat()
                    : notInstalled(mode, "FTB Ranks");
            default -> {
                if (isClassPresent(LUCKPERMS_API_CLASS))
                    yield new LuckPermsCompat();
                if (isClassPresent(FTB_RANKS_API_CLASS))
                    yield new FtbRanksCompat();
                yield IPermissionsCompat.NOOP;
            }
        };
    }

    private static IPermissionsCompat notInstalled(String mode, String modName) {
        MinecraftUtils.logWarnToServer(MinecraftLocaleProvider.Permissions.Warn.providerNotInstalled(mode, modName));
        return IPermissionsCompat.NOOP;
    }

    private static boolean isClassPresent(String className) {
        try {
            Class.forName(className, false, PermissionsCompatProvider.class.getClassLoader());
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}

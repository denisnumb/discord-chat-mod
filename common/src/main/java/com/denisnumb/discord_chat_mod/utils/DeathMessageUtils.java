package com.denisnumb.discord_chat_mod.utils;

import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jetbrains.annotations.Nullable;


public final class DeathMessageUtils {
    private DeathMessageUtils() {}

    public record DeathMessageComponents(
            Component diedEntity,
            String deathCauseLocaleKey,
            @Nullable Component killerEntity,
            @Nullable Component killerWeapon
    ) {}

    @Nullable
    public static DeathMessageComponents getDeathMessageComponents(Component original) {
        if (!(original.getContents() instanceof TranslatableContents tc))
            return null;

        Object[] args = tc.getArgs();
        return new DeathMessageComponents(
                arg(args, 0),
                tc.getKey(),
                arg(args, 1),
                arg(args, 2)
        );
    }

    @Nullable
    private static Component arg(Object[] args, int i) {
        if (i >= args.length)
            return null;

        return args[i] instanceof Component c
                ? c
                : Component.literal(String.valueOf(args[i]));
    }
}

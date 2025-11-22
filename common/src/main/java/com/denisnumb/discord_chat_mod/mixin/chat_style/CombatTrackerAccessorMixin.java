package com.denisnumb.discord_chat_mod.mixin.chat_style;

import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.damagesource.CombatTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(CombatTracker.class)
public interface CombatTrackerAccessorMixin {
    @Accessor("entries")
    List<CombatEntry> getEntries();
}

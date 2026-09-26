package com.denisnumb.discord_chat_mod.chat;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public record MinecraftMessageContext(
        Component player,
        Component content,
        @Nullable Component team,
        @Nullable Entity entity
) {}

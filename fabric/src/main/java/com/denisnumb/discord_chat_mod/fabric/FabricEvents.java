package com.denisnumb.discord_chat_mod.fabric;

import com.denisnumb.discord_chat_mod.MinecraftEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class FabricEvents {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, context, environment) -> {
            MinecraftEvents.handleRegisterCommands(dispatcher, context);
        });

        ServerLivingEntityEvents.AFTER_DEATH.register(MinecraftEvents::handlePlayerDie);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            MinecraftEvents.handleJoinLeave(handler.player, true);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            MinecraftEvents.handleJoinLeave(handler.player, false);
        });
    }

    public static void onAdvancementMade(ServerPlayer player, Advancement advancement){
        MinecraftEvents.handleAdvancementMade(player, advancement);
    }

    public static Component onChatMessage(ServerPlayer player, Component message){
        return MinecraftEvents.handleChatMessage(player, message);
    }
}


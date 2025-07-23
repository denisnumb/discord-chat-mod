package com.denisnumb.discord_chat_mod.neoforge;

import com.denisnumb.discord_chat_mod.DiscordChatMod;
import com.denisnumb.discord_chat_mod.MinecraftEvents;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = DiscordChatMod.MOD_ID)
public class NeoForgeEvents {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        MinecraftEvents.handleRegisterCommands(event.getDispatcher(), event.getBuildContext());
    }

    @SubscribeEvent
    public static void onChatMessage(ServerChatEvent event) {
        event.setMessage(MinecraftEvents.handleChatMessage(event.getPlayer(), event.getMessage()));
    }

    @SubscribeEvent
    public static void onPlayerDieEvent(LivingDeathEvent event) {
        MinecraftEvents.handlePlayerDie(event.getEntity(), event.getSource());
    }

    @SubscribeEvent
    public static void onAdvancementMade(AdvancementEvent.AdvancementEarnEvent event) {
        MinecraftEvents.handleAdvancementMade(event.getEntity(), event.getAdvancement());
    }

    @SubscribeEvent
    public static void onPlayerJoinEvent(PlayerEvent.PlayerLoggedInEvent event) {
        MinecraftEvents.handleJoinLeave(event.getEntity(), true);
    }

    @SubscribeEvent
    public static void onPlayerLeaveEvent(PlayerEvent.PlayerLoggedOutEvent event){
        MinecraftEvents.handleJoinLeave(event.getEntity(), false);
    }

}

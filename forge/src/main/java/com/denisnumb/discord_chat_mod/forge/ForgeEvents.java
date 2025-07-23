package com.denisnumb.discord_chat_mod.forge;

import com.denisnumb.discord_chat_mod.DiscordChatMod;
import com.denisnumb.discord_chat_mod.MinecraftEvents;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DiscordChatMod.MOD_ID)
public class ForgeEvents {
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

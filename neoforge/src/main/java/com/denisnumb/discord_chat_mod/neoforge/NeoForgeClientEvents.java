package com.denisnumb.discord_chat_mod.neoforge;

import com.denisnumb.discord_chat_mod.DiscordChatMod;
import com.denisnumb.discord_chat_mod.MinecraftClientEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ScreenshotEvent;

@EventBusSubscriber(modid = DiscordChatMod.MOD_ID, value = Dist.CLIENT)
public class NeoForgeClientEvents {
    @SubscribeEvent
    public static void onScreenshot(ScreenshotEvent event){
        event.setResultMessage(MinecraftClientEvents.handleScreenshot(event.getScreenshotFile()));
    }

    @SubscribeEvent
    public static void onChatMessage(ClientChatReceivedEvent event){
        event.setMessage(MinecraftClientEvents.handleChatMessage(event.getMessage()));
    }

    @SubscribeEvent
    public static void onJoinServer(ClientPlayerNetworkEvent.LoggingIn event){
        MinecraftClientEvents.handleJoinServer();
    }
}

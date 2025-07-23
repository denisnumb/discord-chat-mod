package com.denisnumb.discord_chat_mod.forge;

import com.denisnumb.discord_chat_mod.DiscordChatMod;
import com.denisnumb.discord_chat_mod.MinecraftClientEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenshotEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DiscordChatMod.MOD_ID, value = Dist.CLIENT)
public class ForgeClientEvents {
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

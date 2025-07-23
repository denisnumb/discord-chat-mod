package com.denisnumb.discord_chat_mod.fabric;

import com.denisnumb.discord_chat_mod.MinecraftClientEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.network.chat.Component;

import java.io.File;

public class FabricClientEvents {
    public static void register() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> MinecraftClientEvents.handleJoinServer());
    }

    public static Component onScreenshot(File screenshotFile){
        return MinecraftClientEvents.handleScreenshot(screenshotFile);
    }

    public static Component onChatMessage(Component component){
        return MinecraftClientEvents.handleChatMessage(component);
    }
}

package com.denisnumb.discord_chat_mod.fabric.client;

import com.denisnumb.discord_chat_mod.fabric.FabricClientEvents;
import com.denisnumb.discord_chat_mod.fabric.network.FabricNetworking;
import net.fabricmc.api.ClientModInitializer;

public final class DiscordChatModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricNetworking.initClient();
        FabricClientEvents.register();
    }
}

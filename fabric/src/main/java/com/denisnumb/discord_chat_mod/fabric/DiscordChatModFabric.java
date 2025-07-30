package com.denisnumb.discord_chat_mod.fabric;

import com.denisnumb.discord_chat_mod.DiscordChatMod;
import com.denisnumb.discord_chat_mod.config.PlatformConfig;
import com.denisnumb.discord_chat_mod.fabric.config.Config;
import com.denisnumb.discord_chat_mod.fabric.config.FabricConfig;
import com.denisnumb.discord_chat_mod.fabric.network.FabricNetworking;
import com.denisnumb.discord_chat_mod.fabric.network.FabricPacketDistributor;
import com.denisnumb.discord_chat_mod.network.PlatformPacketDistributor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import static com.denisnumb.discord_chat_mod.LocaleProvider.*;

public final class DiscordChatModFabric implements ModInitializer{
    @Override
    public void onInitialize() {
        Config.load();
        FabricNetworking.init();
        FabricEvents.register();
        loadLocalization();
        PlatformConfig.setConfigProvider(new FabricConfig());
        PlatformPacketDistributor.setHandler(new FabricPacketDistributor());
        ServerLifecycleEvents.SERVER_STARTING.register(DiscordChatMod::onServerStarting);
        ServerLifecycleEvents.SERVER_STARTED.register(server -> DiscordChatMod.onServerStarted());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> DiscordChatMod.onServerStopped());
    }

    private void loadLocalization() {
        String configLocale = Config.modLocale;

        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            String namespace = mod.getMetadata().getId();

            if (namespace.equals("minecraft") && !configLocale.equals("en_us")){
                loadMinecraftLocale(configLocale);
                continue;
            }

            loadLocaleFromPath(mod.findPath(String.format("assets/%s/lang/%s.json", namespace, configLocale)).orElse(null));
        }
    }
}

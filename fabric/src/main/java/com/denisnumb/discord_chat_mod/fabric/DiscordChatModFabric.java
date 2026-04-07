package com.denisnumb.discord_chat_mod.fabric;

import com.denisnumb.discord_chat_mod.DiscordChatMod;
import com.denisnumb.discord_chat_mod.LocaleProvider;
import com.denisnumb.discord_chat_mod.config.ConfigProvider;
import com.denisnumb.discord_chat_mod.config.ConfigManager;
import com.denisnumb.discord_chat_mod.config.ConfigProviderImpl;
import com.denisnumb.discord_chat_mod.fabric.compat.VanishCompat;
import com.denisnumb.discord_chat_mod.fabric.network.FabricNetworking;
import com.denisnumb.discord_chat_mod.fabric.network.FabricPacketDistributor;
import com.denisnumb.discord_chat_mod.network.PlatformPacketDistributor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import static com.denisnumb.discord_chat_mod.LocaleProvider.*;

public final class DiscordChatModFabric implements ModInitializer{
    @Override
    public void onInitialize() {
        ConfigManager.load(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT);
        FabricNetworking.init();
        ConfigProvider.setConfigProvider(new ConfigProviderImpl());
        LocaleProvider.setLocaleLoader(new FabricLocaleLoader());
        loadLocalization();
        PlatformPacketDistributor.setHandler(new FabricPacketDistributor());
        VanishCompat.init();
        ServerLifecycleEvents.SERVER_STARTING.register(DiscordChatMod::onServerStarting);
        ServerLifecycleEvents.SERVER_STARTED.register(server -> DiscordChatMod.onServerStarted());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> DiscordChatMod.onServerStopped());
    }

    public static class FabricLocaleLoader implements LocaleLoader{
        public void loadLocalization() {
            String configLocale = ConfigProvider.getConfig().modLocale();

            for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
                String namespace = mod.getMetadata().getId();

                if (namespace.equals("minecraft") && !configLocale.equals("en_us")){
                    loadMinecraftLocale(configLocale);
                    continue;
                }

                loadLocaleFromResource(String.format("/assets/%s/lang/%s.json", namespace, configLocale));
            }
        }
    }
}

package com.denisnumb.discord_chat_mod.neoforge;

import com.denisnumb.discord_chat_mod.DiscordChatMod;
import com.denisnumb.discord_chat_mod.LocaleProvider;
import com.denisnumb.discord_chat_mod.MinecraftEvents;
import com.denisnumb.discord_chat_mod.config.ConfigManager;
import com.denisnumb.discord_chat_mod.config.ConfigProvider;
import com.denisnumb.discord_chat_mod.config.ConfigProviderImpl;
import com.denisnumb.discord_chat_mod.neoforge.network.NeoForgePacketDistributor;
import com.denisnumb.discord_chat_mod.network.PlatformPacketDistributor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforgespi.language.IModInfo;

import java.nio.file.Path;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.*;
import static com.denisnumb.discord_chat_mod.LocaleProvider.*;


@Mod(DiscordChatMod.MOD_ID)
public final class DiscordChatModNeoForge {
    public DiscordChatModNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        NeoForgeLocaleLoader localeLoader = new NeoForgeLocaleLoader();
        LocaleProvider.setLocaleLoader(localeLoader);
        modEventBus.addListener(localeLoader::loadLocalizationFromSetup);
        NeoForge.EVENT_BUS.register(this);
        ConfigManager.load(FMLEnvironment.getDist() == Dist.CLIENT);
        ConfigProvider.setConfigProvider(new ConfigProviderImpl());
        PlatformPacketDistributor.setHandler(new NeoForgePacketDistributor());
    }

    public static class NeoForgeLocaleLoader implements LocaleLoader{
        public void loadLocalizationFromSetup(final FMLCommonSetupEvent event) {
            loadLocalization();
        }

        public void loadLocalization() {
            ModList modList = ModList.get();
            String configLocale = ConfigProvider.getConfig().modLocale();

            for (IModInfo modInfo : modList.getMods()){
                String namespace = modInfo.getNamespace();

                if (namespace.equals("minecraft") && !configLocale.equals("en_us")){
                    loadMinecraftLocale(configLocale);
                    continue;
                }

                String localePath = String.format("/assets/%s/lang/%s.json", namespace, configLocale);
                try {
                    loadLocaleFromPath(Path.of(modList.getModFileById(namespace).getFile().getContents().findFile(localePath).orElseThrow()));
                } catch (Exception e) {
                    LOGGER.warn("Failed to load localization {}", localePath);
                }
            }
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        MinecraftEvents.handleRegisterCommands(event.getDispatcher(), event.getBuildContext());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        DiscordChatMod.onServerStarting(event.getServer());
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent ignored) {
        DiscordChatMod.onServerStarted();
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent ignored) {
        DiscordChatMod.onServerStopped();
    }
}

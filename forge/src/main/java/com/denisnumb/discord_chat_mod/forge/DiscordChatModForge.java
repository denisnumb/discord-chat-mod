package com.denisnumb.discord_chat_mod.forge;

import com.denisnumb.discord_chat_mod.DiscordChatMod;
import com.denisnumb.discord_chat_mod.LocaleProvider;
import com.denisnumb.discord_chat_mod.MinecraftEvents;
import com.denisnumb.discord_chat_mod.config.ConfigManager;
import com.denisnumb.discord_chat_mod.config.ConfigProvider;
import com.denisnumb.discord_chat_mod.config.ConfigProviderImpl;
import com.denisnumb.discord_chat_mod.forge.network.ForgePacketDistributor;
import com.denisnumb.discord_chat_mod.network.PlatformPacketDistributor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.forgespi.language.IModInfo;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.*;
import static com.denisnumb.discord_chat_mod.LocaleProvider.*;


@Mod(DiscordChatMod.MOD_ID)
public final class DiscordChatModForge {
    public DiscordChatModForge(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        ForgeLocaleLoader localeLoader = new ForgeLocaleLoader();
        LocaleProvider.setLocaleLoader(localeLoader);
        modEventBus.addListener(localeLoader::loadLocalizationFromSetup);
        MinecraftForge.EVENT_BUS.register(this);
        ConfigManager.load(FMLEnvironment.dist == Dist.CLIENT);
        ConfigProvider.setConfigProvider(new ConfigProviderImpl());
        PlatformPacketDistributor.setHandler(new ForgePacketDistributor());
    }

    public DiscordChatModForge() {
        this(FMLJavaModLoadingContext.get());
    }

    public static class ForgeLocaleLoader implements LocaleLoader {
        public void loadLocalizationFromSetup(final FMLCommonSetupEvent event){
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
                    loadLocaleFromPath(modList.getModFileById(namespace).getFile().findResource(localePath));
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

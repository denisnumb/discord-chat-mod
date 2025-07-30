package com.denisnumb.discord_chat_mod.forge;

import com.denisnumb.discord_chat_mod.DiscordChatMod;
import com.denisnumb.discord_chat_mod.config.PlatformConfig;
import com.denisnumb.discord_chat_mod.forge.config.Config;
import com.denisnumb.discord_chat_mod.forge.config.ForgeConfig;
import com.denisnumb.discord_chat_mod.forge.network.ForgePacketDistributor;
import com.denisnumb.discord_chat_mod.network.PlatformPacketDistributor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.*;
import static com.denisnumb.discord_chat_mod.LocaleProvider.*;


@Mod(DiscordChatMod.MOD_ID)
public final class DiscordChatModForge {
    public DiscordChatModForge(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::loadLocalization);
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
        PlatformConfig.setConfigProvider(new ForgeConfig());
        PlatformPacketDistributor.setHandler(new ForgePacketDistributor());
    }

    private void loadLocalization(final FMLCommonSetupEvent event) {
        ModList modList = ModList.get();
        String configLocale = Config.MOD_LOCALE.get();

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

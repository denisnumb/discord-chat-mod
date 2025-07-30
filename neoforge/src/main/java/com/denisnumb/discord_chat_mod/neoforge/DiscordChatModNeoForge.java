package com.denisnumb.discord_chat_mod.neoforge;

import com.denisnumb.discord_chat_mod.DiscordChatMod;
import com.denisnumb.discord_chat_mod.config.PlatformConfig;
import com.denisnumb.discord_chat_mod.neoforge.config.Config;
import com.denisnumb.discord_chat_mod.neoforge.config.NeoForgeConfig;
import com.denisnumb.discord_chat_mod.neoforge.network.NeoForgePacketDistributor;
import com.denisnumb.discord_chat_mod.network.PlatformPacketDistributor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforgespi.language.IModInfo;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.*;
import static com.denisnumb.discord_chat_mod.LocaleProvider.*;


@Mod(DiscordChatMod.MOD_ID)
public final class DiscordChatModNeoForge {
    public DiscordChatModNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::loadLocalization);
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
        PlatformConfig.setConfigProvider(new NeoForgeConfig());
        PlatformPacketDistributor.setHandler(new NeoForgePacketDistributor());
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

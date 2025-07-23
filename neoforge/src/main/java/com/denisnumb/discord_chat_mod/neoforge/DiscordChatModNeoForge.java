package com.denisnumb.discord_chat_mod.neoforge;

import com.denisnumb.discord_chat_mod.DiscordChatMod;
import com.denisnumb.discord_chat_mod.config.PlatformConfig;
import com.denisnumb.discord_chat_mod.neoforge.config.Config;
import com.denisnumb.discord_chat_mod.neoforge.config.NeoForgeConfig;
import com.denisnumb.discord_chat_mod.neoforge.network.NeoForgePacketDistributor;
import com.denisnumb.discord_chat_mod.network.PlatformPacketDistributor;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.BiConsumer;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.*;


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

        BiConsumer<IModInfo, String> loadFromNamespace = (mod, namespace) -> {
            String localePath = String.format("/assets/%s/lang/%s.json", namespace, Config.MOD_LOCALE.get());
            try {
                Path path = modList.getModFileById(mod.getNamespace()).getFile().findResource(localePath);
                if (!Files.exists(path))
                    return;
                languageData.putAll(new Gson().fromJson(Files.readString(path), new TypeToken<Map<String, String>>(){}.getType()));
            } catch (Exception e) {
                LOGGER.warn("Failed to load localization {}", localePath);
            }
        };

        for (IModInfo modInfo : modList.getMods()){
            if (modInfo.getNamespace().equals("minecraft"))
                continue;
            if (modInfo.getNamespace().equals(MOD_ID))
                loadFromNamespace.accept(modInfo, "minecraft");

            loadFromNamespace.accept(modInfo, modInfo.getNamespace());

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

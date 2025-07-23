package com.denisnumb.discord_chat_mod.forge;

import com.denisnumb.discord_chat_mod.DiscordChatMod;
import com.denisnumb.discord_chat_mod.config.PlatformConfig;
import com.denisnumb.discord_chat_mod.forge.config.Config;
import com.denisnumb.discord_chat_mod.forge.config.ForgeConfig;
import com.denisnumb.discord_chat_mod.forge.network.ForgePacketDistributor;
import com.denisnumb.discord_chat_mod.network.PlatformPacketDistributor;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.BiConsumer;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.*;


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

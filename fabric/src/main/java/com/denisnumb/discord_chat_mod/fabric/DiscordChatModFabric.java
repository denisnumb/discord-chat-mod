package com.denisnumb.discord_chat_mod.fabric;

import com.denisnumb.discord_chat_mod.DiscordChatMod;
import com.denisnumb.discord_chat_mod.config.PlatformConfig;
import com.denisnumb.discord_chat_mod.fabric.config.Config;
import com.denisnumb.discord_chat_mod.fabric.config.FabricConfig;
import com.denisnumb.discord_chat_mod.fabric.network.FabricNetworking;
import com.denisnumb.discord_chat_mod.fabric.network.FabricPacketDistributor;
import com.denisnumb.discord_chat_mod.network.PlatformPacketDistributor;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.*;

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
        BiConsumer<ModContainer, String> loadFromNamespace = (mod, namespace) -> {
            String localePath = String.format("assets/%s/lang/%s.json", namespace, Config.modLocale);
            Optional<Path> optionalPath = mod.findPath(localePath);
            if (optionalPath.isEmpty())
                return;

            Path modPath = optionalPath.get();
            try {
                if (!Files.exists(modPath))
                    return;
                languageData.putAll(new Gson().fromJson(Files.readString(modPath), new TypeToken<Map<String, String>>() {}.getType()));
            } catch (Exception e) {
                LOGGER.warn("Failed to load localization {}", localePath);
            }
        };

        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            String modId = mod.getMetadata().getId();
            if (modId.equals("minecraft"))
                continue;
            if (modId.equals(MOD_ID))
                loadFromNamespace.accept(mod, "minecraft");

            loadFromNamespace.accept(mod, modId);
        }
    }
}

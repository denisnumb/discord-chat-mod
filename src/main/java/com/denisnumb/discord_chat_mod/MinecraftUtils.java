package com.denisnumb.discord_chat_mod;

import com.denisnumb.discord_chat_mod.markdown.tellraw.TellRawComponent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.locale.Language;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

import static com.denisnumb.discord_chat_mod.ColorUtils.Color.*;
import static com.denisnumb.discord_chat_mod.DiscordChatMod.*;
import static com.denisnumb.discord_chat_mod.ColorUtils.getHexColor;
import static com.denisnumb.discord_chat_mod.discord.DiscordUtils.prepareTellRawCommand;

public class MinecraftUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void executeServerCommand(String command) {
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command);
    }

    public static void logErrorToServer(String message) {
        LOGGER.error(message);
        if (Config.LOG_DISCORD_ERRORS_TO_SERVER_CHAT.get())
            executeServerCommand(buildLogMessageCommand(message, RED));
    }

    private static String buildLogMessageCommand(String message, int color) {
        String hexColor = getHexColor(color);
        ArrayList<TellRawComponent> components = new ArrayList<>() {{
           add(new TellRawComponent("[discord_chat_mod] ").setBold().setColor(hexColor));
           add(new TellRawComponent(message).setColor(hexColor));
        }};

        return prepareTellRawCommand(Config.DISCORD_ERRORS_CHAT_PLAYER_SELECTOR.get(), components);
    }

    public static String getTranslateClient(String key, String defaultValue){
        return Language.getInstance().getLanguageData().getOrDefault(key, defaultValue);
    }

    public static String getTranslateClient(String key){
        return Language.getInstance().getLanguageData().get(key);
    }

    public static String getTranslate(String key, String defaultValue) {
        return languageData.getOrDefault(key, defaultValue);
    }

    public static String getTranslate(String key) {
        return languageData.containsKey(key)
                ? languageData.get(key)
                : Language.getInstance().getLanguageData().get(key);
    }

    public static void loadLocalization() {
        ModList modList = ModList.get();
        for (IModInfo modInfo : modList.getMods()){
            if (modInfo.getNamespace().equals("minecraft"))
                continue;
            String localePath = String.format("/assets/%s/lang/%s.json", modInfo.getNamespace(), Config.MOD_LOCALE.get());
            try {
                Path path = modList.getModFileById(modInfo.getNamespace()).getFile().findResource(localePath);
                if (!Files.exists(path))
                    continue;
                languageData.putAll(new Gson().fromJson(Files.readString(path), new TypeToken<Map<String, String>>(){}.getType()));
            } catch (Exception e) {
                LOGGER.error("Failed to load localization {}", localePath);
            }
        }
    }

    public static String getLocalizedDeathMessage(DamageSource source, LivingEntity diedEntity) {
        String diedEntityName = diedEntity.getDisplayName().getString();
        String attackBase = "death.attack." + source.type().msgId();

        if (source.getEntity() == null && source.getDirectEntity() == null) {
            LivingEntity playerKiller = diedEntity.getKillCredit();
            String byPlayer = attackBase + ".player";

            return playerKiller != null
                    ? String.format(getTranslate(byPlayer), diedEntityName, playerKiller.getDisplayName().getString())
                    : String.format(getTranslate(attackBase), diedEntityName);
        } else {
            String killerEntity = source.getEntity() == null
                    ? getTranslate(source.getDirectEntity().getType().getDescriptionId())
                    : getTranslate(source.getEntity().getType().getDescriptionId());

            Entity entity = source.getEntity();
            ItemStack item = (entity instanceof LivingEntity)
                    ? ((LivingEntity)entity).getMainHandItem()
                    : ItemStack.EMPTY;

            return !item.isEmpty() && item.has(DataComponents.CUSTOM_NAME)
                    ? String.format(getTranslate(attackBase + ".item"), diedEntityName, killerEntity, item.getDisplayName().getString())
                    : String.format(getTranslate(attackBase), diedEntityName, killerEntity);
        }
    }
}

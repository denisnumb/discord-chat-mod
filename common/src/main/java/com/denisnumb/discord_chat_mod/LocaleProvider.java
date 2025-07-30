package com.denisnumb.discord_chat_mod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.locale.Language;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LocaleProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type TYPE = new TypeToken<Map<String, String>>(){}.getType();
    private static final Map<String, String> LANGUAGE_DATA = new HashMap<>();
    private static final String CACHE_DIR_NAME = "locale_cache";
    private static final String BASE_GITHUB_URL = "https://raw.githubusercontent.com/denisnumb/discord-chat-mod/1.21.1/data/minecraft_locales/";


    public static String getTranslateClient(String key){
        return Language.getInstance().getOrDefault(key);
    }

    public static String getTranslate(String key) {
        return LANGUAGE_DATA.containsKey(key)
                ? LANGUAGE_DATA.get(key)
                : Language.getInstance().getOrDefault(key);
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
                    ? getTranslate(Objects.requireNonNull(source.getDirectEntity()).getType().getDescriptionId())
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

    public static boolean loadLocaleFromPath(@Nullable Path localePath){
        try{
            if (localePath != null && Files.exists(localePath)){
                addLanguageData(GSON.fromJson(Files.readString(localePath), TYPE));
                return true;
            }
        } catch (Exception e){
            LOGGER.warn("Failed to load localization {}", localePath);
        }

        return false;
    }

    public static void loadMinecraftLocale(String locale){
        if (loadLocaleFromPath(getCachedFile(locale)))
            return;

        String url = String.format(BASE_GITHUB_URL + locale + ".json");
        try (InputStream input = new URI(url).toURL().openStream(); Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            Map<String, String> data = GSON.fromJson(reader, TYPE);
            saveMinecraftLocaleToCache(locale, data);
            addLanguageData(data);
        } catch (Exception e) {
            LOGGER.warn("Failed to load minecraft localization from {}", url);
            e.printStackTrace();
        }
    }

    private static void addLanguageData(Map<String, String> languageData){
        LANGUAGE_DATA.putAll(languageData);
    }

    @Nullable
    private static Path getCachedFile(String locale){
        Path cacheDir = Paths.get(CACHE_DIR_NAME);
        if (!Files.exists(cacheDir)) {
            try {
                Files.createDirectories(cacheDir);
            } catch (IOException e) {
                return null;
            }
        }

        return cacheDir.resolve(locale + ".json");
    }

    private static void saveMinecraftLocaleToCache(String locale, Map<String, String> data){
        Path cachedFile = getCachedFile(locale);

        if (cachedFile != null){
            if (Files.exists(cachedFile))
                return;
            try {
                Files.writeString(cachedFile, GSON.toJson(data), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException ignored) {}
        }
    }
}

package com.denisnumb.discord_chat_mod;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static com.denisnumb.discord_chat_mod.ColorUtils.Color.*;
import static com.denisnumb.discord_chat_mod.DiscordChatMod.*;

public class MinecraftUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void sendMessageToPlayersBySelector(Component message, String selector) {
        CommandSourceStack fakeSource = server.createCommandSourceStack()
                .withSuppressedOutput()
                .withPermission(4);

        try {
            EntitySelectorParser parser = new EntitySelectorParser(new StringReader(selector), true);
            EntitySelector entitySelector = parser.parse();

            for (ServerPlayer player : entitySelector.findPlayers(fakeSource))
                player.sendSystemMessage(ComponentUtils.updateForEntity(null, message, player, 0), false);
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void sendMessageToAllPlayers(Component message){
        sendMessageToPlayersBySelector(message, "@a");
    }

    public static void showTitleBarMessage(Component message){
        Minecraft.getInstance().gui.setOverlayMessage(message, false);
    }

    public static void logErrorToServer(String message) {
        LOGGER.error(message);
        if (Config.LOG_DISCORD_ERRORS_TO_SERVER_CHAT.get())
            sendMessageToPlayersBySelector(buildLogMessageComponent(message, RED), Config.DISCORD_ERRORS_CHAT_PLAYER_SELECTOR.get());
    }

    private static Component buildLogMessageComponent(String message, int color) {
        return Component.empty()
                .append(Component.literal("[discord_chat_mod] ")
                        .withStyle(style -> style.withBold(true))
                )
                .append(Component.literal(message)).withStyle(style -> style.withColor(color));
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
                LOGGER.warn("Failed to load localization {}", localePath);
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

            return !item.isEmpty() && item.hasCustomHoverName()
                    ? String.format(getTranslate(attackBase + ".item"), diedEntityName, killerEntity, item.getDisplayName().getString())
                    : String.format(getTranslate(attackBase), diedEntityName, killerEntity);
        }
    }
}

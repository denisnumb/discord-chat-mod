package com.denisnumb.discord_chat_mod;

import com.denisnumb.discord_chat_mod.config.PlatformConfig;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.core.component.DataComponents;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.Objects;

import static com.denisnumb.discord_chat_mod.ColorUtils.Color.RED;
import static com.denisnumb.discord_chat_mod.ColorUtils.Color.YELLOW;
import static com.denisnumb.discord_chat_mod.DiscordChatMod.languageData;
import static com.denisnumb.discord_chat_mod.DiscordChatMod.server;

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
        } catch (Exception ignored){}
    }

    public static void sendMessageToAllPlayers(Component message){
        sendMessageToPlayersBySelector(message, "@a");
    }

    public static void showTitleBarMessage(Component message){
        Minecraft.getInstance().gui.setOverlayMessage(message, false);
    }

    public static void logErrorToServer(String message) {
        LOGGER.error(message);
        if (PlatformConfig.getConfig().isLoggingDiscordErrorsToServerChatEnabled())
            sendMessageToPlayersBySelector(buildLogMessageComponent(message, RED), PlatformConfig.getConfig().discordErrorsChatPlayerSelector());
    }

    public static void logWarnToServer(String message) {
        LOGGER.warn(message);
        if (PlatformConfig.getConfig().isLoggingDiscordErrorsToServerChatEnabled())
            sendMessageToPlayersBySelector(buildLogMessageComponent(message, YELLOW), PlatformConfig.getConfig().discordErrorsChatPlayerSelector());
    }

    private static Component buildLogMessageComponent(String message, int color) {
        return Component.empty()
                .append(Component.literal("[discord_chat_mod] ")
                        .withStyle(style -> style.withBold(true))
                )
                .append(Component.literal(message)).withColor(color);
    }

    public static String getTranslateClient(String key){
        return Language.getInstance().getOrDefault(key);
    }

    public static String getTranslate(String key) {
        return languageData.containsKey(key)
                ? languageData.get(key)
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
}

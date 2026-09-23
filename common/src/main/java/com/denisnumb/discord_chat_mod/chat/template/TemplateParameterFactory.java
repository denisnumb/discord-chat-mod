package com.denisnumb.discord_chat_mod.chat.template;

import com.denisnumb.discord_chat_mod.utils.PlayerAvatarProvider;
import com.denisnumb.discord_chat_mod.config.ConfigProvider;
import com.denisnumb.discord_chat_mod.utils.JavaUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.denisnumb.discord_chat_mod.chat.template.TemplateParameter.*;
import static com.denisnumb.discord_chat_mod.chat.template.TemplateParameter.DIMENSION;
import static com.denisnumb.discord_chat_mod.chat.template.TemplateParameter.X;
import static com.denisnumb.discord_chat_mod.chat.template.TemplateParameter.Y;
import static com.denisnumb.discord_chat_mod.chat.template.TemplateParameter.Z;
import static com.denisnumb.discord_chat_mod.utils.JavaUtils.mergeMaps;

public final class TemplateParameterFactory {
    private TemplateParameterFactory() {}

    public static Map<TemplatePlaceholder, String> buildPlayerParameters(CommandSourceStack source){
        return source.getPlayer() == null
                ? buildPlayerParameters(source.getDisplayName().getString(), null)
                : buildPlayerParameters(source.getDisplayName().getString(), source.getEntity());
    }

    public static Map<TemplatePlaceholder, String> buildPlayerParameters(Entity entity){
        return buildPlayerParameters(entity.getDisplayName().getString(), entity);
    }

    public static Map<TemplatePlaceholder, String> buildPlayerParameters(String displayName, Entity entity){
        Map<TemplatePlaceholder, String> result = new HashMap<>();
        result.put(PLAYER, displayName);
        if (entity instanceof Player player)
            result.put(PLAYER_AVATAR_URL, PlayerAvatarProvider.getPlayerAvatarUrl(player));
        else
            result.put(PLAYER_AVATAR_URL, ConfigProvider.getConfig().webhookServerAvatarUrl());

        return mergeMaps(result, buildPositionParameters(entity));
    }

    public static Map<TemplatePlaceholder, Component> buildTimestampParameters(){
        Map<TemplatePlaceholder, Component> result = new HashMap<>();
        OffsetDateTime now = JavaUtils.getDateTimeWithUtcOffset(ConfigProvider.getConfig().utcOffsetHours());
        result.put(HH, Component.literal(String.format("%02d", now.getHour())));
        result.put(MM, Component.literal(String.format("%02d", now.getMinute())));
        result.put(SS, Component.literal(String.format("%02d", now.getSecond())));

        return result;
    }

    public static Map<TemplatePlaceholder, Component> buildPositionComponentParameters(@Nullable Entity entity){
        return buildPositionParameters(entity).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> Component.literal(e.getValue())));
    }

    public static Map<TemplatePlaceholder, String> buildPositionParameters(@Nullable Entity entity){
        HashMap<TemplatePlaceholder, String> result = new HashMap<>();

        if (entity == null){
            result.put(X, "");
            result.put(Y, "");
            result.put(Z, "");
            result.put(DIMENSION, "");
            return result;
        }

        BlockPos pos = entity.blockPosition();
        result.put(X, String.valueOf(pos.getX()));
        result.put(Y, String.valueOf(pos.getY()));
        result.put(Z, String.valueOf(pos.getZ()));
        result.put(DIMENSION, getDimensionName(entity.level().dimension()));

        return result;
    }

    private static String getDimensionName(ResourceKey<@NotNull Level> dimension){
        if (dimension == Level.OVERWORLD)
            return "Overworld";
        if (dimension == Level.NETHER)
            return "Nether";
        if (dimension == Level.END)
            return "End";
        return prettifyDimensionPath(dimension.identifier().getPath());
    }

    private static String prettifyDimensionPath(String path){
        return Arrays.stream(path.split("_"))
                .filter(word -> !word.isEmpty())
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}

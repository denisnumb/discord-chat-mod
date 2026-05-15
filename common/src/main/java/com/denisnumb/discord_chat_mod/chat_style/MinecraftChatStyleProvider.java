package com.denisnumb.discord_chat_mod.chat_style;

import com.denisnumb.discord_chat_mod.config.ConfigProvider;
import com.denisnumb.discord_chat_mod.config.IConfigProvider;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.denisnumb.discord_chat_mod.DeathMessageUtils.*;
import static com.denisnumb.discord_chat_mod.chat_style.CustomChatTypeRegistry.*;
import static com.denisnumb.discord_chat_mod.chat_style.ChatStyleUtils.*;
import static com.denisnumb.discord_chat_mod.chat_style.Parameters.*;

public class MinecraftChatStyleProvider {
    private static Map<String, Component> buildPositionComponentParameters(@Nullable Entity entity){
        return buildPositionParameters(entity).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> Component.literal(e.getValue())));
    }

    private static Component applyStyleToAdvancement(String translatedTitle, String translatedDescription, Style advancementStyle) {
        Component title = Component.literal(translatedTitle);
        Component description = ComponentUtils.mergeStyles(title.copy(), Style.EMPTY.withColor(advancementStyle.getColor())).append("\n")
                .append(Component.literal(translatedDescription));
        Component result = title.copy().withStyle((style) -> style.withHoverEvent(new HoverEvent.ShowText(description)));

        return ComponentUtils.wrapInSquareBrackets(result).withStyle(advancementStyle);
    }

    public static Optional<Component> getStyledAdvancementMessage(Player player, AdvancementHolder advancementHolder, String title, String description){
        IConfigProvider config = ConfigProvider.getConfig();
        DisplayInfo displayInfo = advancementHolder.value().display().get();

        String messageStringTemplate = displayInfo.getType() == AdvancementType.TASK
                ? config.minecraftPlayerAdvancementTaskStyle()
                : displayInfo.getType() == AdvancementType.GOAL
                ? config.minecraftPlayerAdvancementGoalStyle()
                : config.minecraftPlayerAdvancementChallengeStyle();

        String translationKey = displayInfo.getType() == AdvancementType.TASK
                ? ADVANCEMENT_TASK
                : displayInfo.getType() == AdvancementType.GOAL
                ? ADVANCEMENT_GOAL
                : ADVANCEMENT_CHALLENGE;

        MutableComponent template = parseConfigTemplateMarkdown(setConfigTemplateTranslatableParameters(messageStringTemplate, translationKey));
        Style advancementStyle = parseTemplateParameterStyles(template, ADVANCEMENT).get(ADVANCEMENT);

        return Optional.of(applyParametersToTemplate(template, mergeMaps(
                Map.of(PLAYER, player.getDisplayName(), ADVANCEMENT, applyStyleToAdvancement(title, description, advancementStyle)),
                buildPositionComponentParameters(player)
        )));
    }

    public static Optional<Component> getStyledJoinedLeftMessage(Player player, boolean isJoin) {
        IConfigProvider config = ConfigProvider.getConfig();

        String messageStringTemplate = isJoin
                ? config.minecraftPlayerJoinedStyle()
                : config.minecraftPlayerLeftStyle();

        String translationKey = isJoin
                ? PLAYER_JOINED
                : PLAYER_LEFT;

        return Optional.of(applyParametersToTemplate(
                parseConfigTemplateMarkdown(setConfigTemplateTranslatableParameters(messageStringTemplate, translationKey)),
                mergeMaps(Map.of(PLAYER, player.getDisplayName()), buildPositionComponentParameters(player))
        ));
    }

    public record ChatMessageComponents(Component player, Component content, @Nullable Component team, @Nullable Entity sender) {}

    public static Optional<Component> getStyledChatMessage(ResourceKey<ChatType> chatType, ChatMessageComponents components){
        String configTemplate = getConfigTemplateByChatType(chatType);

        if (configTemplate == null)
            return Optional.empty();

        String[] params = getParametersByChatType(chatType);
        Component[] values = params.length == 3
                ? new Component[] { components.team, components.player, components.content }
                : new Component[] { components.player, components.content };

        Map<String, Component> parameterToComponent = IntStream.range(0, params.length)
                .boxed()
                .collect(Collectors.toMap(i -> params[i], i -> values[i]));

        return Optional.of(applyParametersToTemplate(
                parseConfigTemplateMarkdown(configTemplate),
                mergeMaps(parameterToComponent, buildPositionComponentParameters(components.sender()))
        ));
    }

    public static Optional<Component> getStyledDeathMessage(DeathMessageComponents components, Entity entity) {
        IConfigProvider config = ConfigProvider.getConfig();

        String causeStyle = config.minecraftPlayerDeathCauseStyle().replace(DEATH_CAUSE, DEATH_CAUSE_REPLACEMENT_TAG);
        String nameStyle = config.minecraftPlayerDeathNameStyle().replace(PLAYER, DIED_ENTITY_REPLACEMENT_TAG);
        String entityStyle = config.minecraftPlayerDeathSecondEntityNameStyle().replace(SECOND_ENTITY, KILLER_ENTITY_REPLACEMENT_TAG);
        String itemStyle = config.minecraftPlayerDeathWeaponStyle().replace(ITEM, ITEM_REPLACEMENT_TAG);

        Component causeTemplate = parseConfigTemplateMarkdown(causeStyle);
        Component playerTemplate = parseConfigTemplateMarkdown(nameStyle);
        Component entityTemplate = parseConfigTemplateMarkdown(entityStyle);
        Component itemTemplate = parseConfigTemplateMarkdown(itemStyle);

        playerTemplate = applyParametersToTemplate(playerTemplate.copy(), Map.of(DIED_ENTITY_REPLACEMENT_TAG, components.diedEntityName()));
        causeTemplate = applyParametersToTemplate(causeTemplate.copy(), Map.of(DEATH_CAUSE_REPLACEMENT_TAG, components.deathCause()));
        if (components.killerEntity() != null)
            entityTemplate = applyParametersToTemplate(entityTemplate.copy(), Map.of(KILLER_ENTITY_REPLACEMENT_TAG, components.killerEntity()));
        if (components.item() != null)
            itemTemplate = applyParametersToTemplate(itemTemplate.copy(), Map.of(ITEM_REPLACEMENT_TAG, components.item()));

        Map<String, Component> parameterToDeathMessageComponent = new HashMap<>();
        parameterToDeathMessageComponent.put(DIED_ENTITY_REPLACEMENT_TAG, playerTemplate);
        parameterToDeathMessageComponent.put(KILLER_ENTITY_REPLACEMENT_TAG, components.killerEntity() != null ? entityTemplate : null);
        parameterToDeathMessageComponent.put(ITEM_REPLACEMENT_TAG, components.item() != null ? itemTemplate : null);
        parameterToDeathMessageComponent.putAll(buildPositionComponentParameters(entity));

        return Optional.of(applyParametersToTemplate(causeTemplate.copy(), parameterToDeathMessageComponent));
    }
}

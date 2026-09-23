package com.denisnumb.discord_chat_mod.chat;

import com.denisnumb.discord_chat_mod.config.ConfigProvider;
import com.denisnumb.discord_chat_mod.config.IConfigProvider;
import com.denisnumb.discord_chat_mod.markdown.MarkdownToken;
import com.denisnumb.discord_chat_mod.utils.ComponentUtils;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.denisnumb.discord_chat_mod.chat.template.TemplateParameterFactory.buildPositionComponentParameters;
import static com.denisnumb.discord_chat_mod.utils.DeathMessageUtils.*;
import static com.denisnumb.discord_chat_mod.chat.CustomChatTypeRegistry.*;
import static com.denisnumb.discord_chat_mod.chat.template.TemplateFactory.*;
import static com.denisnumb.discord_chat_mod.chat.template.TemplateParameters.*;
import static com.denisnumb.discord_chat_mod.chat.template.TemplateParameters.Translatable.*;
import static com.denisnumb.discord_chat_mod.utils.JavaUtils.mergeMaps;
import static com.denisnumb.discord_chat_mod.utils.JavaUtils.newLinkedHashMapOf;

public final class MinecraftMessageFormatter {
    private MinecraftMessageFormatter() {}

    private static Component buildAdvancementComponent(
            Component translatableTitle,
            Component translatableDescription,
            @Nullable MarkdownToken advancementStyle
    ) {
        Component hoverTitle = advancementStyle != null
                ? ComponentUtils.applyTokenStyleToComponent(advancementStyle, translatableTitle)
                : translatableTitle.copy();

        Component hoverContent = Component.empty()
                .append(hoverTitle)
                .append("\n")
                .append(translatableDescription);

        Component title = translatableTitle.copy()
                .withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(hoverContent)));

        return ComponentUtils.wrapInSquareBrackets(title);
    }

    public static Component getStyledAdvancementMessage(Player player, DisplayInfo displayInfo){
        IConfigProvider config = ConfigProvider.getConfig();

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

        List<MarkdownToken> template = parseConfigTemplateMarkdown(messageStringTemplate);
        MarkdownToken advancementStyle = parseTemplateParameterTokens(template, ADVANCEMENT).get(ADVANCEMENT);
        LinkedHashMap<String, Component> placeholders = newLinkedHashMapOf(
                Map.entry(PLAYER, player.getDisplayName()),
                Map.entry(ADVANCEMENT, buildAdvancementComponent(
                        displayInfo.getTitle(),
                        displayInfo.getDescription(),
                        advancementStyle
                ))
        );

        return getStyledTranslatableMessage(
                template,
                translationKey,
                placeholders,
                buildPositionComponentParameters(player)
        );
    }

    public static Component getStyledJoinedLeftMessage(Player player, boolean isJoin) {
        IConfigProvider config = ConfigProvider.getConfig();

        String messageStringTemplate = isJoin
                ? config.minecraftPlayerJoinedStyle()
                : config.minecraftPlayerLeftStyle();

        String translationKey = isJoin
                ? PLAYER_JOINED
                : PLAYER_LEFT;

        return getStyledTranslatableMessage(
                parseConfigTemplateMarkdown(messageStringTemplate),
                translationKey,
                newLinkedHashMapOf(Map.entry(PLAYER, player.getDisplayName())),
                buildPositionComponentParameters(player)
        );
    }

    public static Optional<Component> getStyledChatMessage(ResourceKey<@NotNull ChatType> chatType, MinecraftMessageContext components){
        IConfigProvider config = ConfigProvider.getConfig();
        String translationKey = null;

        String configTemplate = switch (chatType.identifier().getPath()) {
            case CHAT_PATH -> config.minecraftPlayerMessageStyle();
            case SAY_COMMAND_PATH -> config.minecraftSayCommandStyle();
            case TEAM_MSG_COMMAND_INCOMING_PATH -> config.minecraftTeamMessageReceivedStyle();
            case TEAM_MSG_COMMAND_OUTGOING_PATH -> config.minecraftTeamMessageSentStyle();
            case EMOTE_COMMAND_PATH -> config.minecraftMeCommandStyle();
            case MSG_COMMAND_INCOMING_PATH -> {
                translationKey = COMMANDS_MESSAGE_DISPLAY_INCOMING;
                yield config.minecraftTellMessageReceivedStyle();
            }
            case MSG_COMMAND_OUTGOING_PATH -> {
                translationKey = COMMANDS_MESSAGE_DISPLAY_OUTGOING;
                yield config.minecraftTellMessageSentStyle();
            }
            default -> null;
        };

        if (configTemplate == null)
            return Optional.empty();

        String[] params = getParametersByChatType(chatType);
        Component[] values = params.length == 3
                ? new Component[] { components.team(), components.player(), components.content() }
                : new Component[] { components.player(), components.content() };

        List<MarkdownToken> template = parseConfigTemplateMarkdown(configTemplate);
        LinkedHashMap<String, Component> parameterToComponent = new LinkedHashMap<>();
        for (int i = 0; i < params.length; i++) {
            parameterToComponent.put(params[i], values[i]);
        }

        if (translationKey != null) {
            return Optional.of(getStyledTranslatableMessage(
                    template,
                    translationKey,
                    parameterToComponent,
                    buildPositionComponentParameters(components.sender())
            ));
        }

        return Optional.of(applyParametersToTemplate(template, mergeMaps(
                parameterToComponent,
                buildPositionComponentParameters(components.sender())
        )));
    }

    private static final String DEATH_CAUSE_REPLACEMENT_TAG = "{death.cause}";
    private static final String DIED_ENTITY_REPLACEMENT_TAG = "{died.entity}";
    private static final String KILLER_ENTITY_REPLACEMENT_TAG = "{second.entity}";

    public static Component getStyledDeathMessage(DeathMessageComponents components, Entity entity) {
        IConfigProvider config = ConfigProvider.getConfig();

        String causeStyle = config.minecraftPlayerDeathCauseStyle().replace(DEATH_CAUSE, DEATH_CAUSE_REPLACEMENT_TAG);
        String diedEntityStyle = config.minecraftPlayerDeathNameStyle().replace(PLAYER, DIED_ENTITY_REPLACEMENT_TAG);
        String killerEntityStyle = config.minecraftPlayerDeathSecondEntityNameStyle().replace(SECOND_ENTITY, KILLER_ENTITY_REPLACEMENT_TAG);
        String itemStyle = config.minecraftPlayerDeathWeaponStyle();

        Component diedEntity = applyParametersToTemplate(parseConfigTemplateMarkdown(diedEntityStyle), Map.of(DIED_ENTITY_REPLACEMENT_TAG, components.diedEntity()));
        Component killerEntity = null;
        Component item = null;

        if (components.killerEntity() != null)
            killerEntity = applyParametersToTemplate(parseConfigTemplateMarkdown(killerEntityStyle), Map.of(KILLER_ENTITY_REPLACEMENT_TAG, components.killerEntity()));
        if (components.item() != null)
            item = applyParametersToTemplate(parseConfigTemplateMarkdown(itemStyle), Map.of(ITEM, components.item()));

        List<Object> args = new ArrayList<>();
        args.add(diedEntity);
        if (killerEntity != null)
            args.add(killerEntity);
        if (item != null)
            args.add(item);

        Component deathCause = Component.translatable(components.deathCauseLocaleKey(), args.toArray());

        Map<String, Component> parameters = new HashMap<>();
        parameters.put(DEATH_CAUSE_REPLACEMENT_TAG, deathCause);
        parameters.putAll(buildPositionComponentParameters(entity));

        return applyParametersToTemplate(parseConfigTemplateMarkdown(causeStyle), parameters);
    }
}

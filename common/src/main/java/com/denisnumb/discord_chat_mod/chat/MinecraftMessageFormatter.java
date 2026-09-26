package com.denisnumb.discord_chat_mod.chat;

import com.denisnumb.discord_chat_mod.chat.template.MessageTemplate;
import com.denisnumb.discord_chat_mod.chat.template.MessageTypes;
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

import static com.denisnumb.discord_chat_mod.utils.DeathMessageUtils.*;
import static com.denisnumb.discord_chat_mod.chat.CustomChatTypeRegistry.*;
import static com.denisnumb.discord_chat_mod.chat.template.TemplateParameter.*;

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

    public static Component getStyledAdvancementMessage(Player player, DisplayInfo displayInfo) {
        IConfigProvider config = ConfigProvider.getConfig();

        MessageTemplate<MessageTypes.AdvancementMessageType> template = switch (displayInfo.getType()) {
            case AdvancementType.TASK -> config.minecraftPlayerAdvancementTaskStyle();
            case AdvancementType.GOAL -> config.minecraftPlayerAdvancementGoalStyle();
            case AdvancementType.CHALLENGE -> config.minecraftPlayerAdvancementChallengeStyle();
        };

        MarkdownToken style = template.parseParameterStyles(Set.of(ADVANCEMENT)).get(ADVANCEMENT);
        Component advancement = buildAdvancementComponent(displayInfo.getTitle(), displayInfo.getDescription(), style);

        return template.applyParameters(new MessageTypes.AdvancementMessageType.Params(player, advancement));
    }

    public static Component getStyledJoinedLeftMessage(Player player, boolean isJoin) {
        IConfigProvider config = ConfigProvider.getConfig();

        return isJoin
                ? config.minecraftPlayerJoinedStyle().applyParameters(new MessageTypes.PlayerJoined.Params(player))
                : config.minecraftPlayerLeftStyle().applyParameters(new MessageTypes.PlayerLeft.Params(player));
    }

    @Nullable
    public static Component getStyledChatMessage(ResourceKey<@NotNull ChatType> chatType, MinecraftMessageContext ctx){
        IConfigProvider config = ConfigProvider.getConfig();

        final MessageTypes.PlayerMessage.Params playerMessageParams = new MessageTypes.PlayerMessage.Params(ctx);
        final MessageTypes.TeamMessage.Params teamMessageParams = new MessageTypes.TeamMessage.Params(ctx);

        return switch (chatType.identifier().getPath()) {
            case CHAT_PATH ->
                    config.minecraftPlayerMessageStyle().applyParameters(playerMessageParams);
            case SAY_COMMAND_PATH ->
                    config.minecraftSayCommandStyle().applyParameters(playerMessageParams);
            case EMOTE_COMMAND_PATH ->
                    config.minecraftMeCommandStyle().applyParameters(playerMessageParams);
            case TEAM_MSG_COMMAND_INCOMING_PATH ->
                    config.minecraftTeamMessageReceivedStyle().applyParameters(teamMessageParams);
            case TEAM_MSG_COMMAND_OUTGOING_PATH ->
                    config.minecraftTeamMessageSentStyle().applyParameters(teamMessageParams);
            case MSG_COMMAND_INCOMING_PATH ->
                    config.minecraftTellMessageReceivedStyle().applyParameters(new MessageTypes.TellIncomingMessage.Params(ctx));
            case MSG_COMMAND_OUTGOING_PATH ->
                    config.minecraftTellMessageSentStyle().applyParameters(new MessageTypes.TellOutgoingMessage.Params(ctx));
            default -> null;
        };
    }


    public static Component getStyledDeathMessage(DeathMessageComponents components, Entity entity) {
        IConfigProvider config = ConfigProvider.getConfig();

        List<Component> args = new ArrayList<>();
        args.add(config.minecraftPlayerDeathNameStyle().applyParameters(new MessageTypes.DiedEntity.Params(entity, components.diedEntity())));
        if (components.killerEntity() != null)
            args.add(config.minecraftPlayerDeathSecondEntityNameStyle().applyParameters(new MessageTypes.KillerEntity.Params(components.killerEntity())));
        if (components.killerWeapon() != null)
            args.add(config.minecraftPlayerDeathWeaponStyle().applyParameters(new MessageTypes.KillerWeapon.Params(components.killerWeapon())));

        Component deathCause = Component.translatable(components.deathCauseLocaleKey(), args.toArray());
        return config.minecraftPlayerDeathCauseStyle().applyParameters(new MessageTypes.DeathCause.Params(entity, deathCause));
    }
}

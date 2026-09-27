package com.denisnumb.discord_chat_mod.chat.template;

import com.denisnumb.discord_chat_mod.utils.JavaUtils;
import net.minecraft.network.chat.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.denisnumb.discord_chat_mod.chat.template.TemplateParameter.*;

public sealed interface MessageType permits
        MessageTypes.DiscordMessage,
        MessageTypes.PlayerMessage,
        MessageTypes.TeamMessage,
        MessageTypes.TellIncomingMessage,
        MessageTypes.TellOutgoingMessage,
        MessageTypes.AdvancementMessageType,
        MessageTypes.PlayerJoined,
        MessageTypes.PlayerLeft,
        MessageTypes.DeathCause,
        MessageTypes.DiedEntity,
        MessageTypes.KillerEntity,
        MessageTypes.KillerWeapon {
    Set<TemplatePlaceholder> baseParameters();

    default Optional<Translatable> translatable() {
        return baseParameters().stream()
                .filter(Translatable.class::isInstance)
                .map(Translatable.class::cast)
                .findFirst();
    }

    default Set<TemplatePlaceholder> availableParameters() {
        Set<TemplatePlaceholder> result = new HashSet<>(baseParameters());
        result.addAll(Set.of(HH, MM, SS));
        if (result.contains(PLAYER))
            result.addAll(Set.of(X, Y, Z, DIMENSION));
        translatable().ifPresent(result::add);
        return result;
    }

    interface MessageParamsBuilder<T extends MessageType> {
        Map<TemplatePlaceholder, Component> buildOwnParameters();

        default Map<TemplatePlaceholder, Component> build() {
            return JavaUtils.mergeMaps(buildOwnParameters(), TemplateParameterFactory.buildTimestampParameters());
        }
    }
}
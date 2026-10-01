package com.denisnumb.discord_chat_mod.discord.chat.template;

import com.denisnumb.discord_chat_mod.chat.template.TemplatePlaceholder;
import com.denisnumb.discord_chat_mod.config.configs.CommonConfig;
import com.denisnumb.discord_chat_mod.utils.JavaUtils;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.denisnumb.discord_chat_mod.chat.template.TemplateParameter.*;


public sealed interface TemplateType permits
        TemplateTypes.PlayerMessage,
        TemplateTypes.TellrawMessage,
        TemplateTypes.CommandLog,
        TemplateTypes.ImageMessage,
        TemplateTypes.PlayerJoined,
        TemplateTypes.PlayerLeft,
        TemplateTypes.DeathMessage,
        TemplateTypes.DeathCause,
        TemplateTypes.DiedEntity,
        TemplateTypes.KillerEntity,
        TemplateTypes.KillerWeapon,
        TemplateTypes.AdvancementTemplateType,
        TemplateTypes.ServerStarted,
        TemplateTypes.LocalServerStated,
        TemplateTypes.ServerClosed,
        TemplateTypes.PinnedStatusUnavailable,
        TemplateTypes.PinnedStatusAvailable,
        TemplateTypes.PinnedStatusOnlinePlayers,
        TemplateTypes.GuildForwardedMessage,
        TemplateTypes.GuildForwardedMessageWebhookUsername,
        TemplateTypes.PlayerListNickname {
    Set<TemplatePlaceholder> baseParameters();

    default Optional<Translatable> translatable() {
        return baseParameters().stream()
                .filter(Translatable.class::isInstance)
                .map(Translatable.class::cast)
                .findFirst();
    }

    default Set<TemplatePlaceholder> availableParameters() {
        Set<TemplatePlaceholder> result = new HashSet<>(baseParameters());
        result.addAll(Set.of(TIMESTAMP, DATETIME));
        if (result.contains(PLAYER))
            result.addAll(Set.of(X, Y, Z, DIMENSION, PLAYER_AVATAR_URL));
        translatable().ifPresent(result::add);
        return result;
    }

    interface ParamsBuilder<T extends TemplateType> {
        Map<TemplatePlaceholder, String> buildOwnParameters();

        default Map<TemplatePlaceholder, String> build() {
            OffsetDateTime now = JavaUtils.getDateTimeWithUtcOffset(CommonConfig.UTC_OFFSET_HOURS.get());

            return JavaUtils.mergeMaps(
                    buildOwnParameters(),
                    Map.of(
                            TIMESTAMP, String.valueOf(now.toEpochSecond()),
                            DATETIME, now.format(DateTimeFormatter.ISO_INSTANT)
                    )
            );
        }
    }
}

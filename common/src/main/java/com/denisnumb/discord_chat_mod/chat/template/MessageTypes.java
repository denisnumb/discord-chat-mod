package com.denisnumb.discord_chat_mod.chat.template;

import com.denisnumb.discord_chat_mod.chat.MinecraftMessageContext;
import com.denisnumb.discord_chat_mod.utils.JavaUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.denisnumb.discord_chat_mod.chat.template.TemplateParameter.*;
import static com.denisnumb.discord_chat_mod.chat.template.TemplateParameterFactory.buildPositionComponentParameters;

public final class MessageTypes {
    private MessageTypes() {}

    public record DiscordMessage() implements MessageType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(GUILD, MEMBER, MESSAGE);

        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }
        public Optional<TemplateParameter.Translatable> translatable() { return Optional.empty(); }

        public record Params(Component guild, Component member, Component message) implements MessageParamsBuilder<DiscordMessage> {
            public Map<TemplatePlaceholder, Component> buildOwnParameters() {
                return Map.of(GUILD, guild, MEMBER, member, MESSAGE, message);
            }
        }
    }

    public record PlayerMessage() implements MessageType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(PLAYER, MESSAGE);

        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }
        public Optional<TemplateParameter.Translatable> translatable() { return Optional.empty(); }

        public record Params(MinecraftMessageContext ctx) implements MessageParamsBuilder<PlayerMessage> {
            public Map<TemplatePlaceholder, Component> buildOwnParameters() {
                return JavaUtils.mergeMaps(
                        Map.of(PLAYER, ctx.player(), MESSAGE, ctx.content()),
                        buildPositionComponentParameters(ctx.entity())
                );
            }
        }
    }

    public record TeamMessage() implements MessageType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(TEAM, PLAYER, MESSAGE);

        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }
        public Optional<TemplateParameter.Translatable> translatable() { return Optional.empty(); }

        public record Params(MinecraftMessageContext ctx) implements MessageParamsBuilder<TeamMessage> {
            public Map<TemplatePlaceholder, Component> buildOwnParameters() {
                return JavaUtils.mergeMaps(
                        Map.of(TEAM, ctx.team(), PLAYER, ctx.player(), MESSAGE, ctx.content()),
                        buildPositionComponentParameters(ctx.entity())
                );
            }
        }
    }

    public record TellOutgoingMessage() implements MessageType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(Translatable.COMMANDS_MESSAGE_DISPLAY_OUTGOING, RECEIVER, MESSAGE);

        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }
        public Optional<TemplateParameter.Translatable> translatable() { return Optional.of(Translatable.COMMANDS_MESSAGE_DISPLAY_OUTGOING); }

        public record Params(MinecraftMessageContext ctx) implements MessageParamsBuilder<TellOutgoingMessage> {
            public Map<TemplatePlaceholder, Component> buildOwnParameters() {
                return JavaUtils.mergeMaps(
                        Map.of(RECEIVER, ctx.player(), MESSAGE, ctx.content()),
                        buildPositionComponentParameters(ctx.entity())
                );
            }
        }
    }

    public record TellIncomingMessage() implements MessageType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(Translatable.COMMANDS_MESSAGE_DISPLAY_INCOMING, SENDER, MESSAGE);

        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }
        public Optional<TemplateParameter.Translatable> translatable() { return Optional.of(Translatable.COMMANDS_MESSAGE_DISPLAY_INCOMING); }

        public record Params(MinecraftMessageContext ctx) implements MessageParamsBuilder<TellIncomingMessage> {
            public Map<TemplatePlaceholder, Component> buildOwnParameters() {
                return JavaUtils.mergeMaps(
                        Map.of(SENDER, ctx.player(), MESSAGE, ctx.content()),
                        buildPositionComponentParameters(ctx.entity())
                );
            }
        }
    }

    public record PlayerJoined() implements MessageType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(PLAYER, Translatable.PLAYER_JOINED);

        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }
        public Optional<TemplateParameter.Translatable> translatable() { return Optional.of(Translatable.PLAYER_JOINED); }

        public record Params(Entity player) implements MessageParamsBuilder<PlayerJoined> {
            public Map<TemplatePlaceholder, Component> buildOwnParameters() {
                return JavaUtils.mergeMaps(
                        Map.of(PLAYER, player.getDisplayName()),
                        buildPositionComponentParameters(player)
                );
            }
        }
    }

    public record PlayerLeft() implements MessageType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(PLAYER, Translatable.PLAYER_LEFT);

        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }
        public Optional<TemplateParameter.Translatable> translatable() { return Optional.of(Translatable.PLAYER_LEFT); }

        public record Params(Entity player) implements MessageParamsBuilder<PlayerLeft> {
            public Map<TemplatePlaceholder, Component> buildOwnParameters() {
                return JavaUtils.mergeMaps(
                        Map.of(PLAYER, player.getDisplayName()),
                        buildPositionComponentParameters(player)
                );
            }
        }
    }

    public record DeathCause() implements MessageType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(DEATH_CAUSE, X, Y, Z, DIMENSION);

        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }
        public Optional<TemplateParameter.Translatable> translatable() { return Optional.empty(); }

        public record Params(Entity diedEntity, Component deathCause) implements MessageParamsBuilder<DeathCause> {
            public Map<TemplatePlaceholder, Component> buildOwnParameters() {
                return JavaUtils.mergeMaps(
                        Map.of(DEATH_CAUSE, deathCause),
                        buildPositionComponentParameters(diedEntity)
                );
            }
        }
    }

    public record DiedEntity() implements MessageType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(PLAYER);

        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }
        public Optional<TemplateParameter.Translatable> translatable() { return Optional.empty(); }

        public record Params(Entity diedEntity, Component displayName) implements MessageParamsBuilder<DiedEntity> {
            public Map<TemplatePlaceholder, Component> buildOwnParameters() {
                return JavaUtils.mergeMaps(
                        Map.of(PLAYER, displayName),
                        buildPositionComponentParameters(diedEntity)
                );
            }
        }
    }

    public record KillerEntity() implements MessageType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(SECOND_ENTITY);

        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }
        public Optional<TemplateParameter.Translatable> translatable() { return Optional.empty(); }

        public record Params(Component killerEntity) implements MessageParamsBuilder<KillerEntity> {
            public Map<TemplatePlaceholder, Component> buildOwnParameters() {
                return Map.of(SECOND_ENTITY, killerEntity);
            }
        }
    }

    public record KillerWeapon() implements MessageType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(ITEM);

        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }
        public Optional<TemplateParameter.Translatable> translatable() { return Optional.empty(); }

        public record Params(Component killerWeapon) implements MessageParamsBuilder<KillerWeapon> {
            public Map<TemplatePlaceholder, Component> buildOwnParameters() {
                return Map.of(ITEM, killerWeapon);
            }
        }
    }

    public sealed interface AdvancementMessageType extends MessageType
            permits MessageTypes.AdvancementTask, MessageTypes.AdvancementGoal, MessageTypes.AdvancementChallenge {
        default Set<TemplatePlaceholder> baseParameters() {
            return Set.of(PLAYER, ADVANCEMENT, translatable().orElseThrow());
        }

        record Params(Entity player, Component advancement) implements MessageParamsBuilder<AdvancementMessageType> {
            public Map<TemplatePlaceholder, Component> buildOwnParameters() {
                return JavaUtils.mergeMaps(
                        Map.of(PLAYER, player.getDisplayName(), ADVANCEMENT, advancement),
                        buildPositionComponentParameters(player)
                );
            }
        }
    }

    public record AdvancementTask() implements AdvancementMessageType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(PLAYER, ADVANCEMENT, Translatable.ADVANCEMENT_TASK);

        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }
        public Optional<TemplateParameter.Translatable> translatable() { return Optional.of(Translatable.ADVANCEMENT_TASK); }


    }

    public record AdvancementGoal() implements AdvancementMessageType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(PLAYER, ADVANCEMENT, Translatable.ADVANCEMENT_GOAL);

        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }
        public Optional<TemplateParameter.Translatable> translatable() { return Optional.of(Translatable.ADVANCEMENT_GOAL); }

    }

    public record AdvancementChallenge() implements AdvancementMessageType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(PLAYER, ADVANCEMENT, Translatable.ADVANCEMENT_CHALLENGE);

        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }
        public Optional<TemplateParameter.Translatable> translatable() { return Optional.of(Translatable.ADVANCEMENT_CHALLENGE); }
    }
}

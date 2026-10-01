package com.denisnumb.discord_chat_mod.discord.chat.template;

import com.denisnumb.discord_chat_mod.chat.template.*;
import com.denisnumb.discord_chat_mod.locale.DiscordLocaleProvider;
import com.denisnumb.discord_chat_mod.utils.JavaUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

import static com.denisnumb.discord_chat_mod.chat.template.TemplateParameter.*;

public final class TemplateTypes {
    private TemplateTypes() {}

    public record EmptyParams() implements TemplateType.ParamsBuilder<TemplateType> {
        public Map<TemplatePlaceholder, String> buildOwnParameters() {
            return Map.of();
        }
    }

    public record PlayerMessage() implements TemplateType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(PLAYER, MESSAGE);
        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }

        public static Params ofParams(Entity player, String message) {
            return new Params(player, player.getDisplayName().getString(), message);
        }

        public static Params ofParams(CommandSourceStack source, String message) {
            return source.getPlayer() == null
                    ? new Params(null, source.getDisplayName().getString(), message)
                    : new Params(source.getEntity(), source.getDisplayName().getString(), message);
        }

        public record Params(@Nullable Entity player, String playerDisplayName, String message) implements ParamsBuilder<PlayerMessage> {
            public Map<TemplatePlaceholder, String> buildOwnParameters() {
                return JavaUtils.mergeMaps(
                        Map.of(MESSAGE, message),
                        TemplateParameterFactory.buildPlayerParameters(playerDisplayName, player)
                );
            }
        }
    }

    public record TellrawMessage() implements TemplateType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(MESSAGE);
        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }

        public record Params(String message) implements ParamsBuilder<TellrawMessage> {
            public Map<TemplatePlaceholder, String> buildOwnParameters() {
                return Map.of(MESSAGE, message);
            }
        }
    }

    public record CommandLog() implements TemplateType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(PLAYER, COMMAND);
        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }

        public record Params(Entity player, String command) implements ParamsBuilder<CommandLog> {
            public Map<TemplatePlaceholder, String> buildOwnParameters() {
                return JavaUtils.mergeMaps(
                        Map.of(MESSAGE, command),
                        TemplateParameterFactory.buildPlayerParameters(player)
                );
            }
        }
    }

    public record ImageMessage() implements TemplateType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(PLAYER, IMAGE_URL);
        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }

        public record Params(Entity player, String imageUrl) implements ParamsBuilder<ImageMessage> {
            public Map<TemplatePlaceholder, String> buildOwnParameters() {
                return JavaUtils.mergeMaps(
                        Map.of(IMAGE_URL, imageUrl),
                        TemplateParameterFactory.buildPlayerParameters(player)
                );
            }
        }
    }

    public record PlayerJoined() implements TemplateType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(PLAYER, Translatable.PLAYER_JOINED);
        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }

        public record Params(Entity player) implements ParamsBuilder<PlayerJoined> {
            public Map<TemplatePlaceholder, String> buildOwnParameters() {
                return TemplateParameterFactory.buildPlayerParameters(player);
            }
        }
    }

    public record PlayerLeft() implements TemplateType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(PLAYER, Translatable.PLAYER_LEFT);
        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }

        public record Params(Entity player) implements ParamsBuilder<PlayerLeft> {
            public Map<TemplatePlaceholder, String> buildOwnParameters() {
                return TemplateParameterFactory.buildPlayerParameters(player);
            }
        }
    }

    public record DeathMessage() implements TemplateType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(PLAYER, DEATH_MESSAGE);
        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }

        public record Params(Entity diedEntity, String diedEntityDisplayName, String deathMessage) implements ParamsBuilder<DeathMessage> {
            public Map<TemplatePlaceholder, String> buildOwnParameters() {
                return JavaUtils.mergeMaps(
                        Map.of(DEATH_MESSAGE, deathMessage),
                        TemplateParameterFactory.buildPlayerParameters(diedEntityDisplayName, diedEntity)
                );
            }
        }
    }

    public record DeathCause() implements TemplateType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(DEATH_CAUSE);
        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }

        public record Params(String deathCause) implements ParamsBuilder<DeathCause> {
            public Map<TemplatePlaceholder, String> buildOwnParameters() {
                return Map.of(DEATH_CAUSE, deathCause);
            }
        }
    }

    public record DiedEntity() implements TemplateType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(PLAYER);
        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }

        public record Params(Entity diedEntity, String displayName) implements ParamsBuilder<DiedEntity> {
            public Map<TemplatePlaceholder, String> buildOwnParameters() {
                return TemplateParameterFactory.buildPlayerParameters(displayName, diedEntity);
            }
        }
    }

    public record KillerEntity() implements TemplateType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(SECOND_ENTITY);
        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }

        public record Params(String killerEntity) implements ParamsBuilder<KillerEntity> {
            public Map<TemplatePlaceholder, String> buildOwnParameters() {
                return Map.of(SECOND_ENTITY, killerEntity);
            }
        }
    }

    public record KillerWeapon() implements TemplateType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(ITEM);
        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }

        public record Params(String killerWeapon) implements ParamsBuilder<KillerWeapon> {
            public Map<TemplatePlaceholder, String> buildOwnParameters() {
                return Map.of(ITEM, killerWeapon);
            }
        }
    }

    public sealed interface AdvancementTemplateType extends TemplateType permits
            AdvancementTask,
            AdvancementGoal,
            AdvancementChallenge {
        record Params(Entity player, String advancement, String description, String iconUrl) implements ParamsBuilder<AdvancementTemplateType> {
            public Map<TemplatePlaceholder, String> buildOwnParameters() {
                return JavaUtils.mergeMaps(
                        Map.of(ADVANCEMENT, advancement, DESCRIPTION, description, ICON_URL, iconUrl),
                        TemplateParameterFactory.buildPlayerParameters(player)
                );
            }
        }
    }

    public record AdvancementTask() implements AdvancementTemplateType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(PLAYER, ADVANCEMENT, DESCRIPTION, ICON_URL, Translatable.ADVANCEMENT_TASK);
        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }
    }

    public record AdvancementGoal() implements AdvancementTemplateType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(PLAYER, ADVANCEMENT, DESCRIPTION, ICON_URL, Translatable.ADVANCEMENT_GOAL);
        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }
    }

    public record AdvancementChallenge() implements AdvancementTemplateType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(PLAYER, ADVANCEMENT, DESCRIPTION, ICON_URL, Translatable.ADVANCEMENT_CHALLENGE);
        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }
    }

    public record LocalServerStated() implements TemplateType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(Translatable.LOCAL_SERVER_STARTED, SERVER_PORT);
        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }

        public record Params(int serverPort) implements ParamsBuilder<LocalServerStated> {
            public Map<TemplatePlaceholder, String> buildOwnParameters() {
                return Map.of(
                        TemplateParameter.Translatable.LOCAL_SERVER_STARTED, DiscordLocaleProvider.Server.localStarted(serverPort),
                        TemplateParameter.SERVER_PORT, String.valueOf(serverPort)
                );
            }
        }
    }

    public record ServerStarted() implements TemplateType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(Translatable.SERVER_STARTED);
        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }
    }

    public record ServerClosed() implements TemplateType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(Translatable.SERVER_CLOSED);
        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }
    }

    public record PinnedStatusUnavailable() implements TemplateType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(Translatable.SERVER_UNAVAILABLE);
        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }
    }

    public record PinnedStatusAvailable() implements TemplateType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(Translatable.SERVER_AVAILABLE);
        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }
    }

    public record PinnedStatusOnlinePlayers() implements TemplateType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(Translatable.ONLINE_PLAYERS, PLAYER_LIST, PLAYER_COUNT, MAX_PLAYERS);
        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }

        public record Params(String playerList, int playerCount, int maxPlayers) implements ParamsBuilder<PinnedStatusOnlinePlayers> {
            public Map<TemplatePlaceholder, String> buildOwnParameters() {
                return Map.of(
                        TemplateParameter.Translatable.ONLINE_PLAYERS, DiscordLocaleProvider.Server.Status.onlinePlayers(playerCount, maxPlayers),
                        TemplateParameter.PLAYER_LIST, playerList,
                        TemplateParameter.PLAYER_COUNT, String.valueOf(playerCount),
                        TemplateParameter.MAX_PLAYERS, String.valueOf(maxPlayers)
                );
            }
        }
    }

    public record GuildForwardedMessage() implements TemplateType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(Translatable.FORWARDED_MESSAGE, MEMBER, USER, AVATAR_URL, GUILD, MESSAGE);
        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }

        public record Params(Member member, User user, Guild guild, String message) implements ParamsBuilder<GuildForwardedMessage> {
            public Map<TemplatePlaceholder, String> buildOwnParameters() {
                return Map.of(
                        Translatable.FORWARDED_MESSAGE, DiscordLocaleProvider.Discord.forwardedGuildMessage(member.getEffectiveName(), guild.getName()),
                        MEMBER, member.getEffectiveName(),
                        USER, user.getEffectiveName(),
                        AVATAR_URL, member.getEffectiveAvatarUrl(),
                        GUILD, guild.getName(),
                        MESSAGE, message
                );
            }
        }
    }

    public record GuildForwardedMessageWebhookUsername() implements TemplateType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(MEMBER, USER, GUILD);
        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }

        public record Params(Member member, User user, Guild guild) implements ParamsBuilder<GuildForwardedMessageWebhookUsername> {
            public Map<TemplatePlaceholder, String> buildOwnParameters() {
                return Map.of(
                        MEMBER, member.getEffectiveName(),
                        USER, user.getEffectiveName(),
                        GUILD, guild.getName()
                );
            }
        }
    }

    public record PlayerListNickname() implements TemplateType {
        public static final Set<TemplatePlaceholder> BASE_PARAMETERS = Set.of(PLAYER, COUNTER);
        public Set<TemplatePlaceholder> baseParameters() { return BASE_PARAMETERS; }

        public record Params(String player, int counter) implements ParamsBuilder<PlayerListNickname> {
            public Map<TemplatePlaceholder, String> buildOwnParameters() {
                return Map.of(PLAYER, player, COUNTER, String.valueOf(counter));
            }
        }
    }
}

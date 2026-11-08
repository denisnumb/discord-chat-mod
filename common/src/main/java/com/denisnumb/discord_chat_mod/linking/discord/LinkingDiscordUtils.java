package com.denisnumb.discord_chat_mod.linking.discord;

import com.denisnumb.discord_chat_mod.config.configs.DiscordGuildsConfig;
import com.denisnumb.discord_chat_mod.discord.DiscordChannelRegistry;
import com.denisnumb.discord_chat_mod.discord.model.DiscordGuildContext;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import com.denisnumb.discord_chat_mod.locale.MinecraftLocaleProvider;
import com.denisnumb.discord_chat_mod.utils.MinecraftUtils;
import com.denisnumb.discord_chat_mod.utils.NameComponentUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.isDiscordConnected;
import static com.denisnumb.discord_chat_mod.DiscordChatMod.jda;

public final class LinkingDiscordUtils {
    private LinkingDiscordUtils() {}

    private static final Set<String> REPORTED_PROBLEMS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static List<DiscordGuildContext> getGuilds() {
        return isDiscordConnected() ? DiscordChannelRegistry.getAllContexts() : List.of();
    }

    public static Optional<DiscordGuildContext> getGuild(@Nullable Guild guild) {
        if (guild == null || !isDiscordConnected())
            return Optional.empty();

        return Optional.ofNullable(DiscordChannelRegistry.getContext(guild.getId()));
    }

    public static List<String> getUnavailableGuildIds() {
        List<DiscordGuildsConfig.DiscordGuildConfig> configs = DiscordGuildsConfig.discordGuildConfigs;
        if (configs == null)
            return List.of();

        return configs.stream()
                .map(DiscordGuildsConfig.DiscordGuildConfig::guildId)
                .filter(id -> !id.isEmpty() && DiscordChannelRegistry.getContext(id) == null)
                .distinct()
                .toList();
    }

    public static boolean isConfiguredGuild(@Nullable Guild guild) {
        return getGuild(guild).isPresent();
    }

    public static boolean anyGuildConfig(Predicate<DiscordGuildsConfig.AccountLinkingGuildConfig> predicate) {
        List<DiscordGuildsConfig.DiscordGuildConfig> configs = DiscordGuildsConfig.discordGuildConfigs;
        return configs != null && configs.stream()
                .map(DiscordGuildsConfig.DiscordGuildConfig::accountLinking)
                .anyMatch(predicate);
    }

    public static boolean isMemberOfAnyGuild(String discordId) {
        return getGuilds().stream().anyMatch(context -> context.getMember(discordId) != null);
    }

    public static String getGuildNames() {
        String names = getGuilds().stream()
                .map(context -> context.guild.getName())
                .collect(Collectors.joining(", "));

        return names.isEmpty() ? "Discord" : names;
    }

    public static Optional<Role> resolveConfiguredRole(DiscordGuildContext context, String idOrName) {
        Optional<Role> role = resolveRole(context.guild, idOrName);
        if (role.isEmpty())
            errorOnce("role_not_found:" + context.guild.getId() + ":" + idOrName,
                    MinecraftLocaleProvider.Linking.Error.roleNotFound(idOrName, context.guild.getName()));
        return role;
    }

    public static Optional<Role> resolveRole(Guild guild, String idOrName) {
        if (idOrName == null || idOrName.isBlank())
            return Optional.empty();

        if (idOrName.chars().allMatch(Character::isDigit)) {
            Role byId = guild.getRoleById(idOrName);
            if (byId != null)
                return Optional.of(byId);
        }

        return guild.getRolesByName(idOrName, true).stream().findFirst();
    }

    public static boolean memberHasRole(Member member, String idOrName) {
        return member.getRoles().stream()
                .anyMatch(role -> role.getId().equals(idOrName) || role.getName().equalsIgnoreCase(idOrName));
    }

    public static boolean canManageRole(Guild guild, Role role) {
        Member self = guild.getSelfMember();
        if (!self.hasPermission(Permission.MANAGE_ROLES)) {
            errorOnce("manage_roles:" + guild.getId(), MinecraftLocaleProvider.Linking.Error.missingManageRoles(guild.getName()));
            return false;
        }
        if (!self.canInteract(role) || role.isManaged() || role.isPublicRole()) {
            errorOnce("manage_role:" + role.getId(), MinecraftLocaleProvider.Linking.Error.cannotManageRole(role.getName(), guild.getName()));
            return false;
        }
        return true;
    }

    public static boolean canChangeNickname(Member member) {
        Member self = member.getGuild().getSelfMember();
        if (!self.hasPermission(Permission.NICKNAME_MANAGE)) {
            errorOnce("manage_nicknames:" + member.getGuild().getId(), MinecraftLocaleProvider.Linking.Error.missingManageNicknames(member.getGuild().getName()));
            return false;
        }
        return !member.isOwner() && self.canInteract(member);
    }

    public static MutableComponent describeUser(String discordId) {
        for (DiscordGuildContext context : getGuilds()) {
            Member member = context.getMember(discordId);
            if (member != null)
                return NameComponentUtils.discordMember(member);
        }

        JDA currentJda = jda;
        User user = currentJda == null ? null : currentJda.getUserById(discordId);
        return user == null ? Component.literal(discordId) : NameComponentUtils.discordUser(user);
    }

    public static void clearReportedProblems() {
        REPORTED_PROBLEMS.clear();
    }

    public static void errorOnce(String key, Component message) {
        if (REPORTED_PROBLEMS.add(key))
            MinecraftUtils.logErrorToServer(message);
    }

    public static void warnOnce(String key, Component message) {
        if (REPORTED_PROBLEMS.add(key))
            MinecraftUtils.logWarnToServer(message);
    }
}

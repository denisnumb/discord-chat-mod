package com.denisnumb.discord_chat_mod.linking.features;

import com.denisnumb.discord_chat_mod.linking.AccountLinkListener;
import com.denisnumb.discord_chat_mod.linking.AccountLinkService;
import com.denisnumb.discord_chat_mod.linking.AccountLinking;
import com.denisnumb.discord_chat_mod.linking.discord.LinkingDiscordUtils;
import com.denisnumb.discord_chat_mod.discord.model.DiscordGuildContext;
import com.denisnumb.discord_chat_mod.linking.model.AccountLink;
import com.denisnumb.discord_chat_mod.linking.model.UnlinkReason;
import com.denisnumb.discord_chat_mod.locale.MinecraftLocaleProvider;
import com.denisnumb.discord_chat_mod.utils.NameComponentUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.Optional;

import static com.denisnumb.discord_chat_mod.utils.MinecraftUtils.logErrorToServer;


public final class LinkedRoleFeature implements AccountLinkListener {
    @Override
    public void onLinked(AccountLink link) {
        if (!AccountLinking.isEnabled())
            return;

        for (DiscordGuildContext context : LinkingDiscordUtils.getGuilds()) {
            Member member = context.getMember(link.discordId());
            if (member != null)
                getRole(context).ifPresent(role -> addRole(context.guild, member, role));
        }
    }

    @Override
    public void onUnlinked(AccountLink link, UnlinkReason reason) {
        if (!AccountLinking.isEnabled() || AccountLinkService.isLinked(link.discordId()))
            return;

        for (DiscordGuildContext context : LinkingDiscordUtils.getGuilds()) {
            Member member = context.getMember(link.discordId());
            if (member != null)
                getRole(context).ifPresent(role -> removeRole(context.guild, member, role));
        }
    }

    public static void onMemberJoin(DiscordGuildContext context, Member member) {
        if (AccountLinking.isEnabled() && AccountLinkService.isLinked(member.getId()))
            getRole(context).ifPresent(role -> addRole(context.guild, member, role));
    }

    public static void reconcile(DiscordGuildContext context) {
        if (!AccountLinking.isEnabled())
            return;

        getRole(context).ifPresent(role -> {
            for (String discordId : AccountLinkService.getAllDiscordIds()) {
                Member member = context.getMember(discordId);
                if (member != null)
                    addRole(context.guild, member, role);
            }
        });
    }

    private static Optional<Role> getRole(DiscordGuildContext context) {
        String roleKey = context.accountLinking.linkedRole();
        if (roleKey.isEmpty())
            return Optional.empty();

        return LinkingDiscordUtils.resolveConfiguredRole(context, roleKey)
                .filter(role -> LinkingDiscordUtils.canManageRole(context.guild, role));
    }

    private static void addRole(Guild guild, Member member, Role role) {
        if (member.getRoles().contains(role))
            return;
        guild.addRoleToMember(member, role).queue(
                null,
                error -> logErrorToServer(MinecraftLocaleProvider.Linking.Error.addRole(role.getName(), NameComponentUtils.discordMember(member), error.getMessage()))
        );
    }

    private static void removeRole(Guild guild, Member member, Role role) {
        if (!member.getRoles().contains(role))
            return;
        guild.removeRoleFromMember(member, role).queue(
                null,
                error -> logErrorToServer(MinecraftLocaleProvider.Linking.Error.removeRole(role.getName(), NameComponentUtils.discordMember(member), error.getMessage()))
        );
    }
}

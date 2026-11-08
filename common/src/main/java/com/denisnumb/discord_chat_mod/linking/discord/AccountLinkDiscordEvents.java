package com.denisnumb.discord_chat_mod.linking.discord;

import com.denisnumb.discord_chat_mod.linking.AccountLinking;
import com.denisnumb.discord_chat_mod.discord.model.DiscordGuildContext;
import com.denisnumb.discord_chat_mod.linking.features.GuildMembershipFeature;
import com.denisnumb.discord_chat_mod.linking.features.LinkedRoleFeature;
import com.denisnumb.discord_chat_mod.linking.features.NicknameSyncFeature;
import com.denisnumb.discord_chat_mod.linking.features.RoleSyncFeature;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public final class AccountLinkDiscordEvents extends ListenerAdapter {
    private static Optional<DiscordGuildContext> findGuild(Guild guild) {
        return AccountLinking.isEnabled()
                ? LinkingDiscordUtils.getGuild(guild)
                : Optional.empty();
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        findGuild(event.getGuild()).ifPresent(context -> {
            LinkedRoleFeature.onMemberJoin(context, event.getMember());
            NicknameSyncFeature.onMemberJoin(context, event.getMember());
            RoleSyncFeature.onMembershipChanged(event.getMember().getId());
        });
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        findGuild(event.getGuild()).ifPresent(context ->
                GuildMembershipFeature.onMemberLeave(event.getUser().getId())
        );
    }

    @Override
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
        findGuild(event.getGuild()).ifPresent(context ->
                RoleSyncFeature.onMemberRolesChanged(context, event.getMember(), event.getRoles(), List.of())
        );
    }

    @Override
    public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {
        findGuild(event.getGuild()).ifPresent(context ->
                RoleSyncFeature.onMemberRolesChanged(context, event.getMember(), List.of(), event.getRoles())
        );
    }
}

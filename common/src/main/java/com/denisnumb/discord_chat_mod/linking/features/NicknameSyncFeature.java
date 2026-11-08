package com.denisnumb.discord_chat_mod.linking.features;

import com.denisnumb.discord_chat_mod.discord.chat.template.TemplateTypes;
import com.denisnumb.discord_chat_mod.linking.AccountLinkListener;
import com.denisnumb.discord_chat_mod.linking.AccountLinkService;
import com.denisnumb.discord_chat_mod.linking.AccountLinking;
import com.denisnumb.discord_chat_mod.linking.discord.LinkingDiscordUtils;
import com.denisnumb.discord_chat_mod.discord.model.DiscordGuildContext;
import com.denisnumb.discord_chat_mod.linking.model.AccountLink;
import com.denisnumb.discord_chat_mod.linking.model.UnlinkReason;
import com.denisnumb.discord_chat_mod.locale.MinecraftLocaleProvider;
import com.denisnumb.discord_chat_mod.utils.NameComponentUtils;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static com.denisnumb.discord_chat_mod.utils.MinecraftUtils.logErrorToServer;

public final class NicknameSyncFeature implements AccountLinkListener {
    private static final int MAX_NICKNAME_LENGTH = 32;

    private static List<DiscordGuildContext> getNicknameSyncGuilds() {
        if (!AccountLinking.isEnabled())
            return List.of();
        return LinkingDiscordUtils.getGuilds().stream()
                .filter(context -> context.accountLinking.nicknameSync())
                .toList();
    }

    @Override
    public void onLinked(AccountLink link) {
        update(link);
    }

    @Override
    public void onUnlinked(AccountLink link, UnlinkReason reason) {
        if (reason == UnlinkReason.GUILD_LEFT)
            return;

        List<AccountLink> remaining = AccountLinkService.getByDiscordId(link.discordId());
        if (!remaining.isEmpty()) {
            update(remaining.getFirst());
            return;
        }

        for (DiscordGuildContext context : getNicknameSyncGuilds()) {
            Member member = context.getMember(link.discordId());
            if (member != null && Objects.equals(member.getNickname(), formatNickname(context, link, member)))
                setNickname(member, null);
        }
    }

    public static void update(AccountLink link) {
        List<AccountLink> links = AccountLinkService.getByDiscordId(link.discordId());
        if (links.isEmpty() || !links.getFirst().uuid().equals(link.uuid()))
            return;

        for (DiscordGuildContext context : getNicknameSyncGuilds()) {
            Member member = context.getMember(link.discordId());
            if (member != null)
                applyNickname(context, member, links.getFirst());
        }
    }

    public static void onMemberJoin(DiscordGuildContext context, Member member) {
        if (!AccountLinking.isEnabled() || !context.accountLinking.nicknameSync())
            return;

        List<AccountLink> links = AccountLinkService.getByDiscordId(member.getId());
        if (!links.isEmpty())
            applyNickname(context, member, links.getFirst());
    }

    public static void reconcile(DiscordGuildContext context) {
        if (!AccountLinking.isEnabled() || !context.accountLinking.nicknameSync())
            return;

        for (String discordId : AccountLinkService.getAllDiscordIds()) {
            Member member = context.getMember(discordId);
            List<AccountLink> links = AccountLinkService.getByDiscordId(discordId);
            if (member != null && !links.isEmpty())
                applyNickname(context, member, links.getFirst());
        }
    }

    private static void applyNickname(DiscordGuildContext context, Member member, AccountLink link) {
        String nickname = formatNickname(context, link, member);
        if (!nickname.equals(member.getNickname()))
            setNickname(member, nickname);
    }

    private static String formatNickname(DiscordGuildContext context, AccountLink link, Member member) {
        String nickname = context.accountLinking.nicknameTemplate()
                .applyParameters(new TemplateTypes.LinkedMemberNickname.Params(link.name(), member.getUser()))
                .trim();

        if (nickname.isEmpty())
            nickname = link.name();
        return nickname.length() > MAX_NICKNAME_LENGTH
                ? nickname.substring(0, MAX_NICKNAME_LENGTH)
                : nickname;
    }

    private static void setNickname(Member member, @Nullable String nickname) {
        if (!LinkingDiscordUtils.canChangeNickname(member))
            return;

        member.getGuild().modifyNickname(member, nickname).queue(
                null,
                error -> logErrorToServer(MinecraftLocaleProvider.Linking.Error.changeNickname(
                        NameComponentUtils.discordMember(member), member.getGuild().getName(), error.getMessage()))
        );
    }
}

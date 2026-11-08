package com.denisnumb.discord_chat_mod.linking.features;

import com.denisnumb.discord_chat_mod.config.configs.AccountLinkingConfig;
import com.denisnumb.discord_chat_mod.linking.AccountLinkService;
import com.denisnumb.discord_chat_mod.linking.AccountLinking;
import com.denisnumb.discord_chat_mod.linking.discord.LinkingDiscordUtils;
import com.denisnumb.discord_chat_mod.discord.model.DiscordGuildContext;
import com.denisnumb.discord_chat_mod.linking.model.UnlinkReason;
import com.denisnumb.discord_chat_mod.locale.MinecraftLocaleProvider;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.List;
import java.util.Set;

import static com.denisnumb.discord_chat_mod.utils.MinecraftUtils.logWarnToServer;


public final class GuildMembershipFeature {
    private GuildMembershipFeature() {}

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MASS_UNLINK_MIN_COUNT = 5;

    public static void onMemberLeave(String discordId) {
        if (!AccountLinking.isEnabled() || !AccountLinkService.isLinked(discordId))
            return;

        if (AccountLinkingConfig.UNLINK_ON_GUILD_LEAVE.get() && !LinkingDiscordUtils.isMemberOfAnyGuild(discordId) && allGuildsAvailable())
            AccountLinkService.unlinkAll(discordId, UnlinkReason.GUILD_LEFT);
        else
            RoleSyncFeature.onMembershipChanged(discordId);
    }

    public static void reconcile(List<DiscordGuildContext> contexts) {
        if (!AccountLinking.isEnabled() || !AccountLinkingConfig.UNLINK_ON_GUILD_LEAVE.get() || contexts.isEmpty())
            return;

        if (!allGuildsAvailable())
            return;

        for (DiscordGuildContext context : contexts) {
            if (!context.guild.isLoaded()) {
                logWarnToServer(MinecraftLocaleProvider.Linking.Warn.membersNotLoaded(context.guild.getName()));
                return;
            }
        }

        Set<String> linkedIds = AccountLinkService.getAllDiscordIds();
        List<String> leftIds = linkedIds.stream()
                .filter(id -> contexts.stream().allMatch(context -> context.getMember(id) == null))
                .toList();

        if (leftIds.isEmpty())
            return;

        if (leftIds.size() >= MASS_UNLINK_MIN_COUNT && leftIds.size() * 2 > linkedIds.size()) {
            logWarnToServer(MinecraftLocaleProvider.Linking.Warn.massUnlink(leftIds.size(), linkedIds.size()));
            return;
        }

        LOGGER.info("[AccountLinking] Unlinking {} Discord users who left all guilds from the config", leftIds.size());
        for (String discordId : leftIds)
            AccountLinkService.unlinkAll(discordId, UnlinkReason.GUILD_LEFT);
    }

    private static boolean allGuildsAvailable() {
        List<String> unavailable = LinkingDiscordUtils.getUnavailableGuildIds();
        for (String guildId : unavailable)
            LinkingDiscordUtils.warnOnce("guild_unavailable:" + guildId, MinecraftLocaleProvider.Linking.Warn.guildUnavailable(guildId));
        return unavailable.isEmpty();
    }
}

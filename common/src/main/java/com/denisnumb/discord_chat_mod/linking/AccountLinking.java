package com.denisnumb.discord_chat_mod.linking;

import com.denisnumb.discord_chat_mod.config.configs.AccountLinkingConfig;
import com.denisnumb.discord_chat_mod.linking.discord.AccountLinkDiscordEvents;
import com.denisnumb.discord_chat_mod.linking.discord.LinkingDiscordUtils;
import com.denisnumb.discord_chat_mod.discord.model.DiscordGuildContext;
import com.denisnumb.discord_chat_mod.linking.features.*;
import com.denisnumb.discord_chat_mod.linking.model.AccountLink;
import com.denisnumb.discord_chat_mod.locale.MinecraftLocaleProvider;
import com.mojang.logging.LogUtils;
import net.dv8tion.jda.api.JDA;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static com.denisnumb.discord_chat_mod.utils.MinecraftUtils.logErrorToServer;

public final class AccountLinking {
    private AccountLinking() {}

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final AccountLinkDiscordEvents DISCORD_EVENTS = new AccountLinkDiscordEvents();
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger();

    public static final ExecutorService ASYNC = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable, "DiscordChatMod-AccountLinking-" + THREAD_COUNTER.incrementAndGet());
        thread.setDaemon(true);
        return thread;
    });

    static {
        AccountLinkService.addListener(new PlayerNotificationFeature());
        AccountLinkService.addListener(new WhitelistFeature());
        AccountLinkService.addListener(new LinkedRoleFeature());
        AccountLinkService.addListener(new NicknameSyncFeature());
        AccountLinkService.addListener(new RoleSyncFeature());
    }

    public static boolean isEnabled() {
        return AccountLinkingConfig.ENABLED.get();
    }

    public static void onServerStarting(MinecraftServer server) {
        AccountLinkService.load(server);
    }

    public static void onServerStarted() {
        if (isEnabled())
            RoleSyncFeature.ensureSubscribed();
    }

    public static void onServerStopped() {
        RoleSyncFeature.unsubscribe();
        AccountLinkService.unload();
    }

    public static void onDiscordReady(JDA jda) {
        jda.addEventListener(DISCORD_EVENTS);
        LinkingDiscordUtils.clearReportedProblems();

        if (!isEnabled())
            return;

        RoleSyncFeature.ensureSubscribed();
        ASYNC.submit(AccountLinking::reconcileWithDiscord);
    }

    public static void onDiscordStopping(JDA jda) {
        jda.removeEventListener(DISCORD_EVENTS);
    }

    public static void onPlayerJoin(Player player) {
        if (!isEnabled())
            return;

        Optional<AccountLink> link = AccountLinkService.updateName(player.getUUID(), player.getGameProfile().name());
        link.ifPresent(l -> {
            NicknameSyncFeature.update(l);
            RoleSyncFeature.reconcile(l);
        });
    }

    private static void reconcileWithDiscord() {
        try {
            List<DiscordGuildContext> contexts = LinkingDiscordUtils.getGuilds();
            if (contexts.isEmpty() || !AccountLinkService.isLoaded())
                return;

            GuildMembershipFeature.reconcile(contexts);
            for (DiscordGuildContext context : contexts) {
                LinkedRoleFeature.reconcile(context);
                NicknameSyncFeature.reconcile(context);
            }
            RoleSyncFeature.reconcileAll();
        } catch (Exception e) {
            logErrorToServer(MinecraftLocaleProvider.Linking.Error.unexpected("reconcile", String.valueOf(e.getMessage())));
            LOGGER.error("", e);
        }
    }
}

package com.denisnumb.discord_chat_mod;

import com.denisnumb.discord_chat_mod.api_impl.DiscordChatApiImpl;
import com.denisnumb.discord_chat_mod.chat.CustomChatTypeRegistry;
import com.denisnumb.discord_chat_mod.commands.ReloadConfigCommand;
import com.denisnumb.discord_chat_mod.commands.set_avatar.AvatarUrlStorage;
import com.denisnumb.discord_chat_mod.config.configs.CommonConfig;
import com.denisnumb.discord_chat_mod.config.configs.DiscordGuildsConfig;
import com.denisnumb.discord_chat_mod.config.configs.DiscordProxyConfig;
import com.denisnumb.discord_chat_mod.config.configs.LogsConfig;
import com.denisnumb.discord_chat_mod.discord.*;
import com.denisnumb.discord_chat_mod.discord.chat.DiscordMessageFormatter;
import com.denisnumb.discord_chat_mod.discord.chat.DiscordMessageSender;
import com.denisnumb.discord_chat_mod.discord.chat.model.DiscordMessageBody;
import com.denisnumb.discord_chat_mod.discord.data_providers.ChannelMembersProvider;
import com.denisnumb.discord_chat_mod.discord.data_providers.CustomEmojiProvider;
import com.denisnumb.discord_chat_mod.discord.data_providers.StickersProvider;
import com.denisnumb.discord_chat_mod.discord.model.ChannelCategory;
import com.denisnumb.discord_chat_mod.linking.AccountLinking;
import com.mojang.logging.LogUtils;
import com.neovisionaries.ws.client.ProxySettings;
import com.neovisionaries.ws.client.WebSocketFactory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.utils.JDALogger;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.denisnumb.discord_chat_mod.utils.MinecraftUtils.*;
import static com.denisnumb.discord_chat_mod.discord.chat.DiscordMessageSender.*;
import static com.denisnumb.discord_chat_mod.discord.ServerStatusController.initServerStatusController;
import static com.denisnumb.discord_chat_mod.discord.ServerStatusController.updateServerStatusWithDelay;
import static com.denisnumb.discord_chat_mod.discord.ServerStatusController.updateServerStatusMessageToUnavailable;

import com.denisnumb.discord_chat_mod.discord.slash_commands.DiscordSlashCommands;

public final class DiscordChatMod {
    public static final String MOD_ID = "discord_chat_mod";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static JDA jda;
    public static MinecraftServer server;
    private static final DiscordEvents discordEvents = new DiscordEvents();
    private static final AtomicBoolean discordConnecting = new AtomicBoolean(false);

    private enum ServerType {
        DEDICATED,
        LAN,
        SINGLEPLAYER
    }

    private static volatile ServerType serverType;
    private static final AtomicReference<Runnable> pendingStartAnnouncement = new AtomicReference<>();

    public static void onServerStarting(MinecraftServer minecraftServer) {
        server = minecraftServer;

        if (server.isDedicatedServer()) {
            serverType = ServerType.DEDICATED;
            connectDiscordAsync();
        } else if (CommonConfig.CONNECT_DISCORD_IN_SINGLEPLAYER.get()) {
            serverType = ServerType.SINGLEPLAYER;
            connectDiscordAsync();
        }

        CustomChatTypeRegistry.registerChatTypes(server.registryAccess().lookupOrThrow(Registries.CHAT_TYPE));
        AccountLinking.onServerStarting(server);
    }

    public static void onServerStarted() {
        if (LogsConfig.SERVER_LOGS_TO_DISCORD_ENABLED.get())
            ServerLogsRetranslator.start();

        AccountLinking.onServerStarted();

        if (serverType == ServerType.DEDICATED)
            announceSessionStart(() -> sendFromServer(ChannelCategory.SERVER_START_STOP, DiscordMessageFormatter.formatServerStartMessage()));
    }

    public static void onIntegratedServerStarted() {
        serverType = ServerType.LAN;
        announceSessionStart(() -> sendFromServer(ChannelCategory.SERVER_START_STOP, DiscordMessageFormatter.formatLocalServerStartMessage(server.getPort())));
        connectDiscordAsync();
    }

    public static boolean announceSingleplayerJoin(Player player) {
        if (serverType != ServerType.SINGLEPLAYER)
            return false;

        announceSessionStart(() -> {
            sendFromServer(ChannelCategory.PLAYER_JOIN_LEAVE, DiscordMessageFormatter.formatPlayerJoinedMessage(player));
            updateServerStatusWithDelay();
        });
        return true;
    }

    public static void onServerStopped() {
        pendingStartAnnouncement.set(null);

        if (serverType != ServerType.SINGLEPLAYER)
            sendFromServer(ChannelCategory.SERVER_START_STOP, DiscordMessageFormatter.formatServerStopMessage());
        serverType = null;

        stopJDA();
        AccountLinking.onServerStopped();
    }

    private static void announceSessionStart(Runnable sendAnnouncement) {
        pendingStartAnnouncement.set(sendAnnouncement);
        handleDiscord(DiscordChatMod::trySendStartAnnouncement);
    }

    private static void trySendStartAnnouncement() {
        if (!isDiscordConnected() || DiscordGuildRegistry.getAllContexts().isEmpty())
            return;

        Runnable sendAnnouncement = pendingStartAnnouncement.getAndSet(null);
        if (sendAnnouncement != null)
            sendAnnouncement.run();
    }

    private static void sendFromServer(ChannelCategory category, DiscordMessageBody body) {
        DiscordMessageSender.sendMessageFromServer(category, DiscordGuildRegistry.getAllContexts(), body);
    }

    private static void connectDiscordAsync() {
        if (isDiscordConnected() || !discordConnecting.compareAndSet(false, true))
            return;

        Thread t = new Thread(() -> {
            try {
                initJDA();
                if (!server.isDedicatedServer())
                    loadClientDiscordData();
            } finally {
                discordConnecting.set(false);
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private static void loadClientDiscordData() {
        StickersProvider.loadClient(StickersProvider.getNameToUrlMap());
        CustomEmojiProvider.loadClient(CustomEmojiProvider.getNameToUrlMap());
        ChannelMembersProvider.CLIENT_MEMBER_CACHE = ChannelMembersProvider.getMemberData(ChannelCategory.PLAYER_CHAT);
    }

    public static boolean isDiscordConnected() {
        return jda != null
                && jda.getStatus() == JDA.Status.CONNECTED
                && !ReloadConfigCommand.isReloadingNow;
    }

    public static void initJDA(){
        try {
            JDALogger.setFallbackLoggerEnabled(false);
            String hostname = DiscordProxyConfig.PROXY_HOSTNAME.get();
            int port = DiscordProxyConfig.PROXY_PORT.get();
            String user = DiscordProxyConfig.PROXY_USER.get();
            String password = DiscordProxyConfig.PROXY_PASSWORD.get();

            WebSocketFactory webSocketFactory = new WebSocketFactory();
            if (!hostname.isEmpty()) {
                ProxySettings settings = webSocketFactory.getProxySettings();
                settings.setHost(hostname).setPort(port);
                if (!user.isEmpty()) {
                    settings.setCredentials(user, password);
                }
            }

            OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
            if (!hostname.isEmpty()) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hostname, port));
                httpClientBuilder.proxy(proxy);
                if (!user.isEmpty()) {
                    httpClientBuilder.proxyAuthenticator((proxy1, url) -> {
                        String credential = Credentials.basic(user, password);
                        return url.request().newBuilder()
                                .header("Proxy-Authorization", credential)
                                .build();
                    });
                }
            }

            jda = JDABuilder.create(CommonConfig.DISCORD_BOT_TOKEN.get(),
                            GatewayIntent.MESSAGE_CONTENT,
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_PRESENCES,
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.GUILD_EXPRESSIONS)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .disableCache(CacheFlag.VOICE_STATE, CacheFlag.SCHEDULED_EVENTS)
                    .addEventListeners(discordEvents)
                    .setHttpClientBuilder(httpClientBuilder)
                    .setWebsocketFactory(webSocketFactory)
                    .build();

            jda.awaitReady();
            initDiscordSendExecutor();
            DiscordGuildRegistry.initDiscordGuilds(DiscordGuildsConfig.discordGuildConfigs);
            initServerStatusController();
            DiscordSlashCommands.register(jda, DiscordGuildRegistry.getAllContexts());
            AccountLinking.onDiscordReady(jda);
            AvatarUrlStorage.load(server);

            if (LogsConfig.SERVER_LOGS_TO_DISCORD_ENABLED.get())
                ServerLogsRetranslator.init(LogsConfig.SERVER_LOGS_TO_DISCORD_LOGGING_LEVEL.get(), LogsConfig.SERVER_LOGS_PATTERN.get());

            if (server != null && !server.isRunning()) {
                stopJDA();
                return;
            }

            LOGGER.info("Discord connected");
            DiscordChatApiImpl.onDiscordConnected();
            trySendStartAnnouncement();
        } catch (Exception e) {
            logErrorToServer(Component.literal(String.format("DiscordConnectError: %s", e.getMessage())));
            LOGGER.error("", e);
            stopJDA();
        }
    }

    public static void stopJDA() {
        if (jda != null) {
            DiscordChatApiImpl.onDiscordDisconnecting();
            DiscordSlashCommands.unregister(jda);
            AccountLinking.onDiscordStopping(jda);
            updateServerStatusMessageToUnavailable();
            ServerLogsRetranslator.stop();
            stopDiscordSendExecutor();
            AvatarUrlStorage.unload();
            LOGGER.info("Disconnecting from discord...");

            try {
                jda.removeEventListener(discordEvents);
                jda.awaitShutdown(Duration.ofMillis(5000));
                jda.cancelRequests();
                jda.shutdownNow();
                if (jda.getHttpClient() != null) {
                    jda.getHttpClient().dispatcher().executorService().shutdownNow();
                    jda.getHttpClient().connectionPool().evictAll();
                }
                jda.getGatewayPool().shutdownNow();
                jda.getCallbackPool().shutdownNow();
                jda.getRateLimitPool().shutdownNow();
            } catch (Exception ignored) {}
            jda = null;

            LOGGER.info("Discord disconnected");
        }
    }
}

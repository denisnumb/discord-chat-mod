package com.denisnumb.discord_chat_mod;

import com.denisnumb.discord_chat_mod.commands.set_avatar.AvatarUrlStorage;
import com.denisnumb.discord_chat_mod.config.IPlatformConfig;
import com.denisnumb.discord_chat_mod.config.PlatformConfig;
import com.denisnumb.discord_chat_mod.discord.ChannelMembersProvider;
import com.denisnumb.discord_chat_mod.discord.CustomEmojiProvider;
import com.denisnumb.discord_chat_mod.discord.DiscordChannelRegistry;
import com.denisnumb.discord_chat_mod.discord.DiscordEvents;
import com.mojang.logging.LogUtils;
import com.neovisionaries.ws.client.ProxySettings;
import com.neovisionaries.ws.client.WebSocketFactory;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.minecraft.server.MinecraftServer;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.net.Proxy;

import static com.denisnumb.discord_chat_mod.ColorUtils.Color.GREEN;
import static com.denisnumb.discord_chat_mod.ColorUtils.Color.RED;
import static com.denisnumb.discord_chat_mod.LocaleProvider.getTranslate;
import static com.denisnumb.discord_chat_mod.MinecraftUtils.*;
import static com.denisnumb.discord_chat_mod.ModLanguageKey.*;
import static com.denisnumb.discord_chat_mod.discord.DiscordChannelRegistry.clearChannelsCache;
import static com.denisnumb.discord_chat_mod.discord.DiscordChannelRegistry.initDiscordChannels;
import static com.denisnumb.discord_chat_mod.discord.DiscordUtils.*;
import static com.denisnumb.discord_chat_mod.discord.ServerStatusController.initServerStatusController;
import static com.denisnumb.discord_chat_mod.discord.ServerStatusController.updateServerStatusMessageToUnavailable;
import static com.denisnumb.discord_chat_mod.discord.WebhookUtils.initWebhookSendExecutor;
import static com.denisnumb.discord_chat_mod.discord.WebhookUtils.stopWebhookSendExecutor;

public final class DiscordChatMod {
    public static final String MOD_ID = "discord_chat_mod";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static JDA jda;
    public static MinecraftServer server;

    public static void onServerStarting(MinecraftServer minecraftServer) {
        server = minecraftServer;
        if (server.isPublished())
            initJDA();
    }

    public static void onServerStarted() {
        sendEmbedMessage(
                DiscordChannelRegistry.serverStartStopChannel,
                buildEmbed(getTranslate(SERVER_STARTED), GREEN)
        );
    }

    public static void onServerStopped() {
        sendEmbedMessage(
                DiscordChannelRegistry.serverStartStopChannel,
                buildEmbed(getTranslate(SERVER_CLOSED), RED)
        );
        stopJDA();
    }

    public static void onIntegratedServerStarted(){
        new Thread(() -> {
            initJDA();
            sendEmbedMessage(
                    DiscordChannelRegistry.serverStartStopChannel,
                    buildEmbed(String.format(getTranslate(LOCAL_SERVER_STARTED), server.getPort()), GREEN)
            );

            CustomEmojiProvider.loadClient(CustomEmojiProvider.getNameToUrlMap(DiscordChannelRegistry.defaultChannel));
            ChannelMembersProvider.clientMemberData = ChannelMembersProvider.getMemberData(DiscordChannelRegistry.playerChatMessagesChannel);
        }).start();
    }

    public static boolean isDiscordConnected() {
        return jda != null && jda.getStatus() == JDA.Status.CONNECTED;
    }

    private static void initJDA(){
        try {
            IPlatformConfig config = PlatformConfig.getConfig();

            WebSocketFactory webSocketFactory = new WebSocketFactory();
            if (!config.proxyHostname().isEmpty()) {
                ProxySettings settings = webSocketFactory.getProxySettings();
                settings.setHost(config.proxyHostname()).setPort(config.proxyPort());
                if (!config.proxyUser().isEmpty()) {
                    settings.setCredentials(config.proxyUser(), config.proxyPassword());
                }
            }

            OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
            if (!config.proxyHostname().isEmpty()) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(config.proxyHostname(), config.proxyPort()));
                httpClientBuilder.proxy(proxy);
                if (!config.proxyUser().isEmpty()) {
                    httpClientBuilder.proxyAuthenticator((proxy1, url) -> {
                        String credential = Credentials.basic(config.proxyUser(), config.proxyPassword());
                        return url.request().newBuilder()
                                .header("Proxy-Authorization", credential)
                                .build();
                    });
                }
            }

            jda = JDABuilder.create(config.discordBotToken(),
                            GatewayIntent.MESSAGE_CONTENT,
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_PRESENCES,
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.GUILD_EXPRESSIONS)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .addEventListeners(new DiscordEvents())
                    .setHttpClientBuilder(httpClientBuilder)
                    .setWebsocketFactory(webSocketFactory)
                    .build();

            jda.awaitReady();
            initWebhookSendExecutor();
            initDiscordChannels();
            initServerStatusController();
            AvatarUrlStorage.load(server);

            LOGGER.info("Discord connected");
        } catch (Exception e) {
            logErrorToServer(String.format("DiscordConnectError: %s", e.getMessage()));
            e.printStackTrace();
            stopJDA();
        }
    }

    private static void stopJDA() {
        if (jda != null) {
            updateServerStatusMessageToUnavailable();
            clearChannelsCache();
            stopWebhookSendExecutor();
            AvatarUrlStorage.unload();
            jda.shutdown();
            jda = null;
            LOGGER.info("Discord disconnected");
        }
    }
}

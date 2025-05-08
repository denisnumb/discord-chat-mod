package com.denisnumb.discord_chat_mod;


import com.denisnumb.discord_chat_mod.discord.DiscordEvents;
import com.mojang.logging.LogUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.denisnumb.discord_chat_mod.ColorUtils.Color.GREEN;
import static com.denisnumb.discord_chat_mod.ColorUtils.Color.RED;
import static com.denisnumb.discord_chat_mod.MinecraftUtils.loadLocalization;
import static com.denisnumb.discord_chat_mod.ModLanguageKey.*;
import static com.denisnumb.discord_chat_mod.discord.DiscordUtils.requiredPermissions;
import static com.denisnumb.discord_chat_mod.discord.ServerStatusController.*;
import static com.denisnumb.discord_chat_mod.discord.DiscordUtils.*;
import static com.denisnumb.discord_chat_mod.MinecraftUtils.*;
import static com.denisnumb.discord_chat_mod.discord.ServerStatusController.initServerStatusController;

@Mod(DiscordChatMod.MODID)
public class DiscordChatMod
{
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String MODID = "discord_chat_mod";
    public static JDA jda;
    public static MinecraftServer server;
    public static GuildMessageChannel discordChannel;
    public static final Map<String, String> languageData = new HashMap<>();


    public DiscordChatMod()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        loadLocalization();
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        server = event.getServer();
        if (server.isPublished())
            initJDA();
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent ignored) {
        sendShortEmbedMessage(getTranslate(SERVER_STARTED, "Server started"), GREEN);
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent ignored) {
        sendShortEmbedMessage(getTranslate(SERVER_CLOSED, "Server closed"), RED);
        stopJDA();
    }

    public static void onIntegratedServerStarted(){
        new Thread(() -> {
            initJDA();
            sendShortEmbedMessage(String.format(getTranslate(
                    LOCAL_SERVER_STARTED,
                    "Local server started [`%d`]"
                    ), server.getPort()), GREEN);
        }).start();
    }

    public static boolean isDiscordConnected() {
        return jda != null && discordChannel != null;
    }

    private static void initJDA(){
        try {
            jda = JDABuilder.create(Config.DISCORD_BOT_TOKEN.get(),
                            GatewayIntent.MESSAGE_CONTENT,
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_PRESENCES,
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.GUILD_EXPRESSIONS)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .addEventListeners(new DiscordEvents())
                    .build();

            jda.awaitReady();

            try{
                discordChannel = jda.getChannelById(GuildMessageChannel.class, Config.DISCORD_CHANNEL_ID.get());
                if (discordChannel == null)
                    throw new IllegalArgumentException();
            } catch (IllegalArgumentException e){
                throw new IllegalArgumentException(
                        getTranslate(
                                INVALID_CHANNEL_ERROR,
                                "The specified Discord channel ID is invalid or the bot does not have access to the specified channel"
                        )
                );
            }

            Member selfMember = discordChannel.getGuild().getSelfMember();
            EnumSet<Permission> missingPermissions = requiredPermissions.stream()
                    .filter(perm -> !selfMember.hasPermission(discordChannel, perm))
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(Permission.class)));

            if (!missingPermissions.isEmpty()){
                throw new IllegalStateException(String.format(
                        getTranslate(MISSING_PERMISSIONS_ERROR, "The bot does not have enough rights to work correctly! Missing:\n%s"),
                        String.join("\n", missingPermissions.stream().map(Permission::getName).toList())
                ));
            }

            initServerStatusController();
            LOGGER.info("Discord connected");
        } catch (Exception e) {
            logErrorToServer(String.format("DiscordConnectError: %s", e.getMessage()));
            stopJDA();
        }
    }

    private static void stopJDA() {
        if (jda != null) {
            updateServerStatusMessageToUnavailable();
            jda.shutdown();
            jda = null;
            LOGGER.info("Discord disconnected");
        }
    }
}

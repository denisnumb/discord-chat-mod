package com.denisnumb.discord_chat_mod.discord;

import com.denisnumb.discord_chat_mod.chat_images.ImageUtils;
import com.denisnumb.discord_chat_mod.config.IPlatformConfig;
import com.denisnumb.discord_chat_mod.config.PlatformConfig;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.jda;
import static com.denisnumb.discord_chat_mod.MinecraftUtils.*;
import static com.denisnumb.discord_chat_mod.ModLanguageKey.*;

public class DiscordChannelRegistry {
    public static GuildMessageChannel defaultChannel;
    public static GuildMessageChannel pinnedStatusMessageChannel;
    public static GuildMessageChannel deathsChannel;
    public static GuildMessageChannel advancementsChannel;
    public static GuildMessageChannel serverStartStopChannel;
    public static GuildMessageChannel playerJoinLeaveChannel;
    public static GuildMessageChannel playerChatMessagesChannel;
    public static GuildMessageChannel screenshotsChannel;
    public static GuildMessageChannel tellrawChannel;
    public static GuildMessageChannel sayChannel;

    private static final Map<String, Optional<GuildMessageChannel>> CHANNEL_CACHE = new HashMap<>();
    private static final Map<GuildMessageChannel, Webhook> CHANNEL_WEBHOOKS = new HashMap<>();

    private static final List<Permission> requiredPermissions = List.of(
            Permission.VIEW_CHANNEL,
            Permission.MESSAGE_SEND,
            Permission.MESSAGE_SEND_IN_THREADS,
            Permission.MESSAGE_EMBED_LINKS,
            Permission.MESSAGE_ATTACH_FILES,
            Permission.MESSAGE_MANAGE,
            Permission.MESSAGE_HISTORY
    );

    public static Optional<Webhook> getChannelWebhook(GuildMessageChannel channel){
        if (PlatformConfig.getConfig().isWebhookModeEnabled() && CHANNEL_WEBHOOKS.containsKey(channel))
            return Optional.of(CHANNEL_WEBHOOKS.get(channel));
        return Optional.empty();
    }

    public static void clearChannelsCache(){
        CHANNEL_CACHE.clear();
        CHANNEL_WEBHOOKS.clear();
    }

    public static void initDiscordChannels() throws IllegalStateException {
        IPlatformConfig config = PlatformConfig.getConfig();

        defaultChannel = getDiscordChannel(PlatformConfig.getConfig().discordChannelId())
                .orElseThrow(() -> new IllegalStateException("Invalid channel"));

        pinnedStatusMessageChannel = getDiscordChannel(config.pinnedStatusMessageChannelId()).orElse(defaultChannel);
        deathsChannel = getDiscordChannel(config.deathsChannelId()).orElse(defaultChannel);
        advancementsChannel = getDiscordChannel(config.advancementsChannelId()).orElse(defaultChannel);
        serverStartStopChannel = getDiscordChannel(config.serverStartStopChannelId()).orElse(defaultChannel);
        playerJoinLeaveChannel = getDiscordChannel(config.playerJoinLeaveChannelId()).orElse(defaultChannel);
        playerChatMessagesChannel = getDiscordChannel(config.playerChatMessagesChannelId()).orElse(defaultChannel);
        screenshotsChannel = getDiscordChannel(config.screenshotsChannelId()).orElse(defaultChannel);
        tellrawChannel = getDiscordChannel(config.tellrawChannelId()).orElse(defaultChannel);
        sayChannel = getDiscordChannel(config.sayChannelId()).orElse(defaultChannel);
    }

    private static Optional<GuildMessageChannel> getDiscordChannel(String channelId) {
        if (CHANNEL_CACHE.containsKey(channelId))
            return CHANNEL_CACHE.get(channelId);

        if (!channelId.isEmpty()){
            try{
                GuildMessageChannel channel = jda.getChannelById(GuildMessageChannel.class, channelId);
                if (channel == null)
                    throw new IllegalArgumentException(String.format(getTranslate(INVALID_CHANNEL_ERROR), channelId));

                checkDiscordBotPermissionsInChannel(channel);
                initChannelWebhook(channel);
                CHANNEL_CACHE.put(channelId, Optional.of(channel));

                return Optional.of(channel);
            } catch (Exception e){
                logErrorToServer(e.getMessage());
            }
        }
        CHANNEL_CACHE.put(channelId, Optional.empty());

        return Optional.empty();
    }

    private static void initChannelWebhook(GuildMessageChannel channel){
        IPlatformConfig config = PlatformConfig.getConfig();
        if (!config.isWebhookModeEnabled())
            return;

        if (channel instanceof TextChannel textChannel){
            if (!textChannel.getGuild().getSelfMember().hasPermission(textChannel, Permission.MANAGE_WEBHOOKS)){
                logWarnToServer(
                        String.format(
                                getTranslate(WEBHOOK_INITIALIZE_ERROR),
                                String.format(getTranslate(MISSING_MANAGE_WEBHOOK_PERMISSION), "#" + channel.getName())
                        )
                );
                return;
            }

            Webhook webhook = textChannel.retrieveWebhooks()
                    .complete()
                    .stream()
                    .filter(wh -> jda.getSelfUser().equals(wh.getOwnerAsUser()))
                    .findFirst()
                    .orElse(null);

            try{
                String avatarUrl = ImageUtils.isImageUrl(ImageUtils.getMimeType(config.webhookServerAvatarUrl()))
                        ? config.webhookServerAvatarUrl()
                        : jda.getSelfUser().getAvatarUrl();
                Icon avatar = Icon.from(new URI(Objects.requireNonNull(avatarUrl)).toURL().openStream());

                if (webhook == null)
                    webhook = textChannel.createWebhook("DC & Chat Images").setAvatar(avatar).complete();
                else
                    webhook.getManager().setAvatar(avatar).queue();

                CHANNEL_WEBHOOKS.put(channel, webhook);
            } catch (Exception e) {
                logWarnToServer(String.format(getTranslate(WEBHOOK_INITIALIZE_ERROR), e.getMessage()));
            }

        } else {
            logWarnToServer(
                    String.format(
                            getTranslate(WEBHOOK_INITIALIZE_ERROR),
                            String.format(getTranslate(INVALID_CHANNEL_TYPE_FOR_WEBHOOK), "#" + channel.getName())
                    )
            );
        }
    }

    private static void checkDiscordBotPermissionsInChannel(GuildMessageChannel channel) throws IllegalStateException {
        Member selfMember = channel.getGuild().getSelfMember();
        EnumSet<Permission> missingPermissions = requiredPermissions.stream()
                .filter(perm -> !selfMember.hasPermission(channel, perm))
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Permission.class)));

        if (!missingPermissions.isEmpty()){
            throw new IllegalStateException(String.format(
                    getTranslate(MISSING_PERMISSIONS_ERROR),
                    "#" + channel.getName(),
                    String.join("\n", missingPermissions.stream().map(Permission::getName).toList())
            ));
        }
    }


}

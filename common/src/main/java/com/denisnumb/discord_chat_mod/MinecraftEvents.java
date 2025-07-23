package com.denisnumb.discord_chat_mod;
import com.denisnumb.discord_chat_mod.commands.MentionCommand;
import com.denisnumb.discord_chat_mod.commands.SayCommand;
import com.denisnumb.discord_chat_mod.commands.TellrawCommand;
import com.denisnumb.discord_chat_mod.commands.set_avatar.SetAvatarCommand;
import com.denisnumb.discord_chat_mod.config.PlatformConfig;
import com.denisnumb.discord_chat_mod.discord.ChannelMembersProvider;
import com.denisnumb.discord_chat_mod.discord.model.DiscordMemberData;
import com.denisnumb.discord_chat_mod.discord.model.DiscordMentionData;
import com.denisnumb.discord_chat_mod.markdown.MarkdownParser;
import com.denisnumb.discord_chat_mod.markdown.MarkdownToComponentConverter;
import com.denisnumb.discord_chat_mod.markdown.MarkdownToken;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.denisnumb.discord_chat_mod.ColorUtils.Color.*;
import static com.denisnumb.discord_chat_mod.DiscordChatMod.isDiscordConnected;
import static com.denisnumb.discord_chat_mod.MinecraftUtils.getLocalizedDeathMessage;
import static com.denisnumb.discord_chat_mod.MinecraftUtils.getTranslate;
import static com.denisnumb.discord_chat_mod.advancement.AdvancementParser.*;
import static com.denisnumb.discord_chat_mod.discord.DiscordUtils.*;
import static com.denisnumb.discord_chat_mod.discord.ServerStatusController.updateServerStatusWithDelay;
import static com.denisnumb.discord_chat_mod.discord.DiscordChannelRegistry.*;

public class MinecraftEvents {
    public static void handleRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        MentionCommand.register(dispatcher);
        SayCommand.register(dispatcher);
        TellrawCommand.register(dispatcher, context);

        if (PlatformConfig.getConfig().isWebhookModeEnabled() && PlatformConfig.getConfig().isSetAvatarUrlCommandEnabled())
            SetAvatarCommand.register(dispatcher);
    }

    public static Component handleChatMessage(ServerPlayer player, Component component) {
        String message = component.getString();
        Map<String, DiscordMentionData> mentions = Map.of();

        if (isDiscordConnected()) {
            List<DiscordMemberData> memberData = ChannelMembersProvider.getMemberData(playerChatMessagesChannel);

            for (DiscordMemberData member : memberData)
                if (message.contains(member.prettyMention))
                    message = message.replace(member.prettyMention, member.mentionString);

            mentions = new HashMap<>(){{
                for (DiscordMemberData member : memberData)
                    put(member.mentionString, new DiscordMentionData(member));
            }};

            String textForDiscord = replaceEmojiCodesToDiscordMentions(message);
            sendTextMessage(
                    playerChatMessagesChannel,
                    player,
                    textForDiscord,
                    String.format("`<%s>` %s", player.getName().getString(), textForDiscord)
            );
        }

        return new MarkdownToComponentConverter(MarkdownParser.parseMarkdown(message), mentions)
                .convertMarkdownTokensToComponent();
    }

    public static void handlePlayerDie(LivingEntity entity, DamageSource damageSource) {
        if (!isDiscordConnected())
            return;
        if (!(entity instanceof Player))
            return;

        String message;
        try{
            message = getLocalizedDeathMessage(damageSource, entity);
        } catch (Exception ignored) {
            message = damageSource.getLocalizedDeathMessage(entity).getString();
        }
        sendEmbedMessage(deathsChannel, buildEmbed(message, DEFAULT));
    }

    public static void handleAdvancementMade(Player player, AdvancementHolder advancementHolder){
        if (!isDiscordConnected())
            return;
        if (advancementHolder.value().display().isEmpty())
            return;

        DisplayInfo displayInfo = advancementHolder.value().display().get();
        if (!displayInfo.shouldAnnounceChat())
            return;

        String message = displayInfo.getType() == AdvancementType.TASK
                ? getTranslate("chat.type.advancement.task")
                : displayInfo.getType() == AdvancementType.GOAL
                ? getTranslate("chat.type.advancement.goal")
                : getTranslate("chat.type.advancement.challenge");

        ResourceLocation advancementId = advancementHolder.id();
        ResourceLocation advancementResourceLocation = ResourceLocation.fromNamespaceAndPath(
                advancementId.getNamespace(),
                "advancement/" + advancementId.getPath() + ".json"
        );

        String title = displayInfo.getTitle().getString();
        String description = displayInfo.getDescription().getString();

        var advancementJson = getAdvancementFileAsJsonObject(advancementResourceLocation);
        if (advancementJson != null){
            title = getTranslatedAdvancementTitle(advancementJson, title);
            description = getTranslatedAdvancementDescription(advancementJson, description);
        }

        int color = displayInfo.getType() == AdvancementType.CHALLENGE ? PURPLE : GOLD;
        String formattedTitle = MarkdownParser.parseMarkdown(title).stream().allMatch(MarkdownToken::hasNoMarkdown)
                ? "**`" + title + "`**"
                : title;

        sendEmbedMessage(
                advancementsChannel,
                buildEmbed(
                        String.format(
                                message,
                                "**" + player.getName().getString() + "**",
                                formattedTitle
                        ),
                        description,
                        color
                )
        );
    }

    public static void handleJoinLeave(Player player, boolean isJoin) {
        if (!isDiscordConnected())
            return;

        String message = getTranslate(isJoin ? "multiplayer.player.joined" : "multiplayer.player.left");
        int color = isJoin ? GREEN : RED;

        sendEmbedMessage(playerJoinLeaveChannel, buildEmbed(String.format(message, "**" + player.getName().getString() + "**"), color));
        updateServerStatusWithDelay();
    }
}


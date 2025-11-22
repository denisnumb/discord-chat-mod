package com.denisnumb.discord_chat_mod;

import com.denisnumb.discord_chat_mod.commands.*;
import com.denisnumb.discord_chat_mod.commands.set_avatar.SetAvatarCommand;
import com.denisnumb.discord_chat_mod.commands.vanilla.*;
import com.denisnumb.discord_chat_mod.config.ConfigProvider;
import com.denisnumb.discord_chat_mod.config.IConfigProvider;
import com.denisnumb.discord_chat_mod.discord.chat_style.MessageType;
import com.denisnumb.discord_chat_mod.discord.model.ChannelCategory;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.denisnumb.discord_chat_mod.MinecraftUtils.*;
import static com.denisnumb.discord_chat_mod.advancement.AdvancementIconParser.parseAdvancementIcon;
import static com.denisnumb.discord_chat_mod.advancement.AdvancementParser.*;
import static com.denisnumb.discord_chat_mod.chat_style.ChatStyleUtils.mergeMaps;
import static com.denisnumb.discord_chat_mod.discord.DiscordChannelRegistry.getAllContexts;
import static com.denisnumb.discord_chat_mod.discord.DiscordUtils.*;
import static com.denisnumb.discord_chat_mod.discord.ServerStatusController.updateServerStatusWithDelay;
import static com.denisnumb.discord_chat_mod.chat_style.MinecraftChatStyleProvider.*;
import static com.denisnumb.discord_chat_mod.discord.chat_style.DiscordChatStyleProvider.*;
import static com.denisnumb.discord_chat_mod.chat_style.Parameters.*;

public class MinecraftEvents {
    public static void handleRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        MentionCommand.register(dispatcher);
        SendStickerCommand.register(dispatcher);
        ReloadConfigCommand.register(dispatcher);
        SayCommand.register(dispatcher);
        TellrawCommand.register(dispatcher, context);
        MsgCommand.register(dispatcher);
        EmoteCommand.register(dispatcher);
        TeamMsgCommand.register(dispatcher);

        IConfigProvider config = ConfigProvider.getConfig();
        if (config.isWebhookModeEnabled() && config.isSetAvatarUrlCommandEnabled())
            SetAvatarCommand.register(dispatcher);
    }

    public static Component handleChatMessageText(ServerPlayer player, Component component) {
        ProcessChatMessageResult chatMessage = processChatMessage(component.getString(), ChannelCategory.PLAYER_CHAT);

        handleDiscord(() -> {
            Map<String, String> parameters = mergeMaps(Map.of(MESSAGE, chatMessage.forDiscord()), buildPlayerParameters(player));
            Optional<DiscordMessageComponents> chatComponentsOpt = getDiscordMessageComponents(MessageType.CHAT, parameters);
            Optional<DiscordMessageComponents> webhookComponentsOpt = getDiscordMessageComponents(MessageType.CHAT_WEBHOOK, parameters);

            if (chatComponentsOpt.isPresent() && webhookComponentsOpt.isPresent())
                sendMessageFromPlayer(ChannelCategory.PLAYER_CHAT, getAllContexts(), player, webhookComponentsOpt.get(), chatComponentsOpt.get());
        });

        return chatMessage.forMinecraft();
    }

    public static Optional<Component> handleChatMessage(ResourceKey<ChatType> chatType, ChatMessageComponents components) {
        if (ConfigProvider.getConfig().isMinecraftChatCustomizationEnabled())
            return getStyledChatMessage(chatType, components);
        return Optional.empty();
    }

    public static Optional<Component> handlePlayerOrPetDie(List<CombatEntry> combatEntries, LivingEntity entity) {
        if (!(entity instanceof Player) && !(entity instanceof TamableAnimal a && a.isTame()))
            return Optional.empty();

        DeathMessageUtils.DeathMessageComponents components = DeathMessageUtils.getDeathMessageComponents(combatEntries, entity);

        if (entity instanceof Player){
            handleDiscord(() -> {
                Map<String, String> parameters = mergeMaps(
                        Map.of(DEATH_MESSAGE, formatDeathMessageComponents(components)),
                        buildPlayerParameters(components.diedEntityName().getString(), entity)
                );
                getDiscordMessageComponents(MessageType.DEATH, parameters)
                        .ifPresent(discordMessageComponents -> sendMessageFromServer(ChannelCategory.DEATHS, getAllContexts(), discordMessageComponents));
            });
        }

        if (ConfigProvider.getConfig().isMinecraftChatCustomizationEnabled())
            return getStyledDeathMessage(components);
        return Optional.empty();
    }

    public static Optional<Component> handleAdvancementMade(Player player, AdvancementHolder advancementHolder) {
        if (advancementHolder.value().display().isEmpty())
            return Optional.empty();

        DisplayInfo displayInfo = advancementHolder.value().display().get();
        if (!displayInfo.shouldAnnounceChat())
            return Optional.empty();

        ResourceLocation advancementId = advancementHolder.id();
        ResourceLocation advancementResourceLocation = ResourceLocation.fromNamespaceAndPath(
                advancementId.getNamespace(),
                "advancement/" + advancementId.getPath() + ".json"
        );

        String title = displayInfo.getTitle().getString();
        String description = displayInfo.getDescription().getString();

        JsonObject advancementJson = getAdvancementFileAsJsonObject(advancementResourceLocation);
        if (advancementJson != null) {
            title = getTranslatedAdvancementTitle(advancementJson, title);
            description = getTranslatedAdvancementDescription(advancementJson, description);
        }

        String finalTitle = title;
        String finalDescription = description;
        handleDiscord(() -> {
            MessageType messageType = switch (displayInfo.getType()) {
                case TASK -> MessageType.ADVANCEMENT_TASK;
                case CHALLENGE -> MessageType.ADVANCEMENT_CHALLENGE;
                case GOAL -> MessageType.ADVANCEMENT_GOAL;
            };

            Map<String, String> parameters = mergeMaps(
                    Map.of(ADVANCEMENT, finalTitle, DESCRIPTION, finalDescription, ICON_URL, "attachment://icon.png"),
                    buildPlayerParameters(player)
            );

            getDiscordMessageComponents(messageType, parameters).ifPresent(components ->
                    parseAdvancementIcon(displayInfo).ifPresentOrElse(
                            iconData -> sendMessageFromServer(ChannelCategory.ADVANCEMENTS, getAllContexts(), components, iconData),
                            () -> sendMessageFromServer(ChannelCategory.ADVANCEMENTS, getAllContexts(), components)
                    )
            );
        });

        if (ConfigProvider.getConfig().isMinecraftChatCustomizationEnabled())
            return getStyledAdvancementMessage(player, advancementHolder, title, description);
        return Optional.empty();
    }

    public static Optional<Component> handleJoinLeave(Player player, boolean isJoin) {
        handleDiscord(() -> {
            MessageType messageType = isJoin ? MessageType.JOIN : MessageType.LEFT;
            getDiscordMessageComponents(messageType, buildPlayerParameters(player))
                    .ifPresent(components -> sendMessageFromServer(ChannelCategory.PLAYER_JOIN_LEAVE, getAllContexts(), components));
            updateServerStatusWithDelay();
        });

        if (ConfigProvider.getConfig().isMinecraftChatCustomizationEnabled())
            return getStyledJoinedLeftMessage(player, isJoin);
        return Optional.empty();
    }
}


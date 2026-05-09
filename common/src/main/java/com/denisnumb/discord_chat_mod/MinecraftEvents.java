package com.denisnumb.discord_chat_mod;
import com.denisnumb.discord_chat_mod.chat_style.MinecraftChatStyleProvider;
import com.denisnumb.discord_chat_mod.commands.MentionCommand;
import com.denisnumb.discord_chat_mod.commands.ReloadConfigCommand;
import com.denisnumb.discord_chat_mod.commands.SendStickerCommand;
import com.denisnumb.discord_chat_mod.commands.set_avatar.SetAvatarCommand;
import com.denisnumb.discord_chat_mod.commands.vanilla.*;
import com.denisnumb.discord_chat_mod.config.ConfigProvider;
import com.denisnumb.discord_chat_mod.config.IConfigProvider;
import com.denisnumb.discord_chat_mod.discord.chat_style.MessageType;
import com.denisnumb.discord_chat_mod.discord.model.ChannelCategory;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.denisnumb.discord_chat_mod.advancement.AdvancementIconParser.parseAdvancementIcon;
import static com.denisnumb.discord_chat_mod.advancement.AdvancementParser.*;
import static com.denisnumb.discord_chat_mod.chat_style.ChatStyleUtils.mergeMaps;
import static com.denisnumb.discord_chat_mod.chat_style.MinecraftChatStyleProvider.*;
import static com.denisnumb.discord_chat_mod.chat_style.Parameters.*;
import static com.denisnumb.discord_chat_mod.discord.utils.DiscordMessageUtils.*;
import static com.denisnumb.discord_chat_mod.discord.ServerStatusController.updateServerStatusWithDelay;
import static com.denisnumb.discord_chat_mod.discord.DiscordChannelRegistry.*;
import static com.denisnumb.discord_chat_mod.discord.chat_style.DiscordChatStyleProvider.*;

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

    public static Optional<Component> handleChatMessage(ResourceKey<ChatType> chatType, MinecraftChatStyleProvider.ChatMessageComponents components) {
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

    public static Optional<Component> handleAdvancementMade(Player player, Advancement advancement) {
        if (advancement.getDisplay() == null)
            return Optional.empty();

        DisplayInfo displayInfo = advancement.getDisplay();
        if (!displayInfo.shouldAnnounceChat())
            return Optional.empty();

        ResourceLocation advancementId = advancement.getId();
        ResourceLocation advancementResourceLocation = new ResourceLocation(
                advancementId.getNamespace(),
                "advancements/" + advancementId.getPath() + ".json"
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
            MessageType messageType = switch (displayInfo.getFrame()) {
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
            return getStyledAdvancementMessage(player, advancement, title, description);
        return Optional.empty();
    }

    public static void handleCommandExecution(CommandSourceStack source, String command) {
        IConfigProvider config = ConfigProvider.getConfig();
        if (!config.isCommandLogEnabled())
            return;

        Player player = source.getPlayer();
        if (player == null)
            return;

        if (!source.hasPermission(config.commandLogMinPermissionLevel()))
            return;

        String trimmed = command.startsWith("/") ? command.substring(1) : command;
        int spaceIdx = trimmed.indexOf(' ');
        String rootCommand = (spaceIdx == -1 ? trimmed : trimmed.substring(0, spaceIdx))
                .toLowerCase(java.util.Locale.ROOT);
        if (rootCommand.isEmpty() || config.commandLogIgnoredCommands().contains(rootCommand))
            return;

        String displayCommand = "/" + trimmed;

        handleDiscord(() -> {
            Map<String, String> parameters = mergeMaps(
                    Map.of(COMMAND, displayCommand),
                    buildPlayerParameters(player)
            );
            getDiscordMessageComponents(MessageType.COMMAND_LOG, parameters)
                    .ifPresent(components -> sendMessageFromServer(ChannelCategory.COMMAND_LOG, getAllContexts(), components));
        });
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


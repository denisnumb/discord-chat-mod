package com.denisnumb.discord_chat_mod;

import com.denisnumb.discord_chat_mod.chat.MinecraftMessageContext;
import com.denisnumb.discord_chat_mod.chat.MinecraftMessageFormatter;
import com.denisnumb.discord_chat_mod.commands.*;
import com.denisnumb.discord_chat_mod.commands.set_avatar.SetAvatarCommand;
import com.denisnumb.discord_chat_mod.commands.vanilla.*;
import com.denisnumb.discord_chat_mod.config.configs.LogsConfig;
import com.denisnumb.discord_chat_mod.config.configs.MinecraftChatStyleConfig;
import com.denisnumb.discord_chat_mod.config.configs.WebhookModeConfig;
import com.denisnumb.discord_chat_mod.discord.chat.model.DiscordMessageBody;
import com.denisnumb.discord_chat_mod.discord.chat.DiscordMessageFormatter;
import com.denisnumb.discord_chat_mod.discord.model.ChannelCategory;
import com.denisnumb.discord_chat_mod.utils.DeathMessageUtils;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.CombatEntry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Optional;

import com.denisnumb.discord_chat_mod.compat.VanishCompatProvider;
import org.jetbrains.annotations.NotNull;

import static com.denisnumb.discord_chat_mod.utils.AdvancementIconParser.parseAdvancementIcon;
import static com.denisnumb.discord_chat_mod.discord.DiscordChannelRegistry.getAllContexts;
import static com.denisnumb.discord_chat_mod.discord.chat.DiscordMessageSender.*;
import static com.denisnumb.discord_chat_mod.discord.ServerStatusController.updateServerStatusWithDelay;

public final class MinecraftEvents {
    private MinecraftEvents() {}

    public static void handleRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        MentionCommand.register(dispatcher);
        SendStickerCommand.register(dispatcher);
        ReloadConfigCommand.register(dispatcher);
        SayCommand.register(dispatcher);
        TellrawCommand.register(dispatcher, context);
        MsgCommand.register(dispatcher);
        EmoteCommand.register(dispatcher);
        TeamMsgCommand.register(dispatcher);

        if (WebhookModeConfig.ENABLE_SET_AVATAR_URL_COMMAND.get())
            SetAvatarCommand.register(dispatcher);
    }

    public static Optional<Component> handleChatMessage(ResourceKey<@NotNull ChatType> chatType, MinecraftMessageContext components) {
        if (MinecraftChatStyleConfig.ENABLE_MINECRAFT_CHAT_CUSTOMIZATION.get())
            return Optional.ofNullable(MinecraftMessageFormatter.getStyledChatMessage(chatType, components));
        return Optional.empty();
    }

    public static Optional<Component> handlePlayerOrPetDie(List<CombatEntry> combatEntries, LivingEntity entity) {
        if (!(entity instanceof Player) && !(entity instanceof TamableAnimal a && a.isTame()))
            return Optional.empty();

        DeathMessageUtils.DeathMessageComponents components = DeathMessageUtils.getDeathMessageComponents(combatEntries, entity);

        if (entity instanceof Player){
            handleDiscord(() -> sendMessageFromServer(ChannelCategory.DEATHS, getAllContexts(), DiscordMessageFormatter.formatDeathMessage(components, entity)));
        }

        if (MinecraftChatStyleConfig.ENABLE_MINECRAFT_CHAT_CUSTOMIZATION.get())
            return Optional.of(MinecraftMessageFormatter.getStyledDeathMessage(components, entity));
        return Optional.empty();
    }

    public static Optional<Component> handleAdvancementMade(Player player, AdvancementHolder advancementHolder) {
        if (advancementHolder.value().display().isEmpty())
            return Optional.empty();

        DisplayInfo displayInfo = advancementHolder.value().display().get();
        if (!displayInfo.shouldAnnounceChat())
            return Optional.empty();

        handleDiscord(() -> {
            DiscordMessageBody components = DiscordMessageFormatter.formatAdvancementMessage(displayInfo, player, "attachment://icon.png");
            sendMessageFromServer(ChannelCategory.ADVANCEMENTS, getAllContexts(), components, parseAdvancementIcon(displayInfo).orElse(null));
        });

        if (MinecraftChatStyleConfig.ENABLE_MINECRAFT_CHAT_CUSTOMIZATION.get())
            return Optional.of(MinecraftMessageFormatter.getStyledAdvancementMessage(player, displayInfo));
        return Optional.empty();
    }

    public static void handleCommandExecution(CommandSourceStack source, String command) {
        if (!LogsConfig.COMMAND_LOG_ENABLED.get())
            return;

        Player player = source.getPlayer();
        if (player == null)
            return;

        net.minecraft.server.permissions.PermissionCheck levelCheck = switch (Math.max(0, Math.min(4, LogsConfig.COMMAND_LOG_MIN_PERMISSION_LEVEL.get()))) {
            case 0 -> Commands.LEVEL_ALL;
            case 1 -> Commands.LEVEL_MODERATORS;
            case 3 -> Commands.LEVEL_ADMINS;
            case 4 -> Commands.LEVEL_OWNERS;
            default -> Commands.LEVEL_GAMEMASTERS;
        };
        if (!Commands.hasPermission(levelCheck).test(source))
            return;

        String trimmed = command.startsWith("/") ? command.substring(1) : command;
        int spaceIdx = trimmed.indexOf(' ');
        String rootCommand = (spaceIdx == -1 ? trimmed : trimmed.substring(0, spaceIdx))
                .toLowerCase(java.util.Locale.ROOT);
        if (rootCommand.isEmpty() || LogsConfig.COMMAND_LOG_IGNORED_COMMANDS.get().contains(rootCommand))
            return;

        String displayCommand = "/" + trimmed;

        handleDiscord(() ->
                sendMessageFromServer(ChannelCategory.COMMAND_LOG, getAllContexts(), DiscordMessageFormatter.formatPlayerCommandLogMessage(player, displayCommand))
        );
    }

    public static Optional<Component> handleJoinLeave(Player player, boolean isJoin) {
        if (VanishCompatProvider.get().isVanished(player))
            return Optional.empty();

        handleDiscord(() -> {
            DiscordMessageBody components = isJoin
                    ? DiscordMessageFormatter.formatPlayerJoinedMessage(player)
                    : DiscordMessageFormatter.formatPlayerLeftMessage(player);

            sendMessageFromServer(ChannelCategory.PLAYER_JOIN_LEAVE, getAllContexts(), components);
            updateServerStatusWithDelay();
        });

        if (MinecraftChatStyleConfig.ENABLE_MINECRAFT_CHAT_CUSTOMIZATION.get())
            return Optional.of(MinecraftMessageFormatter.getStyledJoinedLeftMessage(player, isJoin));
        return Optional.empty();
    }
}


package com.denisnumb.discord_chat_mod.discord.chat;


import com.denisnumb.discord_chat_mod.config.configs.DiscordChatStyleConfig;
import com.denisnumb.discord_chat_mod.discord.chat.model.DiscordMessageBody;
import com.denisnumb.discord_chat_mod.discord.chat.model.DiscordMessageComponents;
import com.denisnumb.discord_chat_mod.discord.chat.template.TemplateType;
import com.denisnumb.discord_chat_mod.discord.chat.template.TemplateTypes;
import com.denisnumb.discord_chat_mod.markdown.ComponentToMarkdownConverter;
import com.denisnumb.discord_chat_mod.utils.DeathMessageUtils;
import com.denisnumb.discord_chat_mod.locale.DiscordLocaleProvider;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;


public final class DiscordMessageFormatter {
    private DiscordMessageFormatter() {}

    private static String getTranslatedComponent(Component component){
        return component.getContents() instanceof TranslatableContents tc
                ? DiscordLocaleProvider.getTranslate(tc.getKey())
                : component.getString();
    }

    public static DiscordMessageBody formatDeathMessage(DeathMessageUtils.DeathMessageComponents components, LivingEntity diedEntity){
        String deathCauseTemplate = DiscordChatStyleConfig.PLAYER_DEATH_CAUSE_TEMPLATE.get().applyParameters(
                new TemplateTypes.DeathCause.Params(DiscordLocaleProvider.getTranslate(components.deathCauseLocaleKey())));

        String playerTemplate = DiscordChatStyleConfig.PLAYER_DEATH_NAME_TEMPLATE.get().applyParameters(
                new TemplateTypes.DiedEntity.Params(diedEntity, getTranslatedComponent(components.diedEntity())));

        String killerTemplate = DiscordChatStyleConfig.KILLER_ENTITY_TEMPLATE.get().applyParameters(
                new TemplateTypes.KillerEntity.Params(components.killerEntity() == null ? "" : getTranslatedComponent(components.killerEntity())));

        String weaponTemplate = DiscordChatStyleConfig.KILLER_WEAPON_TEMPLATE.get().applyParameters(
                new TemplateTypes.KillerWeapon.Params(components.killerWeapon() == null ? "" : components.killerWeapon().getString()));

        String formattedDeathCause = String.format(deathCauseTemplate, playerTemplate, killerTemplate, weaponTemplate);
        return DiscordChatStyleConfig.DEATH_MESSAGE_TEMPLATE.get()
                .applyParameters(new TemplateTypes.DeathMessage.Params(diedEntity, components.diedEntity().getString(), formattedDeathCause));
    }

    public static DiscordMessageBody formatAdvancementMessage(DisplayInfo displayInfo, Entity player, String iconUrl) {
        String formattedTitle = ComponentToMarkdownConverter.componentToDiscordMarkdown(displayInfo.getTitle());
        String formattedDescription = ComponentToMarkdownConverter.componentToDiscordMarkdown(displayInfo.getDescription());

        TemplateType.ParamsBuilder<TemplateTypes.AdvancementTemplateType> paramsBuilder
                = new TemplateTypes.AdvancementTemplateType.Params(player, formattedTitle, formattedDescription, iconUrl);

        return switch (displayInfo.getType()) {
            case TASK -> DiscordChatStyleConfig.ADVANCEMENT_TASK_TEMPLATE.get().applyParameters(paramsBuilder);
            case CHALLENGE -> DiscordChatStyleConfig.ADVANCEMENT_CHALLENGE_TEMPLATE.get().applyParameters(paramsBuilder);
            case GOAL -> DiscordChatStyleConfig.ADVANCEMENT_GOAL_TEMPLATE.get().applyParameters(paramsBuilder);
        };
    }

    public static DiscordMessageBody formatPlayerJoinedMessage(Player player) {
        return DiscordChatStyleConfig.PLAYER_JOINED_TEMPLATE.get().applyParameters(new TemplateTypes.PlayerJoined.Params(player));
    }

    public static DiscordMessageBody formatPlayerLeftMessage(Player player) {
        return DiscordChatStyleConfig.PLAYER_LEFT_TEMPLATE.get().applyParameters(new TemplateTypes.PlayerLeft.Params(player));
    }

    public static DiscordMessageBody formatPlayerCommandLogMessage(Player player, String command) {
        return DiscordChatStyleConfig.COMMAND_LOG_TEMPLATE.get().applyParameters(new TemplateTypes.CommandLog.Params(player, command));
    }

    public static DiscordMessageBody formatPinnedStatusUnavailableMessage() {
        return DiscordChatStyleConfig.PINNED_STATUS_MESSAGE_SERVER_UNAVAILABLE_TEMPLATE.get().applyParameters(new TemplateTypes.EmptyParams());
    }

    public static DiscordMessageBody formatPinnedStatusAvailableMessage() {
        return DiscordChatStyleConfig.PINNED_STATUS_MESSAGE_SERVER_AVAILABLE_TEMPLATE.get().applyParameters(new TemplateTypes.EmptyParams());
    }

    public static DiscordMessageBody formatPinnedStatusOnlinePlayersMessage(String playerList, int playerCount, int maxPlayers) {
        return DiscordChatStyleConfig.PINNED_STATUS_MESSAGE_TEMPLATE.get().applyParameters(
                new TemplateTypes.PinnedStatusOnlinePlayers.Params(playerList, playerCount, maxPlayers)
        );
    }

    public static DiscordMessageBody formatGuildForwardedMessage(MessageReceivedEvent event) {
        return DiscordChatStyleConfig.GUILD_FORWARDED_MESSAGE_TEMPLATE.get().applyParameters(
                new TemplateTypes.GuildForwardedMessage.Params(event.getMember(), event.getAuthor(), event.getGuild(), event.getMessage().getContentRaw())
        );
    }

    public static DiscordMessageComponents formatImageMessage(Player player, String imageUrl) {
        TemplateTypes.ImageMessage.Params params = new TemplateTypes.ImageMessage.Params(player, imageUrl);

        return new DiscordMessageComponents(
                DiscordChatStyleConfig.IMAGE_MESSAGE_WEBHOOK_TEMPLATE.get().applyParameters(params),
                DiscordChatStyleConfig.IMAGE_MESSAGE_TEMPLATE.get().applyParameters(params)
        );
    }

    public static DiscordMessageComponents formatPlayerChatMessage(Player player, String message) {
        TemplateTypes.PlayerMessage.Params params = TemplateTypes.PlayerMessage.ofParams(player, message);

        return new DiscordMessageComponents(
                DiscordChatStyleConfig.PLAYER_MESSAGE_WEBHOOK_TEMPLATE.get().applyParameters(params),
                DiscordChatStyleConfig.PLAYER_MESSAGE_TEMPLATE.get().applyParameters(params)
        );
    }

    public static DiscordMessageComponents formatMeCommandMessage(CommandSourceStack source, String message) {
        TemplateTypes.PlayerMessage.Params params = TemplateTypes.PlayerMessage.ofParams(source, message);

        return new DiscordMessageComponents(
                DiscordChatStyleConfig.ME_COMMAND_WEBHOOK_TEMPLATE.get().applyParameters(params),
                DiscordChatStyleConfig.ME_COMMAND_TEMPLATE.get().applyParameters(params)
        );
    }

    public static DiscordMessageBody formatSayCommandMessage(CommandSourceStack source, String message) {
        return DiscordChatStyleConfig.SAY_COMMAND_TEMPLATE.get().applyParameters(TemplateTypes.PlayerMessage.ofParams(source, message));
    }

    public static DiscordMessageBody formatTellrawCommandMessage(String message) {
        return DiscordChatStyleConfig.TELLRAW_COMMAND_TEMPLATE.get().applyParameters(new TemplateTypes.TellrawMessage.Params(message));
    }

    public static DiscordMessageBody formatLocalServerStartMessage(int serverPort) {
        return DiscordChatStyleConfig.LOCAL_SERVER_STARTED_MESSAGE_TEMPLATE.get().applyParameters(new TemplateTypes.LocalServerStated.Params(serverPort));
    }

    public static DiscordMessageBody formatServerStartMessage() {
        return DiscordChatStyleConfig.SERVER_STARTED_MESSAGE_TEMPLATE.get().applyParameters(new TemplateTypes.EmptyParams());
    }

    public static DiscordMessageBody formatServerStopMessage() {
        return DiscordChatStyleConfig.SERVER_CLOSED_MESSAGE_TEMPLATE.get().applyParameters(new TemplateTypes.EmptyParams());
    }
}

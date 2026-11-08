package com.denisnumb.discord_chat_mod.linking.discord;

import com.denisnumb.discord_chat_mod.config.configs.AccountLinkingConfig;
import com.denisnumb.discord_chat_mod.linking.AccountLinkService;
import com.denisnumb.discord_chat_mod.linking.AccountLinking;
import com.denisnumb.discord_chat_mod.linking.model.AccountLink;
import com.denisnumb.discord_chat_mod.linking.model.LinkResult;
import com.denisnumb.discord_chat_mod.linking.model.UnlinkReason;
import com.denisnumb.discord_chat_mod.locale.DiscordLocaleProvider;
import com.denisnumb.discord_chat_mod.locale.MinecraftLocaleProvider;
import com.denisnumb.discord_chat_mod.utils.PlayerAvatarProvider;
import com.mojang.logging.LogUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.server;
import static com.denisnumb.discord_chat_mod.utils.ColorUtils.Color.DISCORD_GREEN_COLOR;
import static com.denisnumb.discord_chat_mod.utils.ColorUtils.Color.DISCORD_RED_COLOR;
import static com.denisnumb.discord_chat_mod.utils.MinecraftUtils.logErrorToServer;

public final class AccountLinkSlashCommands extends ListenerAdapter {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String LINK = "link";
    public static final String UNLINK = "unlink";
    public static final Set<String> COMMAND_NAMES = Set.of(LINK, UNLINK);

    private static final String OPTION_CODE = "code";
    private static final String OPTION_NICKNAME = "nickname";
    private static final int MAX_CHOICES = 25;

    public static List<CommandData> buildCommands() {
        return List.of(
                Commands.slash(LINK, DiscordLocaleProvider.Discord.SlashCommands.Link.description())
                        .addOption(OptionType.STRING, OPTION_CODE, DiscordLocaleProvider.Discord.SlashCommands.Link.Args.Code.description(), true),
                Commands.slash(UNLINK, DiscordLocaleProvider.Discord.SlashCommands.Unlink.description())
                        .addOption(OptionType.STRING, OPTION_NICKNAME, DiscordLocaleProvider.Discord.SlashCommands.Unlink.Args.Nickname.description(), false, true)
        );
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!COMMAND_NAMES.contains(event.getName()))
            return;

        if (!LinkingDiscordUtils.isConfiguredGuild(event.getGuild())) {
            reply(event, false, DiscordLocaleProvider.Discord.SlashCommands.commandsAreDisabled());
            return;
        }
        if (!AccountLinking.isEnabled() || !AccountLinkService.isLoaded()) {
            reply(event, false, DiscordLocaleProvider.Discord.Linking.unavailable());
            return;
        }

        try {
            switch (event.getName()) {
                case LINK -> handleLink(event);
                case UNLINK -> handleUnlink(event);
            }
        } catch (Exception e) {
            logErrorToServer(MinecraftLocaleProvider.Linking.Error.unexpected("/" + event.getName(), String.valueOf(e.getMessage())));
            LOGGER.error("", e);
            if (!event.isAcknowledged())
                reply(event, false, DiscordLocaleProvider.Discord.Linking.unavailable());
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        if (!event.getName().equals(UNLINK) || !event.getFocusedOption().getName().equals(OPTION_NICKNAME))
            return;

        String input = event.getFocusedOption().getValue().toLowerCase(Locale.ROOT);
        List<Command.Choice> choices = AccountLinkService.getByDiscordId(event.getUser().getId()).stream()
                .map(AccountLink::name)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(input))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .limit(MAX_CHOICES)
                .map(name -> new Command.Choice(name, name))
                .toList();

        event.replyChoices(choices).queue();
    }

    private void handleLink(SlashCommandInteractionEvent event) {
        String code = Objects.requireNonNull(getString(event, OPTION_CODE));
        AccountLinkService.RedeemResult result = AccountLinkService.redeemCode(event.getUser().getId(), code);

        switch (result.status()) {
            case RATE_LIMITED -> reply(event, false, DiscordLocaleProvider.Discord.Linking.Link.rateLimited());
            case INVALID_CODE -> reply(event, false, DiscordLocaleProvider.Discord.Linking.Link.invalidCode());
            case PROCESSED -> replyLinkResult(event, Objects.requireNonNull(result.linkResult()));
        }
    }

    private void replyLinkResult(SlashCommandInteractionEvent event, LinkResult result) {
        String thumbnailUrl = null;
        if (result.isSuccess() && server != null && result.link() != null){
            Player player = server.getPlayerList().getPlayer(result.link().uuid());
            if (player != null)
                thumbnailUrl = PlayerAvatarProvider.getPlayerAvatarUrl(player);
        }

        switch (result.status()) {
            case LINKED -> {
                String message = DiscordLocaleProvider.Discord.Linking.Link.success(Objects.requireNonNull(result.link()).name());
                if (AccountLinkingConfig.WHITELIST_LINKED_PLAYERS.get())
                    message += "\n" + DiscordLocaleProvider.Discord.Linking.Link.whitelisted();
                replyWithThumbnail(event, true, thumbnailUrl, message);
            }
            case ALREADY_LINKED -> replyWithThumbnail(event, true, thumbnailUrl, DiscordLocaleProvider.Discord.Linking.Link.alreadyLinked(Objects.requireNonNull(result.link()).name()));
            case LIMIT_REACHED -> reply(event, false, DiscordLocaleProvider.Discord.Linking.Link.limitReached(AccountLinkingConfig.MAX_MINECRAFT_ACCOUNTS_PER_DISCORD_USER.get()));
            case UNAVAILABLE -> reply(event, false, DiscordLocaleProvider.Discord.Linking.unavailable());
        }
    }

    private void handleUnlink(SlashCommandInteractionEvent event) {
        String nickname = getString(event, OPTION_NICKNAME);
        List<AccountLink> links = AccountLinkService.getByDiscordId(event.getUser().getId());

        if (links.isEmpty()) {
            reply(event, false, DiscordLocaleProvider.Discord.Linking.Unlink.notLinked());
            return;
        }

        AccountLink target;
        if (nickname != null) {
            target = links.stream().filter(link -> link.name().equalsIgnoreCase(nickname)).findFirst().orElse(null);
            if (target == null) {
                reply(event, false, DiscordLocaleProvider.Discord.Linking.Unlink.unknownNickname(nickname));
                return;
            }
        } else if (links.size() == 1) {
            target = links.getFirst();
        } else {
            reply(event, false, DiscordLocaleProvider.Discord.Linking.Unlink.specifyNickname(
                    links.stream().map(link -> "`" + link.name() + "`").collect(Collectors.joining(", "))));
            return;
        }

        AccountLinkService.unlink(target.uuid(), UnlinkReason.DISCORD_USER);
        reply(event, true, DiscordLocaleProvider.Discord.Linking.Unlink.success(target.name()));
    }

    @Nullable
    private static String getString(SlashCommandInteractionEvent event, String option) {
        OptionMapping mapping = event.getOption(option);
        if (mapping == null)
            return null;
        String value = mapping.getAsString().trim();
        return value.isEmpty() ? null : value;
    }

    private static MessageEmbed embed(boolean success, String message, @Nullable String thumbnailUrl) {
        return new EmbedBuilder()
                .setColor(success ? DISCORD_GREEN_COLOR : DISCORD_RED_COLOR)
                .setDescription(message)
                .setThumbnail(thumbnailUrl)
                .build();
    }

    private static void reply(SlashCommandInteractionEvent event, boolean success, String message) {
        event.replyEmbeds(embed(success, message, null)).setEphemeral(true).queue();
    }

    private static void replyWithThumbnail(SlashCommandInteractionEvent event, boolean success, String thumbnailUrl, String message) {
        event.replyEmbeds(embed(success, message, thumbnailUrl)).setEphemeral(true).queue();
    }
}

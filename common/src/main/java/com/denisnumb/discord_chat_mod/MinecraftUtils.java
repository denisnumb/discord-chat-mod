package com.denisnumb.discord_chat_mod;

import com.denisnumb.discord_chat_mod.config.ConfigDefaults;
import com.denisnumb.discord_chat_mod.config.ConfigProvider;
import com.denisnumb.discord_chat_mod.discord.data_providers.ChannelMembersProvider;
import com.denisnumb.discord_chat_mod.discord.model.ChannelCategory;
import com.denisnumb.discord_chat_mod.discord.model.DiscordMentionData;
import com.denisnumb.discord_chat_mod.discord.model.DiscordUserData;
import com.denisnumb.discord_chat_mod.markdown.MarkdownParser;
import com.denisnumb.discord_chat_mod.markdown.MarkdownToComponentConverter;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.isDiscordConnected;
import static com.denisnumb.discord_chat_mod.DiscordChatMod.server;
import static com.denisnumb.discord_chat_mod.chat_style.ChatStyleUtils.applyParametersToTemplate;
import static com.denisnumb.discord_chat_mod.chat_style.ChatStyleUtils.parseConfigTemplateMarkdown;
import static com.denisnumb.discord_chat_mod.discord.utils.DiscordMessageUtils.replaceEmojiCodesToDiscordMentions;

public class MinecraftUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    public record ProcessChatMessageResult(Component forMinecraft, String forDiscord) {
    }

    public static ProcessChatMessageResult processChatMessage(String message, ChannelCategory chatCategoryToParseMembers) {
        Map<String, DiscordMentionData> mentions = Map.of();
        String forDiscord = message;

        if (isDiscordConnected()) {
            List<DiscordUserData> memberData
                    = ChannelMembersProvider.getMemberData(chatCategoryToParseMembers);

            for (DiscordUserData member : memberData)
                if (message.contains(member.prettyMention))
                    message = message.replace(member.prettyMention, member.mentionString);

            mentions = new HashMap<>() {{
                for (DiscordUserData member : memberData)
                    put(member.mentionString, new DiscordMentionData(member));
            }};

            forDiscord = MarkdownParser.removeColorTags(replaceEmojiCodesToDiscordMentions(message));
        }

        Component forMinecraft = new MarkdownToComponentConverter(MarkdownParser.parseMarkdown(message), mentions)
                .convertMarkdownTokensToComponent();

        return new ProcessChatMessageResult(forMinecraft, forDiscord);
    }

    public static List<ServerPlayer> getPlayerListBySelector(String selector){
        try {
            CommandSourceStack fakeSource = server.createCommandSourceStack()
                    .withSuppressedOutput()
                    .withPermission(4);
            EntitySelectorParser parser = new EntitySelectorParser(new StringReader(selector), true);

            return parser.parse().findPlayers(fakeSource);
        } catch (Exception e){
            return List.of();
        }
    }

    public static void sendMessageToPlayersBySelector(Component message, String selector) {
        try {
            for (ServerPlayer player : getPlayerListBySelector(selector))
                player.sendSystemMessage(ComponentUtils.updateForEntity(null, message, player, 0), false);
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        } catch (Exception ignored) {}
    }

    public static void sendMessageToAllPlayers(Component message){
        sendMessageToPlayersBySelector(message, "@a");
    }

    public static void sendMessageToAllPlayersFromPlayer(Map<String, Component> parameters) {
        String template = ConfigProvider.getConfig().isMinecraftChatCustomizationEnabled()
                ? ConfigProvider.getConfig().minecraftPlayerMessageStyle()
                : ConfigDefaults.MINECRAFT_PLAYER_MESSAGE_STYLE_DEFAULT;

        sendMessageToAllPlayers(applyParametersToTemplate(
                parseConfigTemplateMarkdown(template),
                parameters
        ));
    }

    public static void showTitleBarMessage(Component message){
        Minecraft.getInstance().gui.setOverlayMessage(message, false);
    }

    public static void logErrorToServer(String message) {
        LOGGER.error(message);
        if (ConfigProvider.getConfig().isLoggingDiscordErrorsToServerChatEnabled())
            sendMessageToPlayersBySelector(buildLogMessageComponent(message, ChatFormatting.RED.getColor()), ConfigProvider.getConfig().discordErrorsChatPlayerSelector());
    }

    public static void logWarnToServer(String message) {
        LOGGER.warn(message);
        if (ConfigProvider.getConfig().isLoggingDiscordErrorsToServerChatEnabled())
            sendMessageToPlayersBySelector(buildLogMessageComponent(message, ChatFormatting.YELLOW.getColor()), ConfigProvider.getConfig().discordErrorsChatPlayerSelector());
    }

    public static int getServerPlayerCount(@Nullable MinecraftServer server){
        if (server != null && server.getPlayerList() != null)
            return server.getPlayerCount();
        return 0;
    }

    public static int getServerMaxPlayers(@Nullable MinecraftServer server){
        if (server != null && server.getPlayerList() != null)
            return server.getMaxPlayers();
        return 20;
    }

    public static String[] getServerPlayerNames(@Nullable MinecraftServer server) {
        if (server != null && server.getPlayerList() != null)
            return server.getPlayerNames();
        return new String[0];
    }

    private static Component buildLogMessageComponent(String message, int color) {
        return Component.empty()
                .append(Component.literal("[discord_chat_mod] ")
                        .withStyle(style -> style.withBold(true))
                )
                .append(Component.literal(message)).setStyle(Style.EMPTY.withColor(color));
    }
}

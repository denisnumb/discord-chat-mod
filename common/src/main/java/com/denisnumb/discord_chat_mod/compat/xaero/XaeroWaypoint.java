package com.denisnumb.discord_chat_mod.compat.xaero;

import com.denisnumb.discord_chat_mod.DiscordChatMod;
import com.denisnumb.discord_chat_mod.chat.CommonMessageFormatter.FormattedMessage;
import com.denisnumb.discord_chat_mod.chat.message_part.MessageFormatContext;
import com.denisnumb.discord_chat_mod.chat.message_part.MessagePart;
import com.denisnumb.discord_chat_mod.chat.message_part.MessagePartFinder;
import com.denisnumb.discord_chat_mod.chat.template.TemplateParameterFactory;
import com.denisnumb.discord_chat_mod.compat.webmap.WebMapProvider;
import com.denisnumb.discord_chat_mod.config.configs.DiscordChatStyleConfig;
import com.denisnumb.discord_chat_mod.config.configs.WebMapConfig;
import com.denisnumb.discord_chat_mod.discord.chat.template.StringTemplate;
import com.denisnumb.discord_chat_mod.discord.chat.template.TemplateTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;


public record XaeroWaypoint(
        String rawText,
        String name,
        @Nullable Integer x,
        @Nullable Integer y,
        @Nullable Integer z,
        String container,
        List<String> dimensionCandidates
) implements MessagePart {
    public static final MessagePartFinder FINDER = message -> XaeroWaypointParser.findAll(message).stream()
            .map(occurrence -> new MessagePartFinder.Match(occurrence.start(), occurrence.end(), occurrence.waypoint()))
            .toList();

    private static final String UNKNOWN_COORDINATE = "~";
    private static final String UNKNOWN_DIMENSION = "?";
    private static final int DEFAULT_Y = 64;
    private static final String DEFAULT_WORLD_NAME = "world";
    private static final String DISCORD_MARKDOWN_CHARACTERS = "\\*_~|`[]<>";

    private record Dimension(@Nullable String id, String name) {}

    public boolean hasHorizontalPosition() {
        return x != null && z != null;
    }

    @Override
    public FormattedMessage format(MessageFormatContext context) {
        return new FormattedMessage(
                Component.literal(rawText),
                context.discordConnected() ? formatForDiscord() : rawText
        );
    }

    public String formatForDiscord() {
        if (!WebMapConfig.FORMAT_XAERO_WAYPOINTS.get())
            return rawText;

        Dimension dimension = resolveDimension();
        Optional<String> mapUrl = buildMapUrl(dimension);

        String waypointName = mapUrl.isPresent()
                ? toLinkText(name)
                : escapeDiscordMarkdown(name);

        String text = DiscordChatStyleConfig.XAERO_WAYPOINT_TEMPLATE.get().applyParameters(new TemplateTypes.SharedWaypoint.Params(
                waypointName,
                dimension.name(),
                formatCoordinate(x),
                formatCoordinate(y),
                formatCoordinate(z)
        ));

        return mapUrl
                .map(url -> "[" + text + "](<" + url + ">)")
                .orElse(text);
    }

    private Optional<String> buildMapUrl(Dimension dimension) {
        if (dimension.id() == null || !hasHorizontalPosition())
            return Optional.empty();

        StringTemplate<TemplateTypes.WebMapLink> linkTemplate = WebMapConfig.WEB_MAP_LINK_TEMPLATE.get();
        if (linkTemplate.rawTemplate().isBlank())
            return Optional.empty();

        String mapId = WebMapProvider.get().getMapId(dimension.id(), getWorldName());
        int linkY = y != null ? y : DEFAULT_Y;

        return Optional.of(linkTemplate.applyParameters(new TemplateTypes.WebMapLink.Params(mapId, x, linkY, z)));
    }

    private Dimension resolveDimension() {
        MinecraftServer server = DiscordChatMod.server;

        if (server != null) {
            for (String candidate : dimensionCandidates) {
                for (ServerLevel level : server.getAllLevels()) {
                    if (level.dimension().identifier().toString().equals(candidate))
                        return new Dimension(candidate, TemplateParameterFactory.getDimensionName(level.dimension()));
                }
            }
        }

        if (!dimensionCandidates.isEmpty()) {
            String id = dimensionCandidates.getFirst();
            return new Dimension(id, TemplateParameterFactory.getDimensionName(id));
        }

        String containerBody = XaeroWaypointParser.getContainerBody(container);
        return new Dimension(null, containerBody.isEmpty() ? UNKNOWN_DIMENSION : escapeDiscordMarkdown(containerBody));
    }

    private static String getWorldName() {
        MinecraftServer server = DiscordChatMod.server;
        if (server == null)
            return DEFAULT_WORLD_NAME;

        Path fileName = server.getWorldPath(LevelResource.ROOT).normalize().getFileName();
        return fileName == null || fileName.toString().isEmpty() ? DEFAULT_WORLD_NAME : fileName.toString();
    }

    private static String formatCoordinate(@Nullable Integer coordinate) {
        return coordinate == null ? UNKNOWN_COORDINATE : String.valueOf(coordinate);
    }

    private static String toLinkText(String text) {
        return text.replace('[', '(').replace(']', ')');
    }

    private static String escapeDiscordMarkdown(String text) {
        StringBuilder result = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            if (DISCORD_MARKDOWN_CHARACTERS.indexOf(c) >= 0)
                result.append('\\');
            result.append(c);
        }
        return result.toString();
    }
}

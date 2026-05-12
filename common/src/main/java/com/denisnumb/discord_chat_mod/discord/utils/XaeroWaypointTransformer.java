package com.denisnumb.discord_chat_mod.discord.utils;

import com.denisnumb.discord_chat_mod.config.ConfigProvider;
import com.denisnumb.discord_chat_mod.config.IConfigProvider;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XaeroWaypointTransformer {
    private static final Pattern WAYPOINT_PATTERN = Pattern.compile(
            "xaero-waypoint:([^:]+):[^:]*:(-?\\d+|~):(-?\\d+|~):(-?\\d+|~):[^:]*:[^:]*:[^:]*:(Internal-[\\w%$-]+)"
    );

    private static final Pattern MODDED_CATEGORY = Pattern.compile(
            "Internal-dim%([\\w-]+)\\$([\\w-]+)-waypoints"
    );

    private static final int DEFAULT_Y_FALLBACK = 64;

    private record Dimension(String id, String label) {}

    public static String transform(String text) {
        if (text == null || !text.contains("xaero-waypoint:"))
            return text;

        IConfigProvider config = ConfigProvider.getConfig();
        if (config == null || !config.isXaeroWaypointParsingEnabled())
            return text;

        Matcher matcher = WAYPOINT_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String name = matcher.group(1);
            int x = parseCoord(matcher.group(2), 0);
            int y = parseCoord(matcher.group(3), DEFAULT_Y_FALLBACK);
            int z = parseCoord(matcher.group(4), 0);
            Dimension dimension = resolveDimension(matcher.group(5));

            String replacement = renderWaypoint(config, name, x, y, z, dimension);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static int parseCoord(String raw, int fallback) {
        if ("~".equals(raw)) return fallback;
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static Dimension resolveDimension(String xaeroCategory) {
        Matcher m = MODDED_CATEGORY.matcher(xaeroCategory);
        if (m.matches()) {
            String namespace = m.group(1).replace('-', '_');
            String path = m.group(2).replace('-', '_');
            return new Dimension(namespace + ":" + path, prettifyPath(path));
        }
        String id = xaeroCategory.contains("nether") ? "minecraft:the_nether"
                : xaeroCategory.contains("end") ? "minecraft:the_end"
                : "minecraft:overworld";
        return new Dimension(id, prettifyPath(pathOf(id)));
    }

    private static String pathOf(String dimensionId) {
        int colon = dimensionId.indexOf(':');
        return colon < 0 ? dimensionId : dimensionId.substring(colon + 1);
    }

    private static String prettifyPath(String path) {
        String trimmed = path.startsWith("the_") ? path.substring(4) : path;
        StringBuilder out = new StringBuilder();
        for (String part : trimmed.split("_")) {
            if (part.isEmpty()) continue;
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1).toLowerCase(Locale.ROOT));
        }
        return out.isEmpty() ? path : out.toString();
    }

    private static String renderWaypoint(IConfigProvider config, String name, int x, int y, int z, Dimension dimension) {
        String safeName = escapeMarkdownBrackets(name);
        String coords = String.format("%d, %d, %d", x, y, z);
        String label = dimension.label();

        String template = config.webMapUrlTemplate();
        if (template == null || template.isBlank())
            return String.format("[%s] %s (%s)", label, safeName, coords);

        Optional<String> mapId = resolveMapId(dimension);
        if (mapId.isEmpty())
            return String.format("[%s] %s (%s)", label, safeName, coords);

        String url = template
                .replace("{world}", mapId.get())
                .replace("{x}", Integer.toString(x))
                .replace("{y}", Integer.toString(y))
                .replace("{z}", Integer.toString(z));

        return String.format("[%s] [%s (%s)](<%s>)", label, safeName, coords, url);
    }

    private static Optional<String> resolveMapId(Dimension dimension) {
        return BluemapMapsRegistry.getMapId(dimension.id());
    }

    private static String escapeMarkdownBrackets(String name) {
        return name.replace("[", "\\[").replace("]", "\\]");
    }
}

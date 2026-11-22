package com.denisnumb.discord_chat_mod.compat.xaero;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class XaeroWaypointParser {
    private XaeroWaypointParser() {}

    public static final String PREFIX = "xaero-waypoint:";
    private static final String ESCAPED_COLON = "^col^";
    private static final String COORDINATE = "~|-?\\d{1,9}";
    private static final String CONTAINER = "Internal-[\\w%$.-]+?-waypoints|[\\w%$.-]*";

    private static final Pattern WAYPOINT = Pattern.compile(
            Pattern.quote(PREFIX)
                    + "(?<name>[^:\\r\\n]+)"
                    + ":(?<initials>[^:\\r\\n]*)"
                    + ":(?<x>" + COORDINATE + ")"
                    + ":(?<y>" + COORDINATE + ")"
                    + ":(?<z>" + COORDINATE + ")"
                    + ":(?<color>-?\\d+)"
                    + ":(?<useYaw>[^:\\s]*)"
                    + ":(?<yaw>-?\\d+)"
                    + "(?::(?<container>" + CONTAINER + "))?"
    );

    private static final String CONTAINER_PREFIX = "Internal-";
    private static final String CONTAINER_SUFFIX = "-waypoints";
    private static final String DIM_PREFIX = "dim%";

    private static final String OVERWORLD = "minecraft:overworld";
    private static final String NETHER = "minecraft:the_nether";
    private static final String END = "minecraft:the_end";

    public record Occurrence(int start, int end, XaeroWaypoint waypoint) {}

    public static boolean mayContainWaypoint(@Nullable String text) {
        return text != null && text.contains(PREFIX);
    }

    public static List<Occurrence> findAll(@Nullable String text) {
        if (!mayContainWaypoint(text))
            return List.of();

        List<Occurrence> result = new ArrayList<>();
        Matcher matcher = WAYPOINT.matcher(text);
        while (matcher.find()) {
            String container = matcher.group("container");
            container = container == null ? "" : container;

            XaeroWaypoint waypoint = new XaeroWaypoint(
                    matcher.group(),
                    matcher.group("name").replace(ESCAPED_COLON, ":"),
                    parseCoordinate(matcher.group("x")),
                    parseCoordinate(matcher.group("y")),
                    parseCoordinate(matcher.group("z")),
                    container,
                    resolveDimensionCandidates(container)
            );
            result.add(new Occurrence(matcher.start(), matcher.end(), waypoint));
        }

        return result;
    }

    @Nullable
    private static Integer parseCoordinate(String value) {
        return "~".equals(value) ? null : Integer.valueOf(value);
    }

    public static List<String> resolveDimensionCandidates(String container) {
        String body = getContainerBody(container);
        if (body.isEmpty())
            return List.of();

        switch (body) {
            case "overworld", "dim%0", "dim%minecraft$overworld" -> { return List.of(OVERWORLD); }
            case "the-nether", "the_nether", "nether", "dim%-1" -> { return List.of(NETHER); }
            case "the-end", "the_end", "end", "dim%1" -> { return List.of(END); }
        }

        if (!body.startsWith(DIM_PREFIX))
            return List.of();

        String id = body.substring(DIM_PREFIX.length());
        int separator = id.indexOf('$');
        if (separator <= 0 || separator == id.length() - 1)
            return List.of();

        String namespace = id.substring(0, separator);
        String path = id.substring(separator + 1).replace('%', '/');

        Set<String> candidates = new LinkedHashSet<>();
        candidates.add(namespace.replace('-', '_') + ":" + path.replace('-', '_'));
        candidates.add(namespace + ":" + path);
        return List.copyOf(candidates);
    }

    public static String getContainerBody(String container) {
        String body = container;
        if (body.startsWith(CONTAINER_PREFIX))
            body = body.substring(CONTAINER_PREFIX.length());
        if (body.endsWith(CONTAINER_SUFFIX))
            body = body.substring(0, body.length() - CONTAINER_SUFFIX.length());
        return body;
    }
}

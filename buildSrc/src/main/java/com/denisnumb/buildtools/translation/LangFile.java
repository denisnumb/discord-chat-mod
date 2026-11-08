package com.denisnumb.buildtools.translation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Flat Minecraft lang json: ordered key/value pairs plus the formatting details we want to keep
 * when writing it back (line separator, trailing newline, blank lines that separate key groups).
 */
public final class LangFile {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final Type TYPE = new TypeToken<LinkedHashMap<String, String>>() {}.getType();
    private static final Pattern KEY_LINE = Pattern.compile("^\\s*(\"(?:[^\"\\\\]|\\\\.)*\")\\s*:");

    public final LinkedHashMap<String, String> entries;
    /** Keys that have an empty line right before them (group separators). */
    public final Set<String> groupStarts;
    public final String lineSeparator;
    public final boolean trailingNewline;

    private LangFile(LinkedHashMap<String, String> entries, Set<String> groupStarts,
                     String lineSeparator, boolean trailingNewline) {
        this.entries = entries;
        this.groupStarts = groupStarts;
        this.lineSeparator = lineSeparator;
        this.trailingNewline = trailingNewline;
    }

    public static LangFile read(Path path) throws IOException {
        String raw = Files.readString(path, StandardCharsets.UTF_8);
        if (!raw.isEmpty() && raw.charAt(0) == '﻿')
            raw = raw.substring(1);

        LinkedHashMap<String, String> entries = raw.isBlank() ? null : GSON.fromJson(raw, TYPE);
        if (entries == null)
            entries = new LinkedHashMap<>();

        return new LangFile(
                entries,
                findGroupStarts(raw),
                raw.contains("\r\n") ? "\r\n" : "\n",
                raw.endsWith("\n")
        );
    }

    private static Set<String> findGroupStarts(String raw) {
        Set<String> result = new HashSet<>();
        boolean previousBlank = false;
        boolean firstKey = true;
        for (String line : raw.split("\\r?\\n")) {
            Matcher m = KEY_LINE.matcher(line);
            if (m.find()) {
                if (previousBlank && !firstKey)
                    result.add(GSON.fromJson(m.group(1), String.class));
                firstKey = false;
            }
            previousBlank = line.isBlank();
        }
        return result;
    }

    /**
     * Renders entries in the given order, inserting an empty line before every key from {@code groupStarts}.
     */
    public static String render(Map<String, String> entries, Set<String> groupStarts,
                                String lineSeparator, boolean trailingNewline) {
        if (entries.isEmpty())
            return "{}" + (trailingNewline ? lineSeparator : "");

        StringBuilder sb = new StringBuilder("{").append(lineSeparator);
        boolean first = true;
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            if (!first) {
                sb.append(',').append(lineSeparator);
                if (groupStarts.contains(entry.getKey()))
                    sb.append(lineSeparator);
            }
            first = false;
            sb.append("  ").append(GSON.toJson(entry.getKey()))
                    .append(": ").append(GSON.toJson(entry.getValue()));
        }
        sb.append(lineSeparator).append('}');
        if (trailingNewline)
            sb.append(lineSeparator);
        return sb.toString();
    }
}

package com.denisnumb.discord_chat_mod.emoji.picker;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class RecentEmojiStore {
    private RecentEmojiStore() {}

    public static final int MAX_SIZE = 18;

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Path FILE = Paths.get("config", "discord_chat_mod-recent_emoji.json");
    private static final Gson GSON = new Gson();
    private static final Type TYPE = new TypeToken<List<String>>(){}.getType();

    private static LinkedList<String> ids;

    private static LinkedList<String> ids() {
        if (ids == null) {
            ids = new LinkedList<>();
            try {
                if (Files.exists(FILE)) {
                    List<String> loaded = GSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), TYPE);
                    if (loaded != null)
                        loaded.stream().filter(Objects::nonNull).limit(MAX_SIZE).forEach(ids::add);
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to read recent emoji list", e);
            }
        }
        return ids;
    }

    public static List<PickerEntry> entries() {
        List<PickerEntry> result = new ArrayList<>();
        for (String id : ids()) {
            PickerEntry entry = EmojiCatalog.resolve(id);
            if (entry != null)
                result.add(entry);
        }
        return result;
    }

    public static void add(PickerEntry entry) {
        LinkedList<String> list = ids();
        list.remove(entry.id());
        list.addFirst(entry.id());
        while (list.size() > MAX_SIZE)
            list.removeLast();

        String json = GSON.toJson(new ArrayList<>(list), TYPE);
        CompletableFuture.runAsync(() -> {
            try {
                Files.createDirectories(FILE.getParent());
                Files.writeString(FILE, json, StandardCharsets.UTF_8);
            } catch (IOException e) {
                LOGGER.warn("Failed to save recent emoji list", e);
            }
        });
    }
}

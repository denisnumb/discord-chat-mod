package com.denisnumb.discord_chat_mod.discord;

import com.denisnumb.discord_chat_mod.chat_images.ImageStorage;
import com.denisnumb.discord_chat_mod.chat_images.model.AbstractImage;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.denisnumb.discord_chat_mod.DiscordChatMod.isDiscordConnected;

public class CustomEmojiProvider {
    public static Map<String, AbstractImage> CLIENT_EMOJI_CACHE = new HashMap<>();
    private static long lastGetNameToUrlMap = 0;
    private static long lastGetGuildEmojis = 0;
    private static Map<String, String> cachedNameToUrlMap;
    private static Map<String, List<RichCustomEmoji>> cachedGuildEmojis;

    public static Map<String, List<RichCustomEmoji>> getGuildEmojis(GuildMessageChannel channel){
        if (!isDiscordConnected())
            return Map.of();

        if (System.currentTimeMillis() - lastGetGuildEmojis < 300000)
            return cachedGuildEmojis;
        lastGetGuildEmojis = System.currentTimeMillis();

        return cachedGuildEmojis = channel.getGuild()
                .getEmojis()
                .stream()
                .collect(Collectors.groupingBy(
                        RichCustomEmoji::getName,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> list.stream()
                                        .sorted(Comparator.comparing(RichCustomEmoji::getTimeCreated))
                                        .toList()
                        )
                ));
    }

    public static Map<String, String> getNameToUrlMap(GuildMessageChannel channel){
        if (!isDiscordConnected())
            return Map.of();

        if (System.currentTimeMillis() - lastGetNameToUrlMap < 300000)
            return cachedNameToUrlMap;
        lastGetNameToUrlMap = System.currentTimeMillis();

        return cachedNameToUrlMap = channel.getGuild()
                .getEmojis()
                .stream()
                .collect(Collectors.groupingBy(RichCustomEmoji::getName))
                .entrySet()
                .stream()
                .flatMap(entry -> {
                    String baseName = entry.getKey();
                    List<RichCustomEmoji> sortedEmojis = entry.getValue()
                            .stream()
                            .sorted(Comparator.comparing(RichCustomEmoji::getTimeCreated))
                            .toList();

                    return IntStream.range(0, sortedEmojis.size())
                            .mapToObj(i -> {
                                RichCustomEmoji emoji = sortedEmojis.get(i);
                                String name = i == 0 ? baseName : baseName + "~" + i;
                                return Map.entry(name, emoji.getImageUrl());
                            });
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    public static void loadClient(Map<String, String> rawEmojis){
        ExecutorService executor = Executors.newFixedThreadPool(10);

        rawEmojis.forEach((name, url) -> {
            if (!CLIENT_EMOJI_CACHE.containsKey(name)){
                executor.submit(() -> {
                    AbstractImage image = ImageStorage.parseEmoji(url);
                    if (image != null)
                        CLIENT_EMOJI_CACHE.putIfAbsent(name, image);
                });
            }
        });
        executor.shutdown();

        ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor();
        monitor.execute(() -> {
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            } finally {
                monitor.shutdown();
            }
        });
    }
}

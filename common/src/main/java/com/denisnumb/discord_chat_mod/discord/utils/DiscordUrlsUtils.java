package com.denisnumb.discord_chat_mod.discord.utils;

import com.denisnumb.discord_chat_mod.markdown.MarkdownPattern;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class DiscordUrlsUtils {
    private DiscordUrlsUtils() {}

    /**
     * Only retrieve embed URLs for Discord attachment links.
     * Don't retrieve embed URLs for other hosts as they may:
     * <ul>
     *     <li>Point media types that are incompatible with this mod, such as Tenor embed URLs pointing to video files.</li>
     *     <li>Be "incorrect", such as an embed URL for a social media post being a link to the post's image, causing the link to the post itself being overridden.</li>
     * </ul>
     */
    private static final Set<String> DISCORD_HOSTS = Set.of("cdn.discordapp.com", "media.discordapp.net");

    private static final Set<String> KLIPY_HOSTS = Set.of("klipy.com", "www.klipy.com", "klipy.co", "www.klipy.co");
    private static final Pattern KLIPY_MEDIA_HOST = Pattern.compile("^static\\d*\\.klipy\\.com$");

    /**
     * Embeds for links sent for the first time may be added later, so wait a bit before each refetch.
     */
    private static final Duration REFETCH_DELAY = Duration.ofMillis(700);

    /**
     * Retrieves refreshed Discord attachment links and direct media links of GIF providers asynchronously
     * via message embeds.
     *
     * @param message The message to retrieve the refreshed URLs from.
     * @return A future that resolves to a mapping of URLs in the message text to the corresponding media URL.
     */
    public static @NotNull CompletableFuture<Map<String, String>> retrieveMessageEmbedUrls(Message message) {
        return retrieveMessageEmbedUrls(message, 3);
    }

    private static @NotNull CompletableFuture<Map<String, String>> retrieveMessageEmbedUrls(Message message, int retries) {
        Map<String, String> embedUrls = getMessageEmbedUrls(message);
        try {
            /*
             * Links that have been sent for the first time may not have embeds generated yet,
             * so refetch the message a few times to get the embeds.
             * Only refetch the message if it contains URLs.
             */
            if (retries > 0 && embedUrls.isEmpty() && hasUrls(message.getContentRaw())) {
                return message.getChannel()
                        .retrieveMessageById(message.getId())
                        .delay(REFETCH_DELAY)
                        .submit()
                        .exceptionally(ignored -> message)
                        .thenCompose(refetchedMessage -> retrieveMessageEmbedUrls(refetchedMessage, retries - 1));
            }
        } catch (Exception ignored) {
        }
        return CompletableFuture.completedFuture(embedUrls);
    }

    private static @NotNull Map<String, String> getMessageEmbedUrls(Message message) {
        return message.getEmbeds()
                .stream()
                .map((embed) -> {
                    String url = embed.getUrl();
                    if (url == null)
                        return null;
                    String mediaUrl = getEmbedMediaUrl(embed);
                    return mediaUrl == null ? null : Map.entry(url, mediaUrl);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (first, second) -> first));
    }

    /**
     * @return the media URL that should replace the embed's link, or {@code null} to keep the link as is.
     */
    public static @Nullable String getEmbedMediaUrl(MessageEmbed embed) {
        String host = getHost(embed.getUrl());
        if (host == null)
            return null;
        if (DISCORD_HOSTS.contains(host))
            return getMessageEmbedMediaUrl(embed);
        if (KLIPY_HOSTS.contains(host))
            return getKlipyEmbedMediaUrl(embed);
        return null;
    }

    private static @Nullable String getMessageEmbedMediaUrl(MessageEmbed embed) {
        if (embed.getImage() != null && embed.getImage().getUrl() != null)
            return embed.getImage().getUrl();
        else if (embed.getVideoInfo() != null && embed.getVideoInfo().getUrl() != null)
            return embed.getVideoInfo().getUrl();
        else if (embed.getThumbnail() != null && embed.getThumbnail().getUrl() != null)
            return embed.getThumbnail().getUrl();
        return null;
    }

    private static @Nullable String getKlipyEmbedMediaUrl(MessageEmbed embed) {
        if (embed.getThumbnail() != null && isKlipyAnimatedImageUrl(embed.getThumbnail().getUrl()))
            return embed.getThumbnail().getUrl();
        if (embed.getImage() != null && isKlipyAnimatedImageUrl(embed.getImage().getUrl()))
            return embed.getImage().getUrl();
        return null;
    }

    private static boolean isKlipyAnimatedImageUrl(@Nullable String url) {
        String host = getHost(url);
        if (host == null || !KLIPY_MEDIA_HOST.matcher(host).matches())
            return false;

        String path = URI.create(url).getPath().toLowerCase(Locale.ROOT);
        return path.endsWith(".webp") || path.endsWith(".gif");
    }

    private static @Nullable String getHost(@Nullable String url) {
        if (url == null)
            return null;

        try {
            String host = URI.create(url).getHost();
            return host == null ? null : host.toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Checks if the {@code text} contains any URLs whose embeds this class handles.
     */
    private static boolean hasUrls(String text) {
        return MarkdownPattern.URL.matcher(text).results().anyMatch(matchResult -> {
            String host = getHost(matchResult.group());
            return host != null && (DISCORD_HOSTS.contains(host) || KLIPY_HOSTS.contains(host));
        });
    }
}

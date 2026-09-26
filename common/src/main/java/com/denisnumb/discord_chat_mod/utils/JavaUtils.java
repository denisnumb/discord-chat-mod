package com.denisnumb.discord_chat_mod.utils;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public final class JavaUtils {
    private JavaUtils() {}

    @SafeVarargs
    public static <K, V> Map<K, V> mergeMaps(Map<K, V>... parameterMaps){
        return Arrays.stream(parameterMaps)
                .flatMap(m -> m.entrySet().stream())
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    public static Boolean orNull(boolean a, boolean b) {
        return (a || b) ? Boolean.TRUE : null;
    }

    public static int codePointLength(String s) {
        return s.codePointCount(0, s.length());
    }

    public static OffsetDateTime getDateTimeWithUtcOffset(int offsetHours){
        Instant nowUtc = Instant.now();
        ZoneOffset offset = ZoneOffset.ofHours(offsetHours);

        return nowUtc.atOffset(offset);
    }

    public static InputStream getInputStreamFromUrl(String url) throws IOException, URISyntaxException {
        HttpURLConnection conn = (HttpURLConnection) new URI(url).toURL().openConnection();
        conn.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                        + "AppleWebKit/537.36 (KHTML, like Gecko) "
                        + "Chrome/124.0.0.0 Safari/537.36");

        return conn.getInputStream();
    }

    @Nullable
    public static <K, V> V waitForLocalResource(Map<K, V> map, K key, long timeoutMillis, long pollIntervalMillis) {
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            synchronized (map) {
                if (map.containsKey(key)) {
                    return map.get(key);
                }
            }

            try {
                Thread.sleep(pollIntervalMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return null;
    }
}

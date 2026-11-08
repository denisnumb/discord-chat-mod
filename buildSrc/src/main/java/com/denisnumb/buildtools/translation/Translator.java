package com.denisnumb.buildtools.translation;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class Translator {
    public static final String SOURCE_LANGUAGE = "en";

    private static final int MAX_ATTEMPTS = 4;

    protected final HttpClient http = HttpClient.newBuilder()
            .proxy(ProxySelector.getDefault())
            .connectTimeout(Duration.ofSeconds(20))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public static Translator create(String id, String apiKey, String apiUrl) {
        return switch (id.toLowerCase(Locale.ROOT)) {
            case "google" -> new GoogleTranslator(apiUrl);
            case "deepl" -> new DeepLTranslator(apiKey, apiUrl);
            default -> throw new IllegalArgumentException(
                    "Unknown translator '" + id + "'. Supported: google, deepl");
        };
    }

    public abstract String id();

    protected abstract Map<String, String> knownLanguageCodes();

    protected abstract String defaultLanguageCode(String language);

    protected abstract String encode(List<TextSegment> segments);

    protected abstract List<TextSegment> decode(String translated);

    protected abstract List<String> translateEncoded(List<String> texts, String targetLanguage)
            throws IOException, InterruptedException;

    public String targetLanguage(String minecraftLocale) {
        String locale = minecraftLocale.toLowerCase(Locale.ROOT);
        String known = knownLanguageCodes().get(locale);
        if (known != null)
            return known;
        int separator = locale.indexOf('_');
        return defaultLanguageCode(separator < 0 ? locale : locale.substring(0, separator));
    }

    public List<List<TextSegment>> translate(List<ProtectedText> texts, String targetLanguage)
            throws IOException, InterruptedException {
        List<String> encoded = new ArrayList<>(texts.size());
        for (ProtectedText text : texts)
            encoded.add(encode(text.segments()));

        List<String> translated = translateEncoded(encoded, targetLanguage);
        if (translated.size() != texts.size()) {
            throw new IOException(id() + " returned " + translated.size()
                    + " translations for " + texts.size() + " strings");
        }

        List<List<TextSegment>> result = new ArrayList<>(translated.size());
        for (String text : translated)
            result.add(decode(text));
        return result;
    }

    protected String send(HttpRequest request) throws IOException, InterruptedException {
        IOException lastError = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            if (attempt > 1)
                Thread.sleep(1000L << (attempt - 2));
            try {
                HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
                int code = response.statusCode();
                if (code / 100 == 2)
                    return response.body();
                lastError = new IOException(id() + " responded with HTTP " + code + ": " + abbreviate(response.body()));
                if (code != 429 && code / 100 != 5)
                    throw lastError;
            } catch (IOException e) {
                if (e == lastError)
                    throw e;
                lastError = e;
            }
        }
        throw lastError;
    }

    private static String abbreviate(String body) {
        String oneLine = body == null ? "" : body.replaceAll("\\s+", " ").trim();
        return oneLine.length() > 300 ? oneLine.substring(0, 300) + "…" : oneLine;
    }
}

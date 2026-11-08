package com.denisnumb.buildtools.translation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class GoogleTranslator extends Translator {
    private static final String DEFAULT_URL = "https://translate.googleapis.com/translate_a/single";
    private static final long REQUEST_DELAY_MS = 150;
    private static final Pattern TOKEN = Pattern.compile("[{｛]\\s*([0-9０-９]+)\\s*[}｝]");

    private static final Map<String, String> KNOWN_CODES = Map.of(
            "zh_cn", "zh-CN",
            "zh_tw", "zh-TW",
            "zh_hk", "zh-TW",
            "lzh", "zh-TW",
            "pt_pt", "pt-PT",
            "fil_ph", "tl"
    );

    private final String url;

    GoogleTranslator(String apiUrl) {
        this.url = apiUrl == null || apiUrl.isBlank() ? DEFAULT_URL : apiUrl;
    }

    @Override
    public String id() {
        return "google";
    }

    @Override
    protected Map<String, String> knownLanguageCodes() {
        return KNOWN_CODES;
    }

    @Override
    protected String defaultLanguageCode(String language) {
        return language;
    }

    @Override
    protected String encode(List<TextSegment> segments) {
        StringBuilder sb = new StringBuilder();
        for (TextSegment segment : segments)
            sb.append(segment.isToken() ? "{" + segment.token() + "}" : segment.text());
        return sb.toString();
    }

    @Override
    protected List<TextSegment> decode(String translated) {
        List<TextSegment> segments = new ArrayList<>();
        Matcher m = TOKEN.matcher(translated);
        int last = 0;
        while (m.find()) {
            if (m.start() > last)
                segments.add(TextSegment.text(translated.substring(last, m.start())));
            segments.add(TextSegment.token(parseDigits(m.group(1))));
            last = m.end();
        }
        if (last < translated.length())
            segments.add(TextSegment.text(translated.substring(last)));
        return segments;
    }

    @Override
    protected List<String> translateEncoded(List<String> texts, String targetLanguage)
            throws IOException, InterruptedException {
        List<String> result = new ArrayList<>(texts.size());
        for (int i = 0; i < texts.size(); i++) {
            if (i > 0)
                Thread.sleep(REQUEST_DELAY_MS);
            result.add(translateOne(texts.get(i), targetLanguage));
        }
        return result;
    }

    private String translateOne(String text, String targetLanguage) throws IOException, InterruptedException {
        String query = "client=gtx&dt=t"
                + "&sl=" + SOURCE_LANGUAGE
                + "&tl=" + URLEncoder.encode(targetLanguage, StandardCharsets.UTF_8)
                + "&q=" + URLEncoder.encode(text, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder(URI.create(url + "?" + query))
                .timeout(Duration.ofSeconds(30))
                .header("User-Agent", "Mozilla/5.0")
                .GET()
                .build();

        String body = send(request);
        try {
            JsonArray parts = JsonParser.parseString(body).getAsJsonArray().get(0).getAsJsonArray();
            StringBuilder sb = new StringBuilder();
            for (JsonElement part : parts) {
                JsonElement translated = part.getAsJsonArray().get(0);
                if (!translated.isJsonNull())
                    sb.append(translated.getAsString());
            }
            return sb.toString();
        } catch (RuntimeException e) {
            throw new IOException("Unexpected Google Translate response: " + body, e);
        }
    }

    private static int parseDigits(String digits) {
        int value = 0;
        for (int i = 0; i < digits.length(); i++)
            value = value * 10 + Character.getNumericValue(digits.charAt(i));
        return value;
    }
}

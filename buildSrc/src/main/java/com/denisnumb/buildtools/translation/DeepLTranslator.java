package com.denisnumb.buildtools.translation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class DeepLTranslator extends Translator {
    private static final String FREE_URL = "https://api-free.deepl.com/v2/translate";
    private static final String PRO_URL = "https://api.deepl.com/v2/translate";
    private static final int BATCH_SIZE = 50;
    private static final String CONTEXT = "Short UI and chat messages of a Minecraft mod that connects "
            + "the in-game chat with Discord. Keep the tone short and neutral, like game interface text.";
    private static final Pattern TOKEN = Pattern.compile("<x\\s+i\\s*=\\s*\"(\\d+)\"\\s*/>");

    private static final Map<String, String> KNOWN_CODES = Map.of(
            "zh_cn", "ZH-HANS",
            "zh_tw", "ZH-HANT",
            "zh_hk", "ZH-HANT",
            "pt_br", "PT-BR",
            "pt_pt", "PT-PT",
            "en_gb", "EN-GB",
            "en_us", "EN-US",
            "no_no", "NB",
            "nn_no", "NB"
    );

    private final String apiKey;
    private final String url;

    DeepLTranslator(String apiKey, String apiUrl) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("DeepL translator requires an API key: set 'locale.apiKey' "
                    + "in ~/.gradle/gradle.properties or the LOCALE_TRANSLATOR_API_KEY environment variable");
        }
        this.apiKey = apiKey.trim();
        if (apiUrl != null && !apiUrl.isBlank())
            this.url = apiUrl;
        else
            this.url = this.apiKey.endsWith(":fx") ? FREE_URL : PRO_URL;
    }

    @Override
    public String id() {
        return "deepl";
    }

    @Override
    protected Map<String, String> knownLanguageCodes() {
        return KNOWN_CODES;
    }

    @Override
    protected String defaultLanguageCode(String language) {
        return language.toUpperCase(Locale.ROOT);
    }

    @Override
    protected String encode(List<TextSegment> segments) {
        StringBuilder sb = new StringBuilder();
        for (TextSegment segment : segments) {
            if (segment.isToken())
                sb.append("<x i=\"").append(segment.token()).append("\"/>");
            else
                sb.append(escapeXml(segment.text()));
        }
        return sb.toString();
    }

    @Override
    protected List<TextSegment> decode(String translated) {
        List<TextSegment> segments = new ArrayList<>();
        Matcher m = TOKEN.matcher(translated);
        int last = 0;
        while (m.find()) {
            if (m.start() > last)
                segments.add(TextSegment.text(unescapeXml(translated.substring(last, m.start()))));
            segments.add(TextSegment.token(Integer.parseInt(m.group(1))));
            last = m.end();
        }
        if (last < translated.length())
            segments.add(TextSegment.text(unescapeXml(translated.substring(last))));
        return segments;
    }

    @Override
    protected List<String> translateEncoded(List<String> texts, String targetLanguage)
            throws IOException, InterruptedException {
        List<String> result = new ArrayList<>(texts.size());
        for (int from = 0; from < texts.size(); from += BATCH_SIZE)
            result.addAll(translateBatch(texts.subList(from, Math.min(texts.size(), from + BATCH_SIZE)), targetLanguage));
        return result;
    }

    private List<String> translateBatch(List<String> texts, String targetLanguage)
            throws IOException, InterruptedException {
        JsonObject body = new JsonObject();
        JsonArray text = new JsonArray();
        texts.forEach(text::add);
        body.add("text", text);
        body.addProperty("source_lang", SOURCE_LANGUAGE.toUpperCase(Locale.ROOT));
        body.addProperty("target_lang", targetLanguage);
        body.addProperty("tag_handling", "xml");
        body.addProperty("preserve_formatting", true);
        body.addProperty("context", CONTEXT);

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .header("Authorization", "DeepL-Auth-Key " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        String response = send(request);
        try {
            List<String> result = new ArrayList<>(texts.size());
            for (JsonElement translation : JsonParser.parseString(response).getAsJsonObject().getAsJsonArray("translations"))
                result.add(translation.getAsJsonObject().get("text").getAsString());
            return result;
        } catch (RuntimeException e) {
            throw new IOException("Unexpected DeepL response: " + response, e);
        }
    }

    private static String escapeXml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String unescapeXml(String text) {
        return text.replace("&lt;", "<").replace("&gt;", ">")
                .replace("&quot;", "\"").replace("&apos;", "'")
                .replace("&amp;", "&");
    }
}

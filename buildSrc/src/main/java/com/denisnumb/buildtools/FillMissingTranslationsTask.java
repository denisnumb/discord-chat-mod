package com.denisnumb.buildtools;

import com.denisnumb.buildtools.translation.LangFile;
import com.denisnumb.buildtools.translation.ProtectedText;
import com.denisnumb.buildtools.translation.TextSegment;
import com.denisnumb.buildtools.translation.TranslationException;
import com.denisnumb.buildtools.translation.Translator;
import com.google.gson.JsonParseException;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public abstract class FillMissingTranslationsTask extends DefaultTask {
    @InputFile
    public abstract RegularFileProperty getSourceJson();

    @Internal
    public abstract DirectoryProperty getLangDirectory();

    @Input
    public abstract Property<String> getTranslator();

    @Internal
    public abstract Property<String> getApiKey();

    @Input
    @Optional
    public abstract Property<String> getApiUrl();

    @Input
    public abstract MapProperty<String, String> getLanguageCodes();

    @Input
    @Option(option = "check", description = "Only report missing keys, do not translate or write anything.")
    public abstract Property<Boolean> getCheckOnly();

    @Input
    public abstract Property<Boolean> getFailOnError();

    @Input
    public abstract Property<Boolean> getRemoveObsoleteKeys();

    public FillMissingTranslationsTask() {
        setGroup("localization");
        setDescription("Machine-translates keys missing in lang files using en_us.json as the reference.");
        doNotTrackState("Edits localization files in place; does nothing when no keys are missing");
        getTranslator().convention("google");
        getCheckOnly().convention(false);
        getFailOnError().convention(false);
        getRemoveObsoleteKeys().convention(false);
    }

    @TaskAction
    public void fill() throws IOException, InterruptedException {
        Path sourcePath = getSourceJson().get().getAsFile().toPath();
        LangFile source = LangFile.read(sourcePath);

        List<Path> targets;
        try (Stream<Path> files = Files.list(getLangDirectory().get().getAsFile().toPath())) {
            targets = files
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .filter(path -> !path.getFileName().equals(sourcePath.getFileName()))
                    .sorted()
                    .toList();
        }

        List<String> errors = new ArrayList<>();
        Translator translator = null;

        for (Path target : targets) {
            String fileName = target.getFileName().toString();
            String locale = fileName.substring(0, fileName.length() - ".json".length());

            LangFile file;
            try {
                file = LangFile.read(target);
            } catch (JsonParseException | IOException e) {
                errors.add(fileName + ": cannot be parsed (" + e.getMessage() + ")");
                continue;
            }

            List<String> missing = new ArrayList<>();
            for (String key : source.entries.keySet()) {
                if (file.entries.get(key) == null)
                    missing.add(key);
            }
            List<String> obsolete = new ArrayList<>();
            for (String key : file.entries.keySet()) {
                if (!source.entries.containsKey(key))
                    obsolete.add(key);
            }

            if (!obsolete.isEmpty()) {
                getLogger().warn("{}: {} key(s) not present in {}{}: {}", fileName, obsolete.size(),
                        sourcePath.getFileName(), getRemoveObsoleteKeys().get() ? " will be removed" : "", obsolete);
            }
            boolean removeObsolete = getRemoveObsoleteKeys().get() && !obsolete.isEmpty();
            if (missing.isEmpty() && !removeObsolete) {
                getLogger().info("{}: up to date", fileName);
                continue;
            }

            if (getCheckOnly().get()) {
                getLogger().lifecycle("{}: {} missing key(s)", fileName, missing.size());
                missing.forEach(key -> getLogger().lifecycle("  - {}", key));
                continue;
            }

            Map<String, String> translated = new LinkedHashMap<>();
            if (!missing.isEmpty()) {
                if (translator == null) {
                    try {
                        translator = Translator.create(getTranslator().get(), getApiKey().getOrNull(), getApiUrl().getOrNull());
                    } catch (IllegalArgumentException e) {
                        errors.add(e.getMessage());
                        break;
                    }
                }
                translated = translateMissing(translator, fileName, locale, missing, source.entries, errors);
            }

            if (translated.isEmpty() && !removeObsolete)
                continue;

            Map<String, String> result = new LinkedHashMap<>();
            for (String key : source.entries.keySet()) {
                String value = file.entries.get(key);
                if (value == null)
                    value = translated.get(key);
                if (value != null)
                    result.put(key, value);
            }
            Set<String> groupStarts = new HashSet<>(source.groupStarts);
            if (!removeObsolete && !obsolete.isEmpty()) {
                groupStarts.add(obsolete.get(0));
                obsolete.forEach(key -> result.put(key, file.entries.get(key)));
            }

            Files.writeString(target,
                    LangFile.render(result, groupStarts, file.lineSeparator, file.trailingNewline),
                    StandardCharsets.UTF_8);

            getLogger().lifecycle("{}: added {} of {} missing key(s){}", fileName, translated.size(), missing.size(),
                    removeObsolete ? ", removed " + obsolete.size() + " obsolete key(s)" : "");
            translated.forEach((key, value) -> getLogger().lifecycle("  + {}: {}", key, value));
        }

        if (!errors.isEmpty()) {
            String message = "Some translations were not added:\n  " + String.join("\n  ", errors);
            if (getFailOnError().get())
                throw new GradleException(message);
            getLogger().warn(message + "\nThese keys will be retried on the next run; "
                    + "the game falls back to en_us for them meanwhile.");
        }
    }

    /**
     * One line of a multiline value; lines are translated separately so line breaks stay in place.
     */
    private static final class Line {
        final String key;
        final String leading;
        final String trailing;
        final ProtectedText text;
        String result;

        Line(String key, String line) {
            int start = 0;
            int end = line.length();
            while (start < end && Character.isWhitespace(line.charAt(start)))
                start++;
            while (end > start && Character.isWhitespace(line.charAt(end - 1)))
                end--;
            this.key = key;
            this.leading = line.substring(0, start);
            this.trailing = line.substring(end);
            this.text = ProtectedText.of(line.substring(start, end));
            if (!text.isTranslatable())
                this.result = line;
        }
    }

    private Map<String, String> translateMissing(Translator translator, String fileName, String locale,
                                                 List<String> missing, Map<String, String> sourceEntries,
                                                 List<String> errors) throws InterruptedException {
        String targetLanguage = getLanguageCodes().get().getOrDefault(locale, translator.targetLanguage(locale));
        getLogger().lifecycle("{}: translating {} key(s) with {} ({} → {})", fileName, missing.size(),
                translator.id(), Translator.SOURCE_LANGUAGE, targetLanguage);

        Map<String, List<Line>> linesByKey = new LinkedHashMap<>();
        List<Line> pending = new ArrayList<>();
        for (String key : missing) {
            List<Line> lines = new ArrayList<>();
            for (String line : sourceEntries.get(key).split("\n", -1)) {
                Line parsed = new Line(key, line);
                lines.add(parsed);
                if (parsed.result == null)
                    pending.add(parsed);
            }
            linesByKey.put(key, lines);
        }

        Set<String> failedKeys = new HashSet<>();
        if (!pending.isEmpty()) {
            List<List<TextSegment>> translations;
            try {
                translations = translator.translate(pending.stream().map(line -> line.text).toList(), targetLanguage);
            } catch (IOException e) {
                errors.add(fileName + ": " + translator.id() + " request failed (" + e.getMessage() + "), "
                        + missing.size() + " key(s) skipped");
                return Map.of();
            }

            for (int i = 0; i < pending.size(); i++) {
                Line line = pending.get(i);
                try {
                    line.result = line.leading + line.text.restore(translations.get(i)) + line.trailing;
                } catch (TranslationException e) {
                    if (failedKeys.add(line.key))
                        errors.add(fileName + ": " + line.key + " skipped, " + e.getMessage());
                }
            }
        }

        Map<String, String> result = new LinkedHashMap<>();
        linesByKey.forEach((key, lines) -> {
            if (!failedKeys.contains(key))
                result.put(key, String.join("\n", lines.stream().map(line -> line.result).toList()));
        });
        return result;
    }
}

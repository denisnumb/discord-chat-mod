package com.denisnumb.discord_chat_mod.config.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class ConfigParameterBuilder<R, T> {
    private final String key;
    private R defaultValue;
    private String comment;
    private Function<R, R> normalizer;
    private BiFunction<R, R, R> validator;
    private final List<ConfigMigration<R>> migrations;
    private final List<R> legacyDefaultValues;
    private final Function<R, T> transform;

    private ConfigParameterBuilder(String key,
                                   R defaultValue,
                                   String comment,
                                   Function<R, R> normalizer,
                                   BiFunction<R, R, R> validator,
                                   List<ConfigMigration<R>> migrations,
                                   List<R> legacyDefaultValues,
                                   Function<R, T> transform) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.comment = comment;
        this.normalizer = normalizer;
        this.validator = validator;
        this.migrations = migrations;
        this.legacyDefaultValues = legacyDefaultValues;
        this.transform = transform;
    }

    static <R> ConfigParameterBuilder<R, R> create(String key, R defaultValue) {
        return new ConfigParameterBuilder<>(key, defaultValue, "", null, null,
                new ArrayList<>(), new ArrayList<>(), Function.identity());
    }

    public ConfigParameterBuilder<R, T> defaultValue(R defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public ConfigParameterBuilder<R, T> comment(String comment) {
        this.comment = comment;
        return this;
    }

    public <NT> ConfigParameterBuilder<R, NT> transform(Function<T, NT> transform) {
        Function<R, NT> composed = this.transform.andThen(transform);
        return new ConfigParameterBuilder<>(key, defaultValue, comment, normalizer, validator,
                migrations, legacyDefaultValues, composed);
    }

    public ConfigParameterBuilder<R, T> normalize(Function<R, R> normalize) {
        this.normalizer = this.normalizer == null ? normalize : this.normalizer.andThen(normalize);
        return this;
    }

    public ConfigParameterBuilder<R, T> range(int min, int max) {
        return range(min, max, false);
    }

    public ConfigParameterBuilder<R, T> rangeWithComment(int min, int max) {
        return range(min, max, true);
    }

    @SuppressWarnings("unchecked")
    private ConfigParameterBuilder<R, T> range(int min, int max, boolean addComment) {
        Function<R, R> clamp = value -> {
            int intValue = (Integer) value;
            if (intValue < min) intValue = min;
            if (intValue > max) intValue = max;
            return (R) Integer.valueOf(intValue);
        };

        this.normalizer = this.normalizer == null ? clamp : this.normalizer.andThen(clamp);
        if (addComment){
            this.comment += String.format((comment.isBlank() ? "" : "%n") + " Default: %s%n Range: %d ~ %d", defaultValue, min, max);
        }

        return this;
    }

    public ConfigParameterBuilder<R, T> validator(BiFunction<R, R, R> validator) {
        this.validator = validator;
        return this;
    }

    public ConfigParameterBuilder<R, T> migrateFrom(String oldKey, Function<R, R> transform) {
        this.migrations.add(new ConfigMigration<>(false, oldKey, transform));
        return this;
    }

    public ConfigParameterBuilder<R, T> migrateFrom(String oldKey) {
        return migrateFrom(oldKey, Function.identity());
    }

    public ConfigParameterBuilder<R, T> migrateFromParent(String oldKey, Function<R, R> transform) {
        this.migrations.add(new ConfigMigration<>(true, oldKey, transform));
        return this;
    }

    public ConfigParameterBuilder<R, T> migrateFromParent(String oldKey) {
        return migrateFromParent(oldKey, Function.identity());
    }

    public ConfigParameterBuilder<R, T> migrateDefaultValue(R oldDefaultValue) {
        this.legacyDefaultValues.add(oldDefaultValue);
        return this;
    }

    public ConfigParameter<R, T> build() {
        return new ConfigParameter<>(key, defaultValue, comment, normalizer, validator,
                List.copyOf(migrations), List.copyOf(legacyDefaultValues), transform);
    }
}
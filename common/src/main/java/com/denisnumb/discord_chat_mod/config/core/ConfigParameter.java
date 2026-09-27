package com.denisnumb.discord_chat_mod.config.core;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;


public final class ConfigParameter<R, T> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final String key;
    private final R defaultValue;
    private final String comment;
    private final Function<R, R> normalizer;
    private final BiFunction<R, R, R> validator;
    private final List<ConfigMigration<R>> migrations;
    private final List<R> legacyDefaultValues;
    private final Function<R, T> transform;

    private volatile T value;

    ConfigParameter(String key,
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
        this.value = transform.apply(defaultValue);
    }

    public static <R> ConfigParameterBuilder<R, R> builder(String key, R defaultValue) {
        return ConfigParameterBuilder.create(key, defaultValue);
    }

    public static ConfigParameterBuilder<Integer, Integer> ofInt(String key) {
        return ConfigParameterBuilder.create(key, null);
    }

    public static ConfigParameterBuilder<Boolean, Boolean> ofBoolean(String key) {
        return ConfigParameterBuilder.create(key, null);
    }

    public static ConfigParameterBuilder<String, String> ofString(String key) {
        return ConfigParameterBuilder.create(key, null);
    }

    public static <E> ConfigParameterBuilder<List<E>, List<E>> ofList(String key) {
        return ConfigParameterBuilder.create(key, null);
    }

    public T get() {
        return value;
    }

    public String key() {
        return key;
    }

    public R defaultValue() {
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    void load(CommentedConfig existingConfig, CommentedConfig newConfig, CommentedConfig parentConfig) {
        R migrated = null;

        if (!existingConfig.contains(key)) {
            for (ConfigMigration<R> migration : migrations) {
                CommentedConfig source = migration.resolveSource(existingConfig, parentConfig);

                if (source.contains(migration.oldKey())) {
                    Object rawOld = source.get(migration.oldKey());
                    migrated = migration.transform((R) rawOld);
                    LOGGER.info("[config] Migrating \"{}\" -> \"{}\"", migration.oldKey(), key);
                    source.remove(migration.oldKey());
                    break;
                }
            }
        }

        R raw = migrated != null ? migrated : existingConfig.getOrElse(key, defaultValue);

        if (legacyDefaultValues.contains(raw)) {
            LOGGER.info("[config] Updating default value for \"{}\"", key);
            raw = defaultValue;
        }

        R normalized = normalizer != null ? normalizer.apply(raw) : raw;
        R validated = validator != null ? validator.apply(normalized, defaultValue) : normalized;

        newConfig.set(key, validated);
        if (comment != null && !comment.isBlank()) {
            newConfig.setComment(key, comment);
        }

        this.value = transform.apply(validated);
    }
}
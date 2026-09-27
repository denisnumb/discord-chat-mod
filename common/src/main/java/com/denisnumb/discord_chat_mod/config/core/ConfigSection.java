package com.denisnumb.discord_chat_mod.config.core;

import com.electronwill.nightconfig.core.CommentedConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public abstract class ConfigSection {
    protected ConfigSection() {}

    public static List<ConfigParameter<?, ?>> collectParameters(Class<?> sectionClass) {
        List<ConfigParameter<?, ?>> parameters = new ArrayList<>();

        for (Field field : sectionClass.getDeclaredFields()) {
            if (!ConfigParameter.class.isAssignableFrom(field.getType()))
                continue;
            if (!Modifier.isStatic(field.getModifiers()))
                continue;

            field.setAccessible(true);
            try {
                ConfigParameter<?, ?> parameter = (ConfigParameter<?, ?>) field.get(null);
                if (parameter != null)
                    parameters.add(parameter);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Cannot access config parameter field: " + field, e);
            }
        }

        return parameters;
    }

    public static void loadParameters(List<ConfigParameter<?, ?>> parameters, CommentedConfig existingConfig, CommentedConfig newConfig) {
        loadParameters(parameters, existingConfig, newConfig, null);
    }

    public static void loadParameters(List<ConfigParameter<?, ?>> parameters, CommentedConfig existingConfig, CommentedConfig newConfig, CommentedConfig parentConfig) {
        for (ConfigParameter<?, ?> parameter : parameters) {
            parameter.load(existingConfig, newConfig, parentConfig);
        }
    }

    public static void loadAll(Class<?> sectionClass, CommentedConfig existingConfig, CommentedConfig newConfig) {
        loadParameters(collectParameters(sectionClass), existingConfig, newConfig, null);
    }

    public static void loadAll(Class<?> sectionClass, CommentedConfig existingConfig, CommentedConfig newConfig, CommentedConfig parentConfig) {
        loadParameters(collectParameters(sectionClass), existingConfig, newConfig, parentConfig);
    }
}
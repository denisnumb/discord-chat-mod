package com.denisnumb.discord_chat_mod.config.core;

import com.electronwill.nightconfig.core.CommentedConfig;

import java.util.function.Function;


record ConfigMigration<R>(boolean fromParent, String oldKey, Function<R, R> transform) {
    R transform(R oldValue) {
        return transform.apply(oldValue);
    }

    CommentedConfig resolveSource(CommentedConfig ownSectionConfig, CommentedConfig parentConfig) {
        if (!fromParent)
            return ownSectionConfig;

        if (parentConfig == null)
            throw new IllegalStateException(
                    "Migration for key \"" + oldKey + "\" is declared with migrateFromParent(...), "
                            + "but no parent config was passed to ConfigSection.loadAll(...)");

        return parentConfig;
    }
}
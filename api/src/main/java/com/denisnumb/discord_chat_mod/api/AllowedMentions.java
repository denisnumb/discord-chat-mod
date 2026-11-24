package com.denisnumb.discord_chat_mod.api;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Which mentions in a message actually notify their targets. Mentions are still rendered either way.
 * The default for every message is {@link #none()}: a mod relaying player-written text cannot be abused to ping {@code @everyone}.
 *
 * @since 1.0.0
 */
public final class AllowedMentions {
    private static final AllowedMentions NONE = new AllowedMentions(EnumSet.noneOf(MentionType.class));

    private final Set<MentionType> types;

    private AllowedMentions(EnumSet<MentionType> types) {
        this.types = Collections.unmodifiableSet(types);
    }

    /**
     * No mention notifies anyone.
     */
    public static AllowedMentions none() {
        return NONE;
    }

    /**
     * Only mentions of the given kinds notify.
     */
    public static AllowedMentions of(MentionType... types) {
        EnumSet<MentionType> set = EnumSet.noneOf(MentionType.class);
        Collections.addAll(set, types);
        return set.isEmpty() ? NONE : new AllowedMentions(set);
    }

    public Set<MentionType> types() {
        return types;
    }

    public boolean allows(MentionType type) {
        return types.contains(type);
    }
}

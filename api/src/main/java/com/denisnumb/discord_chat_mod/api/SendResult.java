package com.denisnumb.discord_chat_mod.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Result of {@link ModHandle#send}: the overall status and one {@link Delivery} per channel the message was routed
 * to (the mod supports several guilds, and built-in channels may be duplicated to the default channel).
 *
 * @since 1.0.0
 */
public final class SendResult {
    private final SendStatus status;
    private final List<Delivery> deliveries;

    private SendResult(SendStatus status, List<Delivery> deliveries) {
        this.status = status;
        this.deliveries = deliveries;
    }

    /**
     * A result without deliveries: nothing was attempted ({@link SendStatus#NOT_CONNECTED},
     * {@link SendStatus#RATE_LIMITED}...). Used by the implementation.
     */
    public static SendResult rejected(SendStatus status) {
        Objects.requireNonNull(status, "status");
        if (status == SendStatus.SENT || status == SendStatus.PARTIALLY_SENT)
            throw new IllegalArgumentException("A rejected result cannot be " + status);
        return new SendResult(status, List.of());
    }

    /**
     * Combines per-channel deliveries into a result. Used by the implementation.
     */
    public static SendResult of(List<Delivery> deliveries) {
        List<Delivery> copy = Collections.unmodifiableList(new ArrayList<>(deliveries));
        if (copy.isEmpty())
            return new SendResult(SendStatus.NO_GUILDS, copy);

        long sent = copy.stream().filter(d -> d.status() == SendStatus.SENT).count();
        long failed = copy.stream().filter(d -> d.status() == SendStatus.FAILED).count();

        SendStatus status;
        if (sent > 0)
            status = failed > 0 ? SendStatus.PARTIALLY_SENT : SendStatus.SENT;
        else
            status = failed > 0 ? SendStatus.FAILED : SendStatus.CHANNEL_DISABLED;

        return new SendResult(status, copy);
    }

    public SendStatus status() {
        return status;
    }

    public List<Delivery> deliveries() {
        return deliveries;
    }

    /**
     * @return {@code true} if the message reached at least one channel
     */
    public boolean isSent() {
        return status == SendStatus.SENT || status == SendStatus.PARTIALLY_SENT;
    }

    @Override
    public String toString() {
        return "SendResult{status=" + status + ", deliveries=" + deliveries + "}";
    }
}

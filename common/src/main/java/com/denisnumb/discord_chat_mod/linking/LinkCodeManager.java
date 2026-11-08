package com.denisnumb.discord_chat_mod.linking;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.*;

public final class LinkCodeManager {
    private static final String ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long FAILED_ATTEMPTS_WINDOW_MS = Duration.ofMinutes(10).toMillis();

    public record PendingCode(String code, UUID uuid, String playerName, long expiresAt) {
        public boolean isExpired(long now) {
            return now >= expiresAt;
        }

        public long secondsLeft(long now) {
            return Math.max(0, (expiresAt - now) / 1000);
        }
    }

    private final SecureRandom random = new SecureRandom();
    private final Map<String, PendingCode> byCode = new HashMap<>();
    private final Map<UUID, PendingCode> byUuid = new HashMap<>();
    private final Map<String, Deque<Long>> failedAttempts = new HashMap<>();

    public synchronized PendingCode getOrCreate(UUID uuid, String playerName, Duration ttl) {
        long now = System.currentTimeMillis();
        removeExpired(now);

        PendingCode existing = byUuid.get(uuid);
        if (existing != null && existing.playerName().equals(playerName))
            return existing;
        if (existing != null)
            invalidate(uuid);

        String code;
        do {
            code = generateCode();
        } while (byCode.containsKey(code));

        PendingCode pending = new PendingCode(code, uuid, playerName, now + ttl.toMillis());
        byCode.put(code, pending);
        byUuid.put(uuid, pending);
        return pending;
    }

    public synchronized Optional<PendingCode> find(String rawCode) {
        removeExpired(System.currentTimeMillis());
        return Optional.ofNullable(byCode.get(normalize(rawCode)));
    }

    public synchronized void invalidate(UUID uuid) {
        PendingCode removed = byUuid.remove(uuid);
        if (removed != null)
            byCode.remove(removed.code());
    }

    public synchronized boolean isRateLimited(String discordId) {
        Deque<Long> attempts = failedAttempts.get(discordId);
        if (attempts == null)
            return false;

        long now = System.currentTimeMillis();
        while (!attempts.isEmpty() && now - attempts.peekFirst() > FAILED_ATTEMPTS_WINDOW_MS)
            attempts.pollFirst();

        if (attempts.isEmpty())
            failedAttempts.remove(discordId);

        return attempts.size() >= MAX_FAILED_ATTEMPTS;
    }

    public synchronized void registerFailedAttempt(String discordId) {
        failedAttempts.computeIfAbsent(discordId, id -> new ArrayDeque<>()).addLast(System.currentTimeMillis());
    }

    public synchronized void clearFailedAttempts(String discordId) {
        failedAttempts.remove(discordId);
    }

    public synchronized void clear() {
        byCode.clear();
        byUuid.clear();
        failedAttempts.clear();
    }

    static String normalize(String rawCode) {
        return rawCode == null
                ? ""
                : rawCode.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++)
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        return sb.toString();
    }

    private void removeExpired(long now) {
        Iterator<PendingCode> iterator = byCode.values().iterator();
        while (iterator.hasNext()) {
            PendingCode pending = iterator.next();
            if (pending.isExpired(now)) {
                iterator.remove();
                byUuid.remove(pending.uuid(), pending);
            }
        }
    }
}

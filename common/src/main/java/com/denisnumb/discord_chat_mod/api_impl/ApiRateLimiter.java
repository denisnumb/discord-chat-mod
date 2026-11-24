package com.denisnumb.discord_chat_mod.api_impl;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class ApiRateLimiter {
    private static final long WINDOW_MILLIS = 60_000;

    private final Map<String, Deque<Long>> windows = new ConcurrentHashMap<>();

    boolean tryAcquire(String modId, int limit) {
        if (limit <= 0)
            return true;

        long now = System.currentTimeMillis();
        Deque<Long> window = windows.computeIfAbsent(modId, id -> new ArrayDeque<>());

        synchronized (window) {
            while (!window.isEmpty() && now - window.peekFirst() >= WINDOW_MILLIS)
                window.pollFirst();

            if (window.size() >= limit)
                return false;

            window.addLast(now);
            return true;
        }
    }
}

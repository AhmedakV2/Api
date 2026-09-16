package com.aft.api.state.local;

import com.aft.api.state.CounterStore;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalCounterStore implements CounterStore {
    private final Map<String, ExpiringEntry<Long>> counters = new ConcurrentHashMap<>();

    @Override
    public Window increment(String key, Duration window) {
        long now = System.currentTimeMillis();
        long windowMillis = window.toMillis();
        ExpiringEntry<Long> entry = counters.compute(key, (ignored, current) -> {
            if (current == null || !current.alive(now)) {
                return new ExpiringEntry<>(1L, now + windowMillis);
            }
            return new ExpiringEntry<>(current.value() + 1L, current.expiresAt());
        });
        return new Window(entry.value(), Duration.ofMillis(Math.max(1L, entry.expiresAt() - now)));
    }

    @Override
    public void reset(String key, Duration window) {
        counters.remove(key);
    }

    void sweep(long now) {
        counters.entrySet().removeIf(entry -> !entry.getValue().alive(now));
    }
}

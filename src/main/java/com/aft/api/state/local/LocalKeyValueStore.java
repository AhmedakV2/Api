package com.aft.api.state.local;

import com.aft.api.state.KeyValueStore;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class LocalKeyValueStore implements KeyValueStore {
    private final Map<String, ExpiringEntry<String>> entries = new ConcurrentHashMap<>();

    @Override
    public void put(String key, String value, Duration ttl) {
        long expiresAt = (ttl == null || ttl.isZero() || ttl.isNegative())
                ? 0L
                : System.currentTimeMillis() + ttl.toMillis();
        entries.put(key, new ExpiringEntry<>(value, expiresAt));
    }

    @Override
    public Optional<String> get(String key) {
        long now = System.currentTimeMillis();
        ExpiringEntry<String> entry = entries.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        if (!entry.alive(now)) {
            entries.remove(key, entry);
            return Optional.empty();
        }
        return Optional.of(entry.value());
    }

    @Override
    public Optional<String> take(String key) {
        long now = System.currentTimeMillis();
        AtomicReference<String> taken = new AtomicReference<>();
        entries.computeIfPresent(key, (ignored, entry) -> {
            if (entry.alive(now)) {
                taken.set(entry.value());
            }
            return null;
        });
        return Optional.ofNullable(taken.get());
    }

    @Override
    public boolean contains(String key) {
        return get(key).isPresent();
    }

    @Override
    public void delete(String key) {
        entries.remove(key);
    }

    void sweep(long now) {
        entries.entrySet().removeIf(entry -> !entry.getValue().alive(now));
    }
}

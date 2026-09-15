package com.aft.api.state.coherence;

import com.aft.api.state.KeyValueStore;
import com.tangosol.net.NamedCache;
import java.time.Duration;
import java.util.Optional;

public class CoherenceKeyValueStore implements KeyValueStore {
    private final NamedCache<String, String> cache;

    public CoherenceKeyValueStore(NamedCache<String, String> cache) {
        this.cache = cache;
    }

    @Override
    public void put(String key, String value, Duration ttl) {
        cache.put(key, value, ttl == null || ttl.isZero() ? NamedCache.EXPIRY_DEFAULT : ttl.toMillis());
    }

    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(cache.get(key));
    }

    @Override
    public Optional<String> take(String key) {
        return Optional.ofNullable(cache.remove(key));
    }

    @Override
    public boolean contains(String key) {
        return cache.containsKey(key);
    }

    @Override
    public void delete(String key) {
        cache.remove(key);
    }
}

package com.aft.api.state.coherence;

import com.aft.api.state.CounterStore;
import com.tangosol.net.NamedCache;
import com.tangosol.util.processor.NumberIncrementor;
import java.time.Duration;

public class CoherenceCounterStore implements CounterStore {
    private static final long RETENTION_FACTOR = 2L;

    private final NamedCache<String, Long> cache;

    public CoherenceCounterStore(NamedCache<String, Long> cache) {
        this.cache = cache;
    }

    @Override
    public Window increment(String key, Duration window) {
        long windowMillis = window.toMillis();
        long now = System.currentTimeMillis();
        long slot = now / windowMillis;
        String slotKey = key + ":" + slot;

        Long count = cache.invoke(slotKey, incrementor());
        if (count == null) {
            cache.put(slotKey, 1L, windowMillis * RETENTION_FACTOR);
            count = 1L;
        }
        long windowEnd = (slot + 1) * windowMillis;
        return new Window(count, Duration.ofMillis(Math.max(1L, windowEnd - now)));
    }

    @Override
    public void reset(String key, Duration window) {
        long slot = System.currentTimeMillis() / window.toMillis();
        cache.remove(key + ":" + slot);
    }

    private NumberIncrementor<String, Long, Long> incrementor() {
        return new NumberIncrementor<>((String) null, 1L, false);
    }
}

package com.aft.api.state.local;

import com.aft.api.state.CounterStore;
import com.aft.api.state.KeyValueStore;
import com.aft.api.state.StateStoreProvider;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LocalStateStoreProvider implements StateStoreProvider {
    private static final long SWEEP_INTERVAL_SECONDS = 60L;

    private final Map<String, LocalKeyValueStore> keyValueStores = new ConcurrentHashMap<>();
    private final Map<String, LocalCounterStore> counterStores = new ConcurrentHashMap<>();
    private final List<Runnable> sweepers = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService sweeper =
            Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().name("aft-state-sweeper").factory());

    public LocalStateStoreProvider() {
        sweeper.scheduleAtFixedRate(this::sweep, SWEEP_INTERVAL_SECONDS, SWEEP_INTERVAL_SECONDS,
                TimeUnit.SECONDS);
    }

    @Override
    public KeyValueStore keyValue(String namespace) {
        return keyValueStores.computeIfAbsent(namespace, ignored -> {
            LocalKeyValueStore store = new LocalKeyValueStore();
            sweepers.add(() -> store.sweep(System.currentTimeMillis()));
            return store;
        });
    }

    @Override
    public CounterStore counter(String namespace) {
        return counterStores.computeIfAbsent(namespace, ignored -> {
            LocalCounterStore store = new LocalCounterStore();
            sweepers.add(() -> store.sweep(System.currentTimeMillis()));
            return store;
        });
    }

    @PreDestroy
    public void close() {
        sweeper.shutdownNow();
    }

    private void sweep() {
        sweepers.forEach(Runnable::run);
    }
}

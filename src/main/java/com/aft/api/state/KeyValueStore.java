package com.aft.api.state;

import java.time.Duration;
import java.util.Optional;

public interface KeyValueStore {
    void put(String key, String value, Duration ttl);

    Optional<String> get(String key);

    Optional<String> take(String key);

    boolean contains(String key);

    void delete(String key);
}

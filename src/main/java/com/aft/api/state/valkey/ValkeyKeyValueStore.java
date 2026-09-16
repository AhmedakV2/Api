package com.aft.api.state.valkey;

import com.aft.api.state.KeyValueStore;
import java.time.Duration;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;

public class ValkeyKeyValueStore implements KeyValueStore {
    private final StringRedisTemplate template;
    private final String prefix;

    public ValkeyKeyValueStore(StringRedisTemplate template, String prefix) {
        this.template = template;
        this.prefix = prefix;
    }

    @Override
    public void put(String key, String value, Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            template.opsForValue().set(prefix + key, value);
        } else {
            template.opsForValue().set(prefix + key, value, ttl);
        }
    }

    @Override
    public Optional<String> get(String key) {
        return Optional.ofNullable(template.opsForValue().get(prefix + key));
    }

    @Override
    public Optional<String> take(String key) {
        return Optional.ofNullable(template.opsForValue().getAndDelete(prefix + key));
    }

    @Override
    public boolean contains(String key) {
        return Boolean.TRUE.equals(template.hasKey(prefix + key));
    }

    @Override
    public void delete(String key) {
        template.delete(prefix + key);
    }
}

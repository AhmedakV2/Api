package com.aft.api.state.valkey;

import com.aft.api.state.CounterStore;
import java.time.Duration;
import java.util.List;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

public class ValkeyCounterStore implements CounterStore {
    private final StringRedisTemplate template;
    private final RedisScript<List> script;
    private final String prefix;

    public ValkeyCounterStore(StringRedisTemplate template, RedisScript<List> script, String prefix) {
        this.template = template;
        this.script = script;
        this.prefix = prefix;
    }

    @Override
    public Window increment(String key, Duration window) {
        List<?> result = template.execute(script, List.of(prefix + key), String.valueOf(window.toMillis()));
        if (result == null || result.size() < 2) {
            return new Window(1L, window);
        }
        long count = ((Number) result.get(0)).longValue();
        long remaining = ((Number) result.get(1)).longValue();
        return new Window(count, Duration.ofMillis(Math.max(1L, remaining)));
    }

    @Override
    public void reset(String key, Duration window) {
        template.delete(prefix + key);
    }
}

package com.aft.api.state.valkey;

import com.aft.api.state.CounterStore;
import com.aft.api.state.KeyValueStore;
import com.aft.api.state.StateStoreProvider;
import java.util.List;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

public class ValkeyStateStoreProvider implements StateStoreProvider {
    private final StringRedisTemplate template;
    private final RedisScript<List> counterScript;
    private final String prefix;

    public ValkeyStateStoreProvider(StringRedisTemplate template, RedisScript<List> counterScript, String prefix) {
        this.template = template;
        this.counterScript = counterScript;
        this.prefix = prefix;
    }

    @Override
    public KeyValueStore keyValue(String namespace) {
        return new ValkeyKeyValueStore(template, prefix + namespace + ":");
    }

    @Override
    public CounterStore counter(String namespace) {
        return new ValkeyCounterStore(template, counterScript, prefix + namespace + ":");
    }
}

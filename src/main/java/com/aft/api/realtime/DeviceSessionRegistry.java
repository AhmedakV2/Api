package com.aft.api.realtime;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class DeviceSessionRegistry {
    private static final String KEY_PREFIX = "aft:ws:device:";
    private static final Duration TTL = Duration.ofHours(12);

    private final StringRedisTemplate redis;
    private final String instanceId = UUID.randomUUID().toString();

    public DeviceSessionRegistry(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void online(UUID deviceId, String sessionId) {
        redis.opsForValue().set(key(deviceId), instanceId + "|" + sessionId, TTL);
    }

    public void offline(UUID deviceId) {
        redis.delete(key(deviceId));
    }

    public boolean isOnline(UUID deviceId) {
        return Boolean.TRUE.equals(redis.hasKey(key(deviceId)));
    }

    public Optional<String> ownerInstance(UUID deviceId) {
        String value = redis.opsForValue().get(key(deviceId));
        return value == null ? Optional.empty() : Optional.of(value.split("\\|")[0]);
    }

    public String instanceId() {
        return instanceId;
    }

    private String key(UUID deviceId) {
        return KEY_PREFIX + deviceId;
    }
}

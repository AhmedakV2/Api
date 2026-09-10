package com.aft.api.security;

import com.aft.api.config.SecurityProperties;
import java.time.Duration;
import java.util.Locale;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {
    private static final String KEY_PREFIX = "aft:login:fail:";

    private final StringRedisTemplate redis;
    private final SecurityProperties properties;

    public LoginAttemptService(StringRedisTemplate redis, SecurityProperties properties) {
        this.redis = redis;
        this.properties = properties;
    }

    public boolean isLocked(String email) {
        String value = redis.opsForValue().get(key(email));
        return value != null && Integer.parseInt(value) >= properties.lockout().maxAttempts();
    }

    public void recordFailure(String email) {
        String redisKey = key(email);
        Long current = redis.opsForValue().increment(redisKey);
        if (current != null && current == 1L) {
            redis.expire(redisKey, properties.lockout().duration());
        }
    }

    public void reset(String email) {
        redis.delete(key(email));
    }

    public Duration remainingLock(String email) {
        Long seconds = redis.getExpire(key(email));
        return (seconds == null || seconds < 0) ? Duration.ZERO : Duration.ofSeconds(seconds);
    }

    private String key(String email) {
        return KEY_PREFIX + email.toLowerCase(Locale.ROOT);
    }
}

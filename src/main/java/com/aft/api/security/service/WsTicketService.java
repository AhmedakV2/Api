package com.aft.api.security.service;

import com.aft.api.config.SecurityProperties;
import com.aft.api.common.exception.ApiException;
import com.aft.api.common.exception.ErrorCode;
import com.aft.api.security.TokenHashing;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class WsTicketService {
    private static final String KEY_PREFIX = "aft:ws:ticket:";

    private final StringRedisTemplate redis;
    private final SecurityProperties properties;

    public WsTicketService(StringRedisTemplate redis, SecurityProperties properties) {
        this.redis = redis;
        this.properties = properties;
    }

    public Ticket issue(UUID userId) {
        String raw = TokenHashing.randomSecret();
        redis.opsForValue().set(KEY_PREFIX + TokenHashing.sha256Hex(raw), userId.toString(),
                properties.wsTicketTtl());
        return new Ticket(raw, properties.wsTicketTtl().toSeconds());
    }

    public UUID consume(String rawTicket) {
        String key = KEY_PREFIX + TokenHashing.sha256Hex(rawTicket);
        String userId = redis.opsForValue().getAndDelete(key);
        if (userId == null) {
            throw new ApiException(ErrorCode.UNAUTHENTICATED, "WebSocket bileti gecersiz veya kullanilmis");
        }
        return UUID.fromString(userId);
    }

    public record Ticket(String value, long expiresInSeconds) {
    }
}

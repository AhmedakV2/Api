package com.aft.api.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aft.security")
public record SecurityProperties(Lockout lockout, RateLimit rateLimit, Duration wsTicketTtl) {
    public SecurityProperties {
        lockout = lockout == null ? new Lockout(5, Duration.ofMinutes(15)) : lockout;
        rateLimit = rateLimit == null ? new RateLimit(20, 300, 30) : rateLimit;
        wsTicketTtl = wsTicketTtl == null ? Duration.ofSeconds(60) : wsTicketTtl;
    }

    public record Lockout(int maxAttempts, Duration duration) {
    }

    public record RateLimit(int anonymousPerMinute, int authenticatedPerMinute, int agentPerMinute) {
    }
}

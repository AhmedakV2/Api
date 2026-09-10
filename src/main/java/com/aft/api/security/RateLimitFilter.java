package com.aft.api.security;

import com.aft.api.config.SecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class RateLimitFilter extends OncePerRequestFilter {
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final String AGENT_MESSAGE_PATH = "/api/v1/agent/sessions/";

    private final StringRedisTemplate redis;
    private final RedisScript<List> rateLimitScript;
    private final SecurityProperties properties;

    public RateLimitFilter(StringRedisTemplate redis, RedisScript<List> rateLimitScript,
                           SecurityProperties properties) {
        this.redis = redis;
        this.rateLimitScript = rateLimitScript;
        this.properties = properties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = auth != null && auth.isAuthenticated();
        String bucket = authenticated ? auth.getName() : clientIp(request);
        int limit = resolveLimit(request, authenticated);
        String key = "aft:rate:" + bucket + ":" + limit;

        List<?> result = redis.execute(rateLimitScript, List.of(key), String.valueOf(WINDOW.toMillis()));
        long count = result == null ? 0L : ((Number) result.get(0)).longValue();
        long ttlMillis = result == null ? WINDOW.toMillis() : ((Number) result.get(1)).longValue();

        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, limit - count)));

        if (count > limit) {
            response.setHeader("Retry-After", String.valueOf(Math.max(1, ttlMillis / 1000)));
            response.sendError(429, "Hiz siniri asildi");
            return;
        }
        chain.doFilter(request, response);
    }

    private int resolveLimit(HttpServletRequest request, boolean authenticated) {
        if (request.getRequestURI().startsWith(AGENT_MESSAGE_PATH)
                && request.getRequestURI().endsWith("/messages")) {
            return properties.rateLimit().agentPerMinute();
        }
        return authenticated
                ? properties.rateLimit().authenticatedPerMinute()
                : properties.rateLimit().anonymousPerMinute();
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

package com.aft.api.security;

import com.aft.api.config.SecurityProperties;
import com.aft.api.config.WebSocketConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import com.aft.api.state.CounterStore;
import com.aft.api.state.StateNamespaces;
import com.aft.api.state.StateStoreProvider;
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
    private static final String PROBLEM_BODY =
            "{\"type\":\"https://docs.aft.local/errors/RATE_LIMIT_EXCEEDED\","
                    + "\"title\":\"Hiz siniri asildi\",\"status\":429,"
                    + "\"detail\":\"Hiz siniri asildi\",\"instance\":\"%s\","
                    + "\"code\":\"RATE_LIMIT_EXCEEDED\"}";

    private final CounterStore counters;
    private final SecurityProperties properties;

    public RateLimitFilter(StateStoreProvider stateStores, SecurityProperties properties) {
        this.counters = stateStores.counter(StateNamespaces.RATE_LIMIT);
        this.properties = properties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator") || path.startsWith(WebSocketConfig.ENDPOINT);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = auth != null && auth.isAuthenticated();
        String bucket = authenticated ? auth.getName() : clientIp(request);
        int limit = resolveLimit(request, authenticated);
        String key = bucket + ":" + limit;

        CounterStore.Window window = counters.increment(key, WINDOW);
        long count = window.count();

        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, limit - count)));

        if (count > limit) {
            response.setHeader("Retry-After", String.valueOf(Math.max(1, window.remaining().toSeconds())));
            response.setStatus(429);
            response.setContentType("application/problem+json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(PROBLEM_BODY.formatted(request.getRequestURI()));
            response.getWriter().flush();
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

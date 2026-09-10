package com.aft.api.security;

import com.aft.api.security.entity.ApiKey;
import com.aft.api.security.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
    public static final String HEADER = "X-Aft-Key";
    public static final String ROLE_DEVICE = "ROLE_DEVICE";

    private final ApiKeyService apiKeyService;

    public ApiKeyAuthenticationFilter(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String rawKey = request.getHeader(HEADER);
        if (rawKey != null && !rawKey.isBlank()
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            Optional<ApiKey> key = apiKeyService.authenticate(rawKey);
            key.ifPresent(apiKey -> SecurityContextHolder.getContext()
                    .setAuthentication(toAuthentication(apiKey)));
        }
        chain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken toAuthentication(ApiKey apiKey) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(ROLE_DEVICE));
        apiKey.scopeSet().forEach(scope -> authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope)));

        ApiKeyPrincipal principal = new ApiKeyPrincipal(apiKey.getId(), apiKey.getOwnerId(),
                apiKey.getOrgId(), apiKey.getName());
        return new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }
}

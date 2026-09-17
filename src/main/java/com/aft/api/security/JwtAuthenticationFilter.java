package com.aft.api.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String token = extractToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Claims claims = tokenProvider.parse(token);
            if (claims != null) {
                SecurityContextHolder.getContext().setAuthentication(toAuthentication(claims, request));
            }
        }
        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        return (header != null && header.startsWith(BEARER_PREFIX)) ? header.substring(BEARER_PREFIX.length()) : null;
    }

    @SuppressWarnings("unchecked")
    private UsernamePasswordAuthenticationToken toAuthentication(Claims claims, HttpServletRequest request) {
        List<String> roles = claims.get("roles", List.class);
        List<String> orgs = claims.get("orgs", List.class);
        List<GrantedAuthority> authorities = roles == null ? List.of()
                : roles.stream().map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role)).toList();
        Set<UUID> orgIds = orgs == null ? Set.of()
                : orgs.stream().map(UUID::fromString).collect(Collectors.toUnmodifiableSet());

        AftPrincipal principal = new AftPrincipal(UUID.fromString(claims.getSubject()),
                claims.get("username", String.class), claims.get("email", String.class), null,
                com.aft.api.user.entity.UserStatus.ACTIVE, orgIds, authorities);

        var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authentication;
    }
}

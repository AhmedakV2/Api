package com.aft.api.security;

import com.aft.api.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ORGS = "orgs";

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(AftPrincipal principal) {
        Instant now = Instant.now();
        List<String> roles = principal.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .toList();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(properties.issuer())
                .subject(principal.userId().toString())
                .claim(CLAIM_EMAIL, principal.email())
                .claim(CLAIM_ROLES, roles)
                .claim(CLAIM_ORGS, principal.orgIds().stream().map(UUID::toString).toList())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.accessTtl())))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(properties.issuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public long accessTtlSeconds() {
        return properties.accessTtl().toSeconds();
    }

    public Instant refreshExpiry(Instant from) {
        return from.plus(properties.refreshTtl());
    }
}

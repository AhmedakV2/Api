package com.aft.api.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "aft.jwt")
public record JwtProperties(
        @NotBlank @Size(min = 32) String secret,
        Duration accessTtl,
        Duration refreshTtl,
        String issuer) {
    public JwtProperties {
        accessTtl = accessTtl == null ? Duration.ofMinutes(15) : accessTtl;
        refreshTtl = refreshTtl == null ? Duration.ofDays(30) : refreshTtl;
        issuer = (issuer == null || issuer.isBlank()) ? "aft-api" : issuer;
    }
}

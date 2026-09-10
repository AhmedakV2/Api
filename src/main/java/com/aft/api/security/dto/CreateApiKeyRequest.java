package com.aft.api.security.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;

public record CreateApiKeyRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull UUID orgId,
        Set<String> scopes,
        @Min(1) Integer expiresInDays) {
    public Set<String> scopeSet() {
        return (scopes == null || scopes.isEmpty()) ? Set.of("device") : scopes;
    }
}

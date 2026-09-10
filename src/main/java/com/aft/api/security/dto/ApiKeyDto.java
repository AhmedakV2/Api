package com.aft.api.security.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record ApiKeyDto(UUID id,
                        String name,
                        UUID orgId,
                        UUID ownerId,
                        Set<String> scopes,
                        Instant expiresAt,
                        Instant revokedAt,
                        Instant lastUsedAt,
                        Instant createdAt) {
}

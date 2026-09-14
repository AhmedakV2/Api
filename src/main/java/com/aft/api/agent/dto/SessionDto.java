package com.aft.api.agent.dto;

import java.time.Instant;
import java.util.UUID;

public record SessionDto(UUID id,
                         UUID orgId,
                         UUID userId,
                         UUID deviceId,
                         String title,
                         String mode,
                         String model,
                         String status,
                         Instant createdAt,
                         Instant closedAt) {
}

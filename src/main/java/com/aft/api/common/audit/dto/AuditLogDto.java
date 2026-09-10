package com.aft.api.common.audit.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditLogDto(UUID id,
                          UUID actorId,
                          String action,
                          String entityType,
                          String entityId,
                          String ip,
                          Map<String, Object> detail,
                          Instant createdAt) {
}

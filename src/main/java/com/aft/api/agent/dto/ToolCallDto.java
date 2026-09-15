package com.aft.api.agent.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ToolCallDto(UUID id,
                          UUID messageId,
                          UUID deviceId,
                          String toolName,
                          Map<String, Object> arguments,
                          String resultSummary,
                          Integer durationMs,
                          boolean ok,
                          String error,
                          Instant createdAt) {
}

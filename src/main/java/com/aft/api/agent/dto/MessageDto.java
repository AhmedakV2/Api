package com.aft.api.agent.dto;

import java.time.Instant;
import java.util.UUID;

public record MessageDto(UUID id, int seq, String role, String content, int tokenCount, Instant createdAt) {
}

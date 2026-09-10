package com.aft.api.tenant.dto;

import java.time.Instant;
import java.util.UUID;

public record MemberDto(UUID userId, String email, String displayName, String role, Instant joinedAt) {
}

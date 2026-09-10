package com.aft.api.tenant.dto;

import java.time.Instant;
import java.util.UUID;

public record InvitationDto(UUID id, UUID orgId, String email, String role, Instant expiresAt, String token) {
}

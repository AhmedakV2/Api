package com.aft.api.user.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserDto(UUID id,
                      String email,
                      String displayName,
                      String status,
                      String locale,
                      boolean mfaEnabled,
                      Set<String> roles,
                      Instant lastLoginAt,
                      Instant createdAt) {
}

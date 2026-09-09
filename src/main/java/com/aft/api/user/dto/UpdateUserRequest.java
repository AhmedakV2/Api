package com.aft.api.user.dto;

import com.aft.api.user.entity.UserStatus;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(max = 120) String displayName,
        @Pattern(regexp = "tr|en") String locale,
        UserStatus status,
        Boolean mfaEnabled) {
}

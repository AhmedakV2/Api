package com.aft.api.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AcceptInvitationRequest(@NotBlank String token,
                                      @Size(min = 3, max = 64)
                                      @Pattern(regexp = "^[A-Za-z0-9._-]+$") String username,
                                      @Size(max = 120) String displayName,
                                      @Size(min = 12, max = 128) String password) {
}

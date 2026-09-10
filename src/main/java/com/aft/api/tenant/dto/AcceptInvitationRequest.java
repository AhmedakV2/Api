package com.aft.api.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcceptInvitationRequest(@NotBlank String token,
                                      @Size(max = 120) String displayName,
                                      @Size(min = 12, max = 128) String password) {
}

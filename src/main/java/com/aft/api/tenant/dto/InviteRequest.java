package com.aft.api.tenant.dto;

import com.aft.api.user.entity.RoleCode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InviteRequest(@NotBlank @Email String email, @NotNull RoleCode role) {
}

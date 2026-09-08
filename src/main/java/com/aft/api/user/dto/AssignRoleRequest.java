package com.aft.api.user.dto;

import com.aft.api.user.entity.RoleCode;
import jakarta.validation.constraints.NotNull;

public record AssignRoleRequest(@NotNull RoleCode code) {
}

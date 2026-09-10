package com.aft.api.tenant.dto;

import java.util.UUID;

public record OrganizationDto(UUID id, String name, String slug, String status, long aiTokenBudgetDaily) {
}

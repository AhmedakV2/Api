package com.aft.api.tenant.dto;

import com.aft.api.tenant.entity.OrganizationStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateOrganizationRequest(@Size(max = 160) String name,
                                        OrganizationStatus status,
                                        @Min(0) Long aiTokenBudgetDaily) {
}

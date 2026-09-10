package com.aft.api.device.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeviceCapabilityDto(@NotBlank @Size(max = 64) String toolName,
                                  @Min(1) int schemaVersion,
                                  boolean enabled) {
}

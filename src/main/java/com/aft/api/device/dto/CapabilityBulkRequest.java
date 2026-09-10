package com.aft.api.device.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CapabilityBulkRequest(@NotNull @Size(max = 64) List<@Valid DeviceCapabilityDto> capabilities) {
}

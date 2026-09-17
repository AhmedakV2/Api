package com.aft.api.device.dto;

import java.util.UUID;

public record DeviceProvisionDto(UUID orgId, String deviceKey, DeviceDto device) {
}

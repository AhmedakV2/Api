package com.aft.api.device.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DeviceDto(UUID id,
                        UUID orgId,
                        UUID userId,
                        String hostname,
                        String os,
                        String appVersion,
                        String status,
                        Instant lastSeenAt,
                        List<DeviceCapabilityDto> capabilities) {
}

package com.aft.api.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeviceRegisterRequest(@NotBlank @Size(max = 160) String hostname,
                                    @NotBlank @Size(max = 64) String os,
                                    @NotBlank @Size(max = 32) String appVersion) {
}

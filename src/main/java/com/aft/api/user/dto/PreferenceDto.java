package com.aft.api.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PreferenceDto(@NotBlank @Size(max = 64) String key,
                            @NotBlank @Size(max = 512) String value) {
}

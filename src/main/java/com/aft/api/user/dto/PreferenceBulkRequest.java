package com.aft.api.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PreferenceBulkRequest(@NotNull @Size(max = 100) List<@Valid PreferenceDto> preferences) {
}

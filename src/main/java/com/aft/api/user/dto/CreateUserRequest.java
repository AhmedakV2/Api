package com.aft.api.user.dto;

import com.aft.api.user.entity.RoleCode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record CreateUserRequest(
        @NotBlank @Email @Size(max = 190) String email,
        @NotBlank @Size(min = 12, max = 128) String password,
        @NotBlank @Size(max = 120) String displayName,
        @Pattern(regexp = "tr|en") String locale,
        @NotEmpty Set<RoleCode> roles) {

    public String localeOrDefault() {
        return locale == null || locale.isBlank() ? "tr" : locale;
    }
}

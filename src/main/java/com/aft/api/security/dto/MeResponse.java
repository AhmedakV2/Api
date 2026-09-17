package com.aft.api.security.dto;

import com.aft.api.tenant.dto.OrganizationDto;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record MeResponse(UUID id,
                         String username,
                         String email,
                         String displayName,
                         String locale,
                         boolean mfaEnabled,
                         Set<String> roles,
                         List<OrganizationDto> organizations) {
}

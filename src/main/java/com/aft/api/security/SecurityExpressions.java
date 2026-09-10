package com.aft.api.security;

import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("aft")
public class SecurityExpressions {
    public boolean isSelf(UUID userId, Authentication authentication) {
        return authentication != null
                && authentication.getPrincipal() instanceof AftPrincipal principal
                && principal.userId().equals(userId);
    }

    public boolean inOrg(UUID orgId, Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return switch (authentication.getPrincipal()) {
            case AftPrincipal principal -> principal.orgIds().contains(orgId);
            case ApiKeyPrincipal principal -> principal.orgId().equals(orgId);
            default -> false;
        };
    }

    public UUID apiKeyId(Authentication authentication) {
        return (authentication != null && authentication.getPrincipal() instanceof ApiKeyPrincipal principal)
                ? principal.apiKeyId() : null;
    }
}

package com.aft.api.security;

import java.io.Serializable;
import java.util.UUID;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class PermissionEvaluatorImpl implements PermissionEvaluator {
    @Override
    public boolean hasPermission(Authentication authentication, Object target, Object permission) {
        if (target instanceof UUID orgId) {
            return hasOrgAccess(authentication, orgId);
        }
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId,
                                 String targetType, Object permission) {
        if (!"Organization".equals(targetType)) {
            return false;
        }
        return hasOrgAccess(authentication, UUID.fromString(targetId.toString()));
    }

    private boolean hasOrgAccess(Authentication authentication, UUID orgId) {
        if (authentication == null) {
            return false;
        }
        return switch (authentication.getPrincipal()) {
            case AftPrincipal principal -> principal.orgIds().contains(orgId);
            case ApiKeyPrincipal principal -> principal.orgId().equals(orgId);
            default -> false;
        };
    }
}

package com.aft.api.common.audit;

import com.aft.api.security.AftPrincipal;
import com.aft.api.security.ApiKeyPrincipal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("auditorAware")
public class AuditorAwareImpl implements AuditorAware<UUID> {
    @Override
    public Optional<UUID> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }
        return switch (auth.getPrincipal()) {
            case AftPrincipal principal -> Optional.of(principal.userId());
            case ApiKeyPrincipal principal -> Optional.of(principal.ownerId());
            default -> Optional.empty();
        };
    }
}

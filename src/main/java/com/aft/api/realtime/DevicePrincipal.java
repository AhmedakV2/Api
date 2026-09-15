package com.aft.api.realtime;

import java.security.Principal;
import java.util.UUID;

public record DevicePrincipal(UUID userId, UUID deviceId) implements Principal {
    @Override
    public String getName() {
        return deviceId.toString();
    }
}

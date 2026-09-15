package com.aft.api.realtime;

import java.security.Principal;
import java.util.UUID;

/** WebSocket baglantisinin kimligi; kullanici ve cihaz birlikte tasinir. */
public record DevicePrincipal(UUID userId, UUID deviceId) implements Principal {

    @Override
    public String getName() {
        return deviceId.toString();
    }
}

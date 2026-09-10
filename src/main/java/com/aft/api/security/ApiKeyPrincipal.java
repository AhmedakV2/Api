package com.aft.api.security;

import java.util.UUID;

public record ApiKeyPrincipal(UUID apiKeyId, UUID ownerId, UUID orgId, String name) {
    @Override
    public String toString() {
        return "ApiKey[" + name + "]";
    }
}

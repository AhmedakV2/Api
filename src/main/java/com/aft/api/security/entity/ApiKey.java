package com.aft.api.security.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "api_key")
public class ApiKey {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "owner_id", nullable = false, updatable = false)
    private UUID ownerId;

    @Column(name = "org_id", nullable = false, updatable = false)
    private UUID orgId;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "key_hash", nullable = false, length = 64, updatable = false)
    private String keyHash;

    @Column(name = "scopes", nullable = false, length = 255)
    private String scopes = "";

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected ApiKey() {
    }

    public ApiKey(UUID ownerId, UUID orgId, String name, String keyHash,
                  Set<String> scopes, Instant expiresAt) {
        this.ownerId = ownerId;
        this.orgId = orgId;
        this.name = name;
        this.keyHash = keyHash;
        this.scopes = String.join(",", scopes);
        this.expiresAt = expiresAt;
    }

    public boolean isUsable(Instant now) {
        return revokedAt == null && (expiresAt == null || expiresAt.isAfter(now));
    }

    public void revoke(Instant when) {
        if (revokedAt == null) {
            this.revokedAt = when;
        }
    }

    public void touch(Instant when) {
        this.lastUsedAt = when;
    }

    public Set<String> scopeSet() {
        if (scopes == null || scopes.isBlank()) {
            return Set.of();
        }
        return new LinkedHashSet<>(Arrays.asList(scopes.split(",")));
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public UUID getOrgId() {
        return orgId;
    }

    public String getName() {
        return name;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

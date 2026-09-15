package com.aft.api.tenant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "invitation")
public class Invitation {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "org_id", nullable = false, updatable = false)
    private UUID orgId;

    @Column(name = "email", nullable = false, updatable = false, columnDefinition = "citext")
    private String email;

    @Column(name = "role_id", nullable = false, updatable = false)
    private UUID roleId;

    @Column(name = "token_hash", nullable = false, length = 64, updatable = false)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Invitation() {
    }

    public Invitation(UUID orgId, String email, UUID roleId, String tokenHash, Instant expiresAt) {
        this.orgId = orgId;
        this.email = email;
        this.roleId = roleId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    public boolean isPending(Instant now) {
        return acceptedAt == null && expiresAt.isAfter(now);
    }

    public void accept(Instant when) {
        this.acceptedAt = when;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrgId() {
        return orgId;
    }

    public String getEmail() {
        return email;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }
}

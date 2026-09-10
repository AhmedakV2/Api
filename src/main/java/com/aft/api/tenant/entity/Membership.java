package com.aft.api.tenant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "membership")
public class Membership {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "org_id", nullable = false, updatable = false)
    private UUID orgId;

    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt = Instant.now();

    protected Membership() {
    }

    public Membership(UUID userId, UUID orgId, UUID roleId) {
        this.userId = userId;
        this.orgId = orgId;
        this.roleId = roleId;
    }

    public void changeRole(UUID newRoleId) {
        this.roleId = newRoleId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getOrgId() {
        return orgId;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }
}

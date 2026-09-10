package com.aft.api.tenant.entity;

import com.aft.api.common.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "organization")
public class Organization extends AuditableEntity {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 160)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 160, updatable = false)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private OrganizationStatus status = OrganizationStatus.ACTIVE;

    @Column(name = "ai_token_budget_daily", nullable = false)
    private long aiTokenBudgetDaily = 1_000_000L;

    protected Organization() {
    }

    public Organization(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }

    public void rename(String newName) {
        this.name = newName;
    }

    public void changeStatus(OrganizationStatus newStatus) {
        this.status = newStatus;
    }

    public void changeBudget(long dailyTokens) {
        this.aiTokenBudgetDaily = dailyTokens;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public OrganizationStatus getStatus() {
        return status;
    }

    public long getAiTokenBudgetDaily() {
        return aiTokenBudgetDaily;
    }
}

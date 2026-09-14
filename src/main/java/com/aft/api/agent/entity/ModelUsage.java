package com.aft.api.agent.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "model_usage")
public class ModelUsage {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "org_id", nullable = false, updatable = false)
    private UUID orgId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "model", nullable = false, length = 80, updatable = false)
    private String model;

    @Column(name = "token_in", nullable = false)
    private int tokenIn;

    @Column(name = "token_out", nullable = false)
    private int tokenOut;

    @Column(name = "cost", nullable = false, precision = 12, scale = 6)
    private BigDecimal cost = BigDecimal.ZERO;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    private Instant recordedAt = Instant.now();

    protected ModelUsage() {
    }

    public ModelUsage(UUID orgId, UUID userId, UUID sessionId, String model,
                      int tokenIn, int tokenOut, BigDecimal cost) {
        this.orgId = orgId;
        this.userId = userId;
        this.sessionId = sessionId;
        this.model = model;
        this.tokenIn = tokenIn;
        this.tokenOut = tokenOut;
        this.cost = cost == null ? BigDecimal.ZERO : cost;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrgId() {
        return orgId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public String getModel() {
        return model;
    }

    public int getTokenIn() {
        return tokenIn;
    }

    public int getTokenOut() {
        return tokenOut;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }
}

package com.aft.api.agent.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agent_session")
public class AgentSession {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "org_id", nullable = false, updatable = false)
    private UUID orgId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "device_id")
    private UUID deviceId;

    @Column(name = "title", nullable = false, length = 200)
    private String title = "";

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 32)
    private SessionMode mode = SessionMode.CHAT;

    @Column(name = "model", nullable = false, length = 80)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private SessionStatus status = SessionStatus.OPEN;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "closed_at")
    private Instant closedAt;

    protected AgentSession() {
    }

    public AgentSession(UUID orgId, UUID userId, UUID deviceId, String title, SessionMode mode, String model) {
        this.orgId = orgId;
        this.userId = userId;
        this.deviceId = deviceId;
        this.title = title == null ? "" : title;
        this.mode = mode == null ? SessionMode.CHAT : mode;
        this.model = model;
    }

    public boolean isOpen() {
        return status == SessionStatus.OPEN;
    }

    public void rename(String newTitle) {
        this.title = newTitle;
    }

    public void close(Instant when) {
        if (status == SessionStatus.OPEN) {
            this.status = SessionStatus.CLOSED;
            this.closedAt = when;
        }
    }

    public void fail(Instant when) {
        this.status = SessionStatus.FAILED;
        this.closedAt = when;
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

    public UUID getDeviceId() {
        return deviceId;
    }

    public String getTitle() {
        return title;
    }

    public SessionMode getMode() {
        return mode;
    }

    public void retune(String value) {
        this.model = value;
    }

    public String getModel() {
        return model;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }
}

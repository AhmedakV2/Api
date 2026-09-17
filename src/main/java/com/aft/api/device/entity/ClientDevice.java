package com.aft.api.device.entity;

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
@Table(name = "client_device")
public class ClientDevice {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "org_id", nullable = false, updatable = false)
    private UUID orgId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "hostname", nullable = false, length = 160)
    private String hostname;

    @Column(name = "os", nullable = false, length = 64)
    private String os;

    @Column(name = "app_version", nullable = false, length = 32)
    private String appVersion;

    @Column(name = "api_key_id")
    private UUID apiKeyId;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private DeviceStatus status = DeviceStatus.OFFLINE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected ClientDevice() {
    }

    public ClientDevice(UUID orgId, UUID userId, String hostname, String os, String appVersion, UUID apiKeyId) {
        this.orgId = orgId;
        this.userId = userId;
        this.hostname = hostname;
        this.os = os;
        this.appVersion = appVersion;
        this.apiKeyId = apiKeyId;
    }

    public void refresh(String hostname, String os, String appVersion) {
        this.hostname = hostname;
        this.os = os;
        this.appVersion = appVersion;
    }

    public void attachApiKey(UUID newApiKeyId) {
        this.apiKeyId = newApiKeyId;
    }

    public void markSeen(Instant when) {
        this.lastSeenAt = when;
        if (status != DeviceStatus.BLOCKED) {
            this.status = DeviceStatus.ONLINE;
        }
    }

    public void markOffline() {
        if (status == DeviceStatus.ONLINE) {
            this.status = DeviceStatus.OFFLINE;
        }
    }

    public void block() {
        this.status = DeviceStatus.BLOCKED;
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

    public String getHostname() {
        return hostname;
    }

    public String getOs() {
        return os;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public UUID getApiKeyId() {
        return apiKeyId;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public DeviceStatus getStatus() {
        return status;
    }
}

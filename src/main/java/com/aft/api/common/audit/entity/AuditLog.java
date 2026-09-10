package com.aft.api.common.audit.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;

@Entity
@Immutable
@Table(name = "audit_log")
public class AuditLog {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "actor_id", updatable = false)
    private UUID actorId;

    @Column(name = "action", nullable = false, length = 64, updatable = false)
    private String action;

    @Column(name = "entity_type", nullable = false, length = 64, updatable = false)
    private String entityType;

    @Column(name = "entity_id", length = 64, updatable = false)
    private String entityId;

    @Column(name = "ip", columnDefinition = "inet", updatable = false)
    private String ip;

    @Type(JsonBinaryType.class)
    @Column(name = "detail_json", columnDefinition = "jsonb", nullable = false, updatable = false)
    private Map<String, Object> detail = Map.of();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected AuditLog() {
    }

    public AuditLog(UUID actorId, String action, String entityType, String entityId,
                    String ip, Map<String, Object> detail) {
        this.actorId = actorId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.ip = ip;
        this.detail = detail == null ? Map.of() : detail;
    }

    public UUID getId() {
        return id;
    }

    public UUID getActorId() {
        return actorId;
    }

    public String getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getIp() {
        return ip;
    }

    public Map<String, Object> getDetail() {
        return detail;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

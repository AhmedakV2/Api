package com.aft.api.common.audit;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private UUID createdBy;
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @LastModifiedBy
    @Column(name = "updated_by")
    private UUID updatedBy;
    @Version
    @Column(name = "version", nullable = false)
    private long version;
    public Instant getCreatedAt() {
        return createdAt;
    }
    public UUID getCreatedBy() {
        return createdBy;
    }
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    public UUID getUpdatedBy() {
        return updatedBy;
    }
    public long getVersion() {
        return version;
    }
}

package com.aft.api.common.audit.repository;

import com.aft.api.common.audit.entity.AuditLog;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    @Query("""
            SELECT a FROM AuditLog a
            WHERE a.createdAt >= :from AND a.createdAt < :to
              AND (:actorId IS NULL OR a.actorId = :actorId)
              AND (:action IS NULL OR a.action = :action)
              AND (:entityType IS NULL OR a.entityType = :entityType)
            """)
    Page<AuditLog> search(@Param("from") Instant from,
                          @Param("to") Instant to,
                          @Param("actorId") UUID actorId,
                          @Param("action") String action,
                          @Param("entityType") String entityType,
                          Pageable pageable);
}

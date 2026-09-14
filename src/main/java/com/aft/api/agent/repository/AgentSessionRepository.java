package com.aft.api.agent.repository;

import com.aft.api.agent.entity.AgentSession;
import com.aft.api.agent.entity.SessionStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AgentSessionRepository extends JpaRepository<AgentSession, UUID> {

    @Query("""
            SELECT s FROM AgentSession s
            WHERE s.userId = :userId AND s.orgId = :orgId
              AND (:status IS NULL OR s.status = :status)
            """)
    Page<AgentSession> findOwned(@Param("userId") UUID userId,
                                 @Param("orgId") UUID orgId,
                                 @Param("status") SessionStatus status,
                                 Pageable pageable);
}

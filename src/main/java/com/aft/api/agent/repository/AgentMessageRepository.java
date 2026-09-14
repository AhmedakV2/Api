package com.aft.api.agent.repository;

import com.aft.api.agent.entity.AgentMessage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AgentMessageRepository extends JpaRepository<AgentMessage, UUID> {

    List<AgentMessage> findBySessionIdOrderBySeqAsc(UUID sessionId);

    @Query("SELECT m FROM AgentMessage m WHERE m.sessionId = :sessionId ORDER BY m.seq DESC LIMIT :limit")
    List<AgentMessage> findRecent(@Param("sessionId") UUID sessionId, @Param("limit") int limit);

    @Query("SELECT COALESCE(MAX(m.seq), 0) FROM AgentMessage m WHERE m.sessionId = :sessionId")
    int findMaxSeq(@Param("sessionId") UUID sessionId);

    void deleteBySessionId(UUID sessionId);
}

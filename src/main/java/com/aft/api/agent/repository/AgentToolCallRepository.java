package com.aft.api.agent.repository;

import com.aft.api.agent.entity.AgentToolCall;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentToolCallRepository extends JpaRepository<AgentToolCall, UUID> {

    List<AgentToolCall> findByMessageIdOrderByCreatedAtAsc(UUID messageId);
}

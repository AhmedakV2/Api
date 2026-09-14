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
@Table(name = "agent_message")
public class AgentMessage {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "session_id", nullable = false, updatable = false)
    private UUID sessionId;

    @Column(name = "seq", nullable = false, updatable = false)
    private int seq;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 16, updatable = false)
    private MessageRole role;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "token_count", nullable = false)
    private int tokenCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected AgentMessage() {
    }

    public AgentMessage(UUID sessionId, int seq, MessageRole role, String content, int tokenCount) {
        this.sessionId = sessionId;
        this.seq = seq;
        this.role = role;
        this.content = content;
        this.tokenCount = tokenCount;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public int getSeq() {
        return seq;
    }

    public MessageRole getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public int getTokenCount() {
        return tokenCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

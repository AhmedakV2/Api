package com.aft.api.agent.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "agent_tool_call")
public class AgentToolCall {
    private static final int SUMMARY_LIMIT = 2048;

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "message_id", nullable = false, updatable = false)
    private UUID messageId;

    @Column(name = "device_id")
    private UUID deviceId;

    @Column(name = "tool_name", nullable = false, length = 64, updatable = false)
    private String toolName;

    @Type(JsonBinaryType.class)
    @Column(name = "arguments_json", columnDefinition = "jsonb", nullable = false, updatable = false)
    private Map<String, Object> arguments = Map.of();

    @Column(name = "result_summary", length = SUMMARY_LIMIT)
    private String resultSummary;

    @Column(name = "duration_ms")
    private Integer durationMs;

    @Column(name = "ok", nullable = false)
    private boolean ok;

    @Column(name = "error", length = 512)
    private String error;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected AgentToolCall() {
    }

    public AgentToolCall(UUID messageId, UUID deviceId, String toolName, Map<String, Object> arguments) {
        this.messageId = messageId;
        this.deviceId = deviceId;
        this.toolName = toolName;
        this.arguments = arguments == null ? Map.of() : arguments;
    }

    public void succeed(String summary, int durationMs) {
        this.ok = true;
        this.resultSummary = clip(summary);
        this.durationMs = durationMs;
    }

    public void fail(String errorText, int durationMs) {
        this.ok = false;
        this.error = errorText == null ? null : errorText.substring(0, Math.min(errorText.length(), 512));
        this.durationMs = durationMs;
    }

    private String clip(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= SUMMARY_LIMIT ? value : value.substring(0, SUMMARY_LIMIT);
    }

    public UUID getId() {
        return id;
    }

    public UUID getMessageId() {
        return messageId;
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public String getToolName() {
        return toolName;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }

    public String getResultSummary() {
        return resultSummary;
    }

    public Integer getDurationMs() {
        return durationMs;
    }

    public boolean isOk() {
        return ok;
    }

    public String getError() {
        return error;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

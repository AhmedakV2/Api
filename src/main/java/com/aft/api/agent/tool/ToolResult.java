package com.aft.api.agent.tool;

import java.util.UUID;

public record ToolResult(UUID callId,
                         boolean ok,
                         String contentJson,
                         String error,
                         boolean truncated) {
    public static ToolResult ok(UUID callId, String contentJson, boolean truncated) {
        return new ToolResult(callId, true, contentJson, null, truncated);
    }

    public static ToolResult failed(UUID callId, String error) {
        return new ToolResult(callId, false, null, error, false);
    }
}

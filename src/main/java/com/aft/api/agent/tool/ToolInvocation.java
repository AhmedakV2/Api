package com.aft.api.agent.tool;

import java.util.UUID;

/** Sunucudan istemciye giden arac cagrisi. */
public record ToolInvocation(UUID callId,
                             UUID sessionId,
                             String toolName,
                             String argumentsJson,
                             boolean approvalRequired,
                             long timeoutMs) {
}

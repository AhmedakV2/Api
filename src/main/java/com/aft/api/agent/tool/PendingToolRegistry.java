package com.aft.api.agent.tool;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PendingToolRegistry {
    private static final Logger log = LoggerFactory.getLogger(PendingToolRegistry.class);

    private final Map<UUID, CompletableFuture<ToolResult>> pending = new ConcurrentHashMap<>();

    public CompletableFuture<ToolResult> register(UUID callId) {
        CompletableFuture<ToolResult> future = new CompletableFuture<>();
        pending.put(callId, future);
        return future;
    }

    public boolean complete(ToolResult result) {
        CompletableFuture<ToolResult> future = pending.remove(result.callId());
        if (future == null) {
            log.debug("Bilinmeyen veya suresi dolmus arac cagrisi callId={}", result.callId());
            return false;
        }
        return future.complete(result);
    }

    public void cancel(UUID callId) {
        CompletableFuture<ToolResult> future = pending.remove(callId);
        if (future != null) {
            future.cancel(true);
        }
    }

    public boolean isPending(UUID callId) {
        return pending.containsKey(callId);
    }

    public int pendingCount() {
        return pending.size();
    }
}

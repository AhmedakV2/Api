package com.aft.api.agent.tool;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class ToolCallContext {
    public static final String KEY = "aftToolCallContext";

    private final UUID sessionId;
    private final UUID userId;
    private final UUID deviceId;
    private final AtomicInteger hops = new AtomicInteger();

    private volatile UUID messageId;

    public ToolCallContext(UUID sessionId, UUID userId, UUID deviceId) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.deviceId = deviceId;
    }

    public UUID sessionId() {
        return sessionId;
    }

    public UUID userId() {
        return userId;
    }

    public UUID deviceId() {
        return deviceId;
    }

    public UUID messageId() {
        return messageId;
    }

    public void bindMessage(UUID value) {
        this.messageId = value;
    }

    public int nextHop() {
        return hops.incrementAndGet();
    }

    public int hops() {
        return hops.get();
    }
}

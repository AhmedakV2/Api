package com.aft.api.agent.dto;

import java.util.UUID;

public record ChatFrame(String turnId,
                        UUID sessionId,
                        String kind,
                        String text,
                        String messageId,
                        String model) {

    public static ChatFrame delta(String turnId, UUID sessionId, String text) {
        return new ChatFrame(turnId, sessionId, "delta", text, null, null);
    }

    public static ChatFrame done(String turnId, UUID sessionId, UUID messageId, String model) {
        return new ChatFrame(turnId, sessionId, "done", null, messageId.toString(), model);
    }

    public static ChatFrame error(String turnId, UUID sessionId, String message) {
        return new ChatFrame(turnId, sessionId, "error", message, null, null);
    }
}

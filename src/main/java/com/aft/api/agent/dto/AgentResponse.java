package com.aft.api.agent.dto;

import java.util.UUID;

public record AgentResponse(UUID sessionId,
                            UUID messageId,
                            int seq,
                            String content,
                            String model,
                            int tokenIn,
                            int tokenOut) {
}

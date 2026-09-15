package com.aft.api.agent.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/** Arac sonucunun HTTP uzerinden geri verilmesi; WebSocket kurulamayan ortamlar icin yedek yol. */
public record ToolResultRequest(@NotNull UUID callId,
                                boolean ok,
                                @Size(max = 262144) String contentJson,
                                @Size(max = 512) String error,
                                boolean truncated) {
}

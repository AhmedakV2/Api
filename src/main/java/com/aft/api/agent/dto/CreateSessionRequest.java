package com.aft.api.agent.dto;

import com.aft.api.agent.entity.SessionMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateSessionRequest(@NotNull UUID orgId,
                                   UUID deviceId,
                                   @Size(max = 200) String title,
                                   SessionMode mode,
                                   @Size(max = 80) String model) {
}

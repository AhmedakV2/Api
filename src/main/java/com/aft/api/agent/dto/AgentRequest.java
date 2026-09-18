package com.aft.api.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AgentRequest(@NotBlank @Size(max = 32000) String content,
                           @Size(max = 80) String model) {
}

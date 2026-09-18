package com.aft.api.agent.dto;

import java.util.List;

public record ModelInfoDto(String provider,
                           boolean ready,
                           List<ModelTierDto> tiers,
                           String defaultTier,
                           String defaultModel) {
}

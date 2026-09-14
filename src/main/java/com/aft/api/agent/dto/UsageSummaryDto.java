package com.aft.api.agent.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record UsageSummaryDto(Instant from, Instant to, long totalTokens, BigDecimal totalCost,
                              List<ModelUsageRow> rows) {

    public record ModelUsageRow(String model, long tokenIn, long tokenOut, BigDecimal cost, long calls) {
    }
}

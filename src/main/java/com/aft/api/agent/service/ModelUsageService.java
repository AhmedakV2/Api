package com.aft.api.agent.service;

import com.aft.api.agent.dto.UsageSummaryDto;
import com.aft.api.agent.entity.ModelUsage;
import com.aft.api.agent.repository.ModelUsageRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ModelUsageService {

    private final ModelUsageRepository repository;

    public ModelUsageService(ModelUsageRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(UUID orgId, UUID userId, UUID sessionId, String model, Usage usage) {
        int tokenIn = value(usage == null ? null : usage.getPromptTokens());
        int tokenOut = value(usage == null ? null : usage.getCompletionTokens());
        repository.save(new ModelUsage(orgId, userId, sessionId, model, tokenIn, tokenOut, BigDecimal.ZERO));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordCounts(UUID orgId, UUID userId, UUID sessionId, String model, int tokenIn, int tokenOut) {
        repository.save(new ModelUsage(orgId, userId, sessionId, model, tokenIn, tokenOut, BigDecimal.ZERO));
    }

    @Transactional(readOnly = true)
    public long tokensToday(UUID orgId) {
        return repository.sumTokensSince(orgId, Instant.now().truncatedTo(ChronoUnit.DAYS));
    }

    @Transactional(readOnly = true)
    public UsageSummaryDto summary(UUID orgId, Instant from, Instant to) {
        List<ModelUsageRepositoryRow> rows = repository.summarize(orgId, from, to).stream()
                .map(row -> new ModelUsageRepositoryRow(row.model(), row.tokenIn(), row.tokenOut(),
                        row.cost(), row.calls()))
                .toList();

        long totalTokens = rows.stream().mapToLong(row -> row.tokenIn() + row.tokenOut()).sum();
        BigDecimal totalCost = rows.stream().map(ModelUsageRepositoryRow::cost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new UsageSummaryDto(from, to, totalTokens, totalCost,
                rows.stream().map(row -> new UsageSummaryDto.ModelUsageRow(row.model(), row.tokenIn(),
                        row.tokenOut(), row.cost(), row.calls())).toList());
    }

    private int value(Integer raw) {
        return raw == null ? 0 : raw;
    }

    private record ModelUsageRepositoryRow(String model, long tokenIn, long tokenOut,
                                           BigDecimal cost, long calls) {
    }
}

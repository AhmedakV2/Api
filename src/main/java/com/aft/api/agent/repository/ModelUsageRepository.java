package com.aft.api.agent.repository;

import com.aft.api.agent.entity.ModelUsage;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ModelUsageRepository extends JpaRepository<ModelUsage, UUID> {
    @Query("""
            SELECT new com.aft.api.agent.repository.ModelUsageRepository$ModelTotal(
                u.model, SUM(u.tokenIn), SUM(u.tokenOut), SUM(u.cost), COUNT(u))
            FROM ModelUsage u
            WHERE u.orgId = :orgId AND u.recordedAt >= :from AND u.recordedAt < :to
            GROUP BY u.model
            ORDER BY SUM(u.tokenIn) + SUM(u.tokenOut) DESC
            """)
    List<ModelTotal> summarize(@Param("orgId") UUID orgId,
                               @Param("from") Instant from,
                               @Param("to") Instant to);

    record ModelTotal(String model, long tokenIn, long tokenOut, BigDecimal cost, long calls) {
    }
}

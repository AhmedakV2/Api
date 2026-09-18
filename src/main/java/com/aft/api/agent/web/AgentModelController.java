package com.aft.api.agent.web;

import com.aft.api.agent.dto.ModelInfoDto;
import com.aft.api.agent.dto.UsageSummaryDto;
import com.aft.api.agent.provider.ModelRouter;
import com.aft.api.agent.service.ModelUsageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/agent")
@Tag(name = "Agent Model", description = "Model listesi ve kullanim ozeti")
public class AgentModelController {
    private static final int DEFAULT_WINDOW_DAYS = 30;

    private final ModelRouter modelRouter;
    private final ModelUsageService usageService;

    public AgentModelController(ModelRouter modelRouter, ModelUsageService usageService) {
        this.modelRouter = modelRouter;
        this.usageService = usageService;
    }

    @GetMapping("/models")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Kullanilabilir modeller")
    public ModelInfoDto models() {
        return new ModelInfoDto("OLLM",
                modelRouter.isReady(),
                modelRouter.tiers(),
                modelRouter.defaultTier().name(),
                modelRouter.modelFor(modelRouter.defaultTier()));
    }

    @GetMapping("/usage")
    @PreAuthorize("hasRole('ADMIN') and @aft.inOrg(#orgId, authentication)")
    @Operation(summary = "Token ve maliyet ozeti")
    public UsageSummaryDto usage(
            @RequestParam UUID orgId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        Instant end = to == null ? Instant.now() : to;
        Instant start = from == null ? end.minus(DEFAULT_WINDOW_DAYS, ChronoUnit.DAYS) : from;
        return usageService.summary(orgId, start, end);
    }
}

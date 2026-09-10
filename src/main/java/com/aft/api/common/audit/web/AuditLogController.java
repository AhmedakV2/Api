package com.aft.api.common.audit.web;

import com.aft.api.common.audit.dto.AuditLogDto;
import com.aft.api.common.audit.entity.AuditLog;
import com.aft.api.common.audit.repository.AuditLogRepository;
import com.aft.api.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit-logs")
@Tag(name = "Denetim", description = "Degisiklik ve erisim izi")
public class AuditLogController {
    private static final int DEFAULT_WINDOW_DAYS = 7;

    private final AuditLogRepository repository;

    public AuditLogController(AuditLogRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Denetim kaydi sorgulama")
    public PageResponse<AuditLogDto> search(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) UUID actorId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @ParameterObject @PageableDefault(size = 50, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        Instant end = to == null ? Instant.now() : to;
        Instant start = from == null ? end.minus(DEFAULT_WINDOW_DAYS, ChronoUnit.DAYS) : from;

        return PageResponse.of(repository.search(start, end, actorId, action, entityType, pageable)
                .map(this::toDto));
    }

    private AuditLogDto toDto(AuditLog entry) {
        return new AuditLogDto(entry.getId(), entry.getActorId(), entry.getAction(), entry.getEntityType(),
                entry.getEntityId(), entry.getIp(), entry.getDetail(), entry.getCreatedAt());
    }
}

package com.aft.api.security.service;

import com.aft.api.common.audit.AuditAction;
import com.aft.api.common.audit.AuditLogService;
import com.aft.api.common.exception.ForbiddenException;
import com.aft.api.common.exception.NotFoundException;
import com.aft.api.security.TokenHashing;
import com.aft.api.security.dto.ApiKeyCreatedDto;
import com.aft.api.security.dto.ApiKeyDto;
import com.aft.api.security.dto.CreateApiKeyRequest;
import com.aft.api.security.entity.ApiKey;
import com.aft.api.security.repository.ApiKeyRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApiKeyService {
    private static final Logger log = LoggerFactory.getLogger(ApiKeyService.class);
    private static final String PREFIX = "aft_";

    private final ApiKeyRepository repository;
    private final AuditLogService auditLog;

    public ApiKeyService(ApiKeyRepository repository, AuditLogService auditLog) {
        this.repository = repository;
        this.auditLog = auditLog;
    }

    @Transactional
    public ApiKeyCreatedDto create(UUID ownerId, CreateApiKeyRequest request) {
        String rawSecret = PREFIX + TokenHashing.randomSecret();
        Instant expiresAt = request.expiresInDays() == null
                ? null : Instant.now().plus(request.expiresInDays(), ChronoUnit.DAYS);

        ApiKey key = repository.save(new ApiKey(ownerId, request.orgId(), request.name(),
                TokenHashing.sha256Hex(rawSecret), request.scopeSet(), expiresAt));

        auditLog.record(AuditAction.API_KEY_CREATED, "ApiKey", key.getId().toString(), ownerId,
                Map.of("name", request.name(), "orgId", request.orgId().toString()));
        log.info("API anahtari uretildi id={} org={}", key.getId(), request.orgId());
        return new ApiKeyCreatedDto(toDto(key), rawSecret);
    }

    @Transactional(readOnly = true)
    public List<ApiKeyDto> list(UUID orgId) {
        return repository.findByOrgIdOrderByCreatedAtDesc(orgId).stream().map(this::toDto).toList();
    }

    @Transactional
    public void revoke(UUID keyId, UUID orgId, UUID actorId) {
        ApiKey key = repository.findById(keyId).orElseThrow(() -> NotFoundException.of("ApiKey", keyId));
        if (!key.getOrgId().equals(orgId)) {
            throw new ForbiddenException("Anahtar baska bir organizasyona ait");
        }
        key.revoke(Instant.now());
        auditLog.record(AuditAction.API_KEY_REVOKED, "ApiKey", keyId.toString(), actorId, Map.of());
    }

    @Transactional(readOnly = true)
    public Optional<ApiKey> authenticate(String rawKey) {
        return repository.findByKeyHash(TokenHashing.sha256Hex(rawKey))
                .filter(key -> key.isUsable(Instant.now()));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markUsed(UUID keyId) {
        repository.touch(keyId, Instant.now());
    }

    private ApiKeyDto toDto(ApiKey key) {
        return new ApiKeyDto(key.getId(), key.getName(), key.getOrgId(), key.getOwnerId(),
                key.scopeSet(), key.getExpiresAt(), key.getRevokedAt(), key.getLastUsedAt(), key.getCreatedAt());
    }
}

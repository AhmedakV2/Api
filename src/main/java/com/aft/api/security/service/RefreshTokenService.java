package com.aft.api.security.service;

import com.aft.api.common.audit.AuditAction;
import com.aft.api.common.audit.AuditLogService;
import com.aft.api.common.exception.ApiException;
import com.aft.api.common.exception.ErrorCode;
import com.aft.api.security.JwtTokenProvider;
import com.aft.api.security.TokenHashing;
import com.aft.api.security.entity.RefreshToken;
import com.aft.api.security.repository.RefreshTokenRepository;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {
    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    private final RefreshTokenRepository repository;
    private final JwtTokenProvider tokenProvider;
    private final AuditLogService auditLog;

    public RefreshTokenService(RefreshTokenRepository repository, JwtTokenProvider tokenProvider,
                               AuditLogService auditLog) {
        this.repository = repository;
        this.tokenProvider = tokenProvider;
        this.auditLog = auditLog;
    }

    @Transactional
    public String issue(UUID userId, String userAgent, String ip) {
        return issueInFamily(userId, UUID.randomUUID(), userAgent, ip);
    }

    @Transactional
    public Rotation rotate(String rawToken, String userAgent, String ip) {
        RefreshToken existing = repository.findByTokenHash(TokenHashing.sha256Hex(rawToken))
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHENTICATED, "Yenileme jetonu gecersiz"));

        Instant now = Instant.now();
        if (!existing.isActive(now)) {
            repository.revokeFamily(existing.getFamilyId(), now);
            auditLog.record(AuditAction.REFRESH_TOKEN_REUSE, "RefreshToken", existing.getId().toString(),
                    existing.getUserId(), Map.of("familyId", existing.getFamilyId().toString()));
            log.warn("Iptal edilmis yenileme jetonu yeniden sunuldu, aile iptal edildi familyId={}",
                    existing.getFamilyId());
            throw new ApiException(ErrorCode.UNAUTHENTICATED, "Yenileme jetonu gecersiz");
        }

        String rawNext = issueInFamily(existing.getUserId(), existing.getFamilyId(), userAgent, ip);
        RefreshToken next = repository.findByTokenHash(TokenHashing.sha256Hex(rawNext)).orElseThrow();
        existing.rotateTo(next.getId(), now);
        return new Rotation(existing.getUserId(), rawNext);
    }

    @Transactional
    public void revoke(String rawToken) {
        repository.findByTokenHash(TokenHashing.sha256Hex(rawToken))
                .ifPresent(token -> token.revoke(Instant.now()));
    }

    @Transactional
    public void revokeAllForUser(UUID userId) {
        repository.revokeAllForUser(userId, Instant.now());
    }

    private String issueInFamily(UUID userId, UUID familyId, String userAgent, String ip) {
        String raw = TokenHashing.randomSecret();
        repository.save(new RefreshToken(userId, TokenHashing.sha256Hex(raw), familyId,
                tokenProvider.refreshExpiry(Instant.now()), userAgent, ip));
        return raw;
    }

    public record Rotation(UUID userId, String rawToken) {
    }
}

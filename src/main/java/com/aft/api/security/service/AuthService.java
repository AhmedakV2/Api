package com.aft.api.security.service;

import java.util.Locale;
import com.aft.api.common.audit.AuditAction;
import com.aft.api.common.audit.AuditLogService;
import com.aft.api.common.exception.ApiException;
import com.aft.api.common.exception.ErrorCode;
import com.aft.api.security.AftPrincipal;
import com.aft.api.security.AftUserDetailsService;
import com.aft.api.security.JwtTokenProvider;
import com.aft.api.security.LoginAttemptService;
import com.aft.api.security.dto.LoginRequest;
import com.aft.api.security.dto.MeResponse;
import com.aft.api.security.dto.TokenResponse;
import com.aft.api.tenant.service.OrganizationService;
import com.aft.api.user.entity.UserAccount;
import com.aft.api.user.entity.UserStatus;
import com.aft.api.user.repository.UserRepository;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AftUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;
    private final OrganizationService organizationService;
    private final AuditLogService auditLog;

    public AuthService(AftUserDetailsService userDetailsService,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       RefreshTokenService refreshTokenService,
                       LoginAttemptService loginAttemptService,
                       OrganizationService organizationService,
                       AuditLogService auditLog) {
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.loginAttemptService = loginAttemptService;
        this.organizationService = organizationService;
        this.auditLog = auditLog;
    }

    @Transactional
    public TokenResponse login(LoginRequest request, String userAgent, String ip) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (loginAttemptService.isLocked(email)) {
            auditLog.record(AuditAction.LOGIN_BLOCKED, "UserAccount", email, null, Map.of("ip", safe(ip)));
            throw new ApiException(ErrorCode.ACCOUNT_LOCKED,
                    "Hesap kilitli, kalan sure: " + loginAttemptService.remainingLock(email).toMinutes() + " dakika");
        }

        AftPrincipal principal;
        try {
            principal = userDetailsService.loadUserByUsername(email);
        } catch (UsernameNotFoundException e) {
            loginAttemptService.recordFailure(email);
            throw new ApiException(ErrorCode.UNAUTHENTICATED, "E-posta veya parola hatali");
        }

        if (!passwordEncoder.matches(request.password(), principal.passwordHash())) {
            loginAttemptService.recordFailure(email);
            auditLog.record(AuditAction.LOGIN_FAILED, "UserAccount", principal.userId().toString(),
                    principal.userId(), Map.of("ip", safe(ip)));
            throw new ApiException(ErrorCode.UNAUTHENTICATED, "E-posta veya parola hatali");
        }
        if (principal.status() != UserStatus.ACTIVE) {
            throw new ApiException(ErrorCode.FORBIDDEN, "Hesap etkin degil");
        }

        loginAttemptService.reset(email);
        userRepository.findById(principal.userId()).ifPresent(user -> user.markLogin(Instant.now()));

        String refreshToken = refreshTokenService.issue(principal.userId(), userAgent, ip);
        auditLog.record(AuditAction.LOGIN_SUCCESS, "UserAccount", principal.userId().toString(),
                principal.userId(), Map.of("ip", safe(ip)));
        log.info("Giris basarili userId={}", principal.userId());

        return TokenResponse.of(tokenProvider.createAccessToken(principal), refreshToken,
                tokenProvider.accessTtlSeconds());
    }

    @Transactional
    public TokenResponse refresh(String rawRefreshToken, String userAgent, String ip) {
        RefreshTokenService.Rotation rotation = refreshTokenService.rotate(rawRefreshToken, userAgent, ip);
        AftPrincipal principal = userDetailsService.loadById(rotation.userId());
        if (principal.status() != UserStatus.ACTIVE) {
            refreshTokenService.revokeAllForUser(principal.userId());
            throw new ApiException(ErrorCode.FORBIDDEN, "Hesap etkin degil");
        }
        return TokenResponse.of(tokenProvider.createAccessToken(principal), rotation.rawToken(),
                tokenProvider.accessTtlSeconds());
    }

    @Transactional
    public void logout(String rawRefreshToken, UUID actorId) {
        refreshTokenService.revoke(rawRefreshToken);
        auditLog.record(AuditAction.LOGOUT, "UserAccount", String.valueOf(actorId), actorId, Map.of());
    }

    @Transactional(readOnly = true)
    public MeResponse me(UUID userId) {
        UserAccount user = userRepository.findWithRolesById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHENTICATED, "Kullanici bulunamadi"));
        return new MeResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getLocale(),
                user.isMfaEnabled(),
                user.getRoles().stream().map(role -> role.getCode().name()).collect(Collectors.toSet()),
                organizationService.findByMember(userId));
    }

    private String safe(String ip) {
        return ip == null ? "" : ip;
    }
}

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
import com.aft.api.security.dto.RegisterRequest;
import com.aft.api.security.dto.TokenResponse;
import com.aft.api.common.util.Slugs;
import com.aft.api.tenant.dto.CreateOrganizationRequest;
import com.aft.api.tenant.repository.OrganizationRepository;
import com.aft.api.tenant.service.OrganizationService;
import com.aft.api.user.entity.Role;
import com.aft.api.user.entity.RoleCode;
import com.aft.api.user.entity.UserAccount;
import com.aft.api.user.entity.UserStatus;
import com.aft.api.user.repository.RoleRepository;
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
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;
    private final OrganizationService organizationService;
    private final OrganizationRepository organizationRepository;
    private final AuditLogService auditLog;

    public AuthService(AftUserDetailsService userDetailsService,
                       UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       RefreshTokenService refreshTokenService,
                       LoginAttemptService loginAttemptService,
                       OrganizationService organizationService,
                       OrganizationRepository organizationRepository,
                       AuditLogService auditLog) {
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.loginAttemptService = loginAttemptService;
        this.organizationService = organizationService;
        this.organizationRepository = organizationRepository;
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
    public TokenResponse register(RegisterRequest request, String userAgent, String ip) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ApiException(ErrorCode.CONFLICT, "Bu e-posta zaten kayitli");
        }

        Role role = roleRepository.findByCode(RoleCode.USER)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "USER rolu tanimli degil"));

        UserAccount user = new UserAccount(email, passwordEncoder.encode(request.password()),
                request.displayName().trim(), "tr");
        user.grant(role);
        userRepository.save(user);

        createWorkspace(user);

        auditLog.record(AuditAction.USER_CREATED, "UserAccount", user.getId().toString(),
                user.getId(), Map.of("ip", safe(ip)));
        log.info("Kayit basarili userId={}", user.getId());

        return login(new LoginRequest(email, request.password()), userAgent, ip);
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

    private void createWorkspace(UserAccount user) {
        String base = user.getDisplayName().isBlank() ? user.getEmail() : user.getDisplayName();
        organizationService.create(new CreateOrganizationRequest(freeName(trim(base))), user.getId());
    }

    private String freeName(String base) {
        if (!organizationRepository.existsBySlug(Slugs.toSlug(base))) {
            return base;
        }
        for (int attempt = 0; attempt < 5; attempt++) {
            String candidate = base + " " + UUID.randomUUID().toString().substring(0, 6);
            if (!organizationRepository.existsBySlug(Slugs.toSlug(candidate))) {
                return candidate;
            }
        }
        throw new ApiException(ErrorCode.CONFLICT, "Calisma alani adi uretilemedi");
    }

    private String trim(String value) {
        String cleaned = value.trim();
        return cleaned.length() > 140 ? cleaned.substring(0, 140) : cleaned;
    }

    private String safe(String ip) {
        return ip == null ? "" : ip;
    }
}
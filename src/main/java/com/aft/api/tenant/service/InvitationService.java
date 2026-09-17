package com.aft.api.tenant.service;

import java.util.Locale;
import com.aft.api.common.audit.AuditAction;
import com.aft.api.common.audit.AuditLogService;
import com.aft.api.common.exception.ApiException;
import com.aft.api.common.exception.ErrorCode;
import com.aft.api.common.exception.NotFoundException;
import com.aft.api.common.exception.ValidationException;
import com.aft.api.security.TokenHashing;
import com.aft.api.tenant.dto.AcceptInvitationRequest;
import com.aft.api.tenant.dto.InvitationDto;
import com.aft.api.tenant.dto.InviteRequest;
import com.aft.api.tenant.entity.Invitation;
import com.aft.api.tenant.repository.InvitationRepository;
import com.aft.api.user.dto.CreateUserRequest;
import com.aft.api.user.entity.Role;
import com.aft.api.user.entity.RoleCode;
import com.aft.api.user.repository.RoleRepository;
import com.aft.api.user.repository.UserRepository;
import com.aft.api.user.service.UserService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvitationService {
    private static final int VALID_DAYS = 7;

    private final InvitationRepository invitationRepository;
    private final MembershipService membershipService;
    private final OrganizationService organizationService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final RoleRepository roleRepository;
    private final AuditLogService auditLog;

    public InvitationService(InvitationRepository invitationRepository,
                             MembershipService membershipService,
                             OrganizationService organizationService,
                             UserRepository userRepository,
                             UserService userService,
                             RoleRepository roleRepository,
                             AuditLogService auditLog) {
        this.invitationRepository = invitationRepository;
        this.membershipService = membershipService;
        this.organizationService = organizationService;
        this.userRepository = userRepository;
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.auditLog = auditLog;
    }

    @Transactional
    public InvitationDto invite(UUID orgId, InviteRequest request, UUID actorId) {
        organizationService.require(orgId);
        Role role = requireRole(request.role());
        String rawToken = TokenHashing.randomSecret();
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        Invitation invitation = invitationRepository.save(new Invitation(orgId, email, role.getId(),
                TokenHashing.sha256Hex(rawToken), Instant.now().plus(VALID_DAYS, ChronoUnit.DAYS)));

        auditLog.record(AuditAction.MEMBER_INVITED, "Invitation", invitation.getId().toString(), actorId,
                Map.of("email", email, "orgId", orgId.toString()));
        return new InvitationDto(invitation.getId(), orgId, email, role.getCode().name(),
                invitation.getExpiresAt(), rawToken);
    }

    @Transactional
    public void accept(AcceptInvitationRequest request) {
        Invitation invitation = invitationRepository.findByTokenHash(TokenHashing.sha256Hex(request.token()))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Davet bulunamadi"));
        if (!invitation.isPending(Instant.now())) {
            throw new ValidationException("Davet suresi dolmus veya zaten kullanilmis");
        }

        UUID userId = userRepository.findWithRolesByEmailIgnoreCase(invitation.getEmail())
                .map(user -> user.getId())
                .orElseGet(() -> createAccount(invitation, request));

        RoleCode roleCode = roleRepository.findById(invitation.getRoleId())
                .orElseThrow(() -> NotFoundException.of("Role", invitation.getRoleId())).getCode();

        membershipService.addMember(invitation.getOrgId(), userId, roleCode);
        invitation.accept(Instant.now());
    }

    private UUID createAccount(Invitation invitation, AcceptInvitationRequest request) {
        if (request.password() == null || request.displayName() == null || request.username() == null) {
            throw new ValidationException("Yeni hesap icin kullanici adi, gorunen ad ve parola zorunludur");
        }
        return userService.create(new CreateUserRequest(request.username(), invitation.getEmail(),
                request.password(), request.displayName(), "tr", Set.of(RoleCode.USER))).id();
    }

    private Role requireRole(RoleCode code) {
        return roleRepository.findByCode(code).orElseThrow(() -> NotFoundException.of("Role", code));
    }
}

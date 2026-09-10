package com.aft.api.tenant.service;

import com.aft.api.common.audit.AuditAction;
import com.aft.api.common.audit.AuditLogService;
import com.aft.api.common.exception.NotFoundException;
import com.aft.api.common.exception.ValidationException;
import com.aft.api.tenant.dto.MemberDto;
import com.aft.api.tenant.entity.Membership;
import com.aft.api.tenant.repository.MembershipRepository;
import com.aft.api.user.entity.Role;
import com.aft.api.user.entity.RoleCode;
import com.aft.api.user.entity.UserAccount;
import com.aft.api.user.repository.RoleRepository;
import com.aft.api.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MembershipService {
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuditLogService auditLog;

    public MembershipService(MembershipRepository membershipRepository,
                             UserRepository userRepository,
                             RoleRepository roleRepository,
                             AuditLogService auditLog) {
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.auditLog = auditLog;
    }

    @Transactional(readOnly = true)
    public List<MemberDto> listMembers(UUID orgId) {
        List<Membership> memberships = membershipRepository.findByOrgId(orgId);
        if (memberships.isEmpty()) {
            return List.of();
        }
        Map<UUID, UserAccount> users = userRepository
                .findAllById(memberships.stream().map(Membership::getUserId).toList()).stream()
                .collect(Collectors.toMap(UserAccount::getId, Function.identity()));
        Map<UUID, Role> roles = roleRepository.findAll().stream()
                .collect(Collectors.toMap(Role::getId, Function.identity()));

        return memberships.stream()
                .filter(membership -> users.containsKey(membership.getUserId()))
                .map(membership -> {
                    UserAccount user = users.get(membership.getUserId());
                    Role role = roles.get(membership.getRoleId());
                    return new MemberDto(user.getId(), user.getEmail(), user.getDisplayName(),
                            role == null ? null : role.getCode().name(), membership.getJoinedAt());
                })
                .toList();
    }

    @Transactional
    public void addMember(UUID orgId, UUID userId, RoleCode roleCode) {
        if (membershipRepository.existsByUserIdAndOrgId(userId, orgId)) {
            return;
        }
        UUID roleId = requireRole(roleCode).getId();
        membershipRepository.save(new Membership(userId, orgId, roleId));
        auditLog.record(AuditAction.MEMBER_JOINED, "Membership", orgId + ":" + userId, userId,
                Map.of("role", roleCode.name()));
    }

    @Transactional
    public void removeMember(UUID orgId, UUID userId, UUID actorId) {
        Membership membership = membershipRepository.findByUserIdAndOrgId(userId, orgId)
                .orElseThrow(() -> NotFoundException.of("Membership", orgId + ":" + userId));

        UUID ownerRoleId = requireRole(RoleCode.OWNER).getId();
        if (membership.getRoleId().equals(ownerRoleId)
                && membershipRepository.countByOrgIdAndRoleId(orgId, ownerRoleId) <= 1) {
            throw new ValidationException("Organizasyonun son sahibi cikarilamaz");
        }
        membershipRepository.delete(membership);
        auditLog.record(AuditAction.MEMBER_REMOVED, "Membership", orgId + ":" + userId, actorId, Map.of());
    }

    private Role requireRole(RoleCode code) {
        return roleRepository.findByCode(code).orElseThrow(() -> NotFoundException.of("Role", code));
    }
}

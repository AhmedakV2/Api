package com.aft.api.tenant.service;

import com.aft.api.common.audit.AuditAction;
import com.aft.api.common.audit.AuditLogService;
import com.aft.api.common.exception.ConflictException;
import com.aft.api.common.exception.NotFoundException;
import com.aft.api.common.util.Slugs;
import com.aft.api.tenant.dto.CreateOrganizationRequest;
import com.aft.api.tenant.dto.OrganizationDto;
import com.aft.api.tenant.dto.UpdateOrganizationRequest;
import com.aft.api.tenant.entity.Membership;
import com.aft.api.tenant.entity.Organization;
import com.aft.api.tenant.mapper.TenantMapper;
import com.aft.api.tenant.repository.MembershipRepository;
import com.aft.api.tenant.repository.OrganizationRepository;
import com.aft.api.user.entity.RoleCode;
import com.aft.api.user.repository.RoleRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final MembershipRepository membershipRepository;
    private final RoleRepository roleRepository;
    private final TenantMapper tenantMapper;
    private final AuditLogService auditLog;

    public OrganizationService(OrganizationRepository organizationRepository,
                               MembershipRepository membershipRepository,
                               RoleRepository roleRepository,
                               TenantMapper tenantMapper,
                               AuditLogService auditLog) {
        this.organizationRepository = organizationRepository;
        this.membershipRepository = membershipRepository;
        this.roleRepository = roleRepository;
        this.tenantMapper = tenantMapper;
        this.auditLog = auditLog;
    }

    @Transactional
    public OrganizationDto create(CreateOrganizationRequest request, UUID creatorId) {
        String slug = Slugs.toSlug(request.name());
        if (organizationRepository.existsBySlug(slug)) {
            throw new ConflictException("Bu ada sahip bir organizasyon zaten var: " + slug);
        }
        Organization organization = organizationRepository.save(new Organization(request.name().trim(), slug));

        UUID ownerRoleId = roleRepository.findByCode(RoleCode.OWNER)
                .orElseThrow(() -> NotFoundException.of("Role", RoleCode.OWNER)).getId();
        membershipRepository.save(new Membership(creatorId, organization.getId(), ownerRoleId));

        auditLog.record(AuditAction.ORGANIZATION_CREATED, "Organization", organization.getId().toString(),
                creatorId, Map.of("slug", slug));
        return tenantMapper.toDto(organization);
    }

    @Transactional(readOnly = true)
    public List<OrganizationDto> findByMember(UUID userId) {
        return organizationRepository.findByMember(userId).stream().map(tenantMapper::toDto).toList();
    }

    @Transactional
    public OrganizationDto update(UUID orgId, UpdateOrganizationRequest request, UUID actorId) {
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> NotFoundException.of("Organization", orgId));
        if (request.name() != null && !request.name().isBlank()) {
            organization.rename(request.name().trim());
        }
        if (request.status() != null) {
            organization.changeStatus(request.status());
        }
        if (request.aiTokenBudgetDaily() != null) {
            organization.changeBudget(request.aiTokenBudgetDaily());
        }
        auditLog.record(AuditAction.ORGANIZATION_UPDATED, "Organization", orgId.toString(), actorId, Map.of());
        return tenantMapper.toDto(organization);
    }

    @Transactional(readOnly = true)
    public Organization require(UUID orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> NotFoundException.of("Organization", orgId));
    }
}

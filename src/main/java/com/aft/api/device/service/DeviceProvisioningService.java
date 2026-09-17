package com.aft.api.device.service;

import com.aft.api.common.audit.AuditAction;
import com.aft.api.common.audit.AuditLogService;
import com.aft.api.common.exception.ValidationException;
import com.aft.api.device.dto.DeviceDto;
import com.aft.api.device.dto.DeviceProvisionDto;
import com.aft.api.device.dto.DeviceRegisterRequest;
import com.aft.api.security.dto.ApiKeyCreatedDto;
import com.aft.api.security.dto.CreateApiKeyRequest;
import com.aft.api.security.repository.ApiKeyRepository;
import com.aft.api.security.service.ApiKeyService;
import com.aft.api.tenant.dto.OrganizationDto;
import com.aft.api.tenant.service.OrganizationService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceProvisioningService {
    private static final Logger log = LoggerFactory.getLogger(DeviceProvisioningService.class);
    private static final String KEY_NAME = "AFT Masaustu Istemci";
    private static final String DEVICE_SCOPE = "device";

    private final OrganizationService organizationService;
    private final ApiKeyService apiKeyService;
    private final ApiKeyRepository apiKeyRepository;
    private final DeviceRegistryService registryService;
    private final AuditLogService auditLog;

    public DeviceProvisioningService(OrganizationService organizationService,
                                     ApiKeyService apiKeyService,
                                     ApiKeyRepository apiKeyRepository,
                                     DeviceRegistryService registryService,
                                     AuditLogService auditLog) {
        this.organizationService = organizationService;
        this.apiKeyService = apiKeyService;
        this.apiKeyRepository = apiKeyRepository;
        this.registryService = registryService;
        this.auditLog = auditLog;
    }

    @Transactional
    public DeviceProvisionDto provision(UUID userId, DeviceRegisterRequest request) {
        UUID orgId = defaultOrg(userId);
        revokePrevious(orgId, userId);

        ApiKeyCreatedDto created = apiKeyService.create(userId,
                new CreateApiKeyRequest(KEY_NAME, orgId, Set.of(DEVICE_SCOPE), null));
        DeviceDto device = registryService.bind(orgId, userId, created.key().id(), request);

        auditLog.record(AuditAction.DEVICE_PROVISIONED, "ClientDevice", device.id().toString(), userId,
                Map.of("orgId", orgId.toString(), "hostname", request.hostname()));
        log.info("Istemci otomatik saglandi userId={} org={} device={}", userId, orgId, device.id());
        return new DeviceProvisionDto(orgId, created.secret(), device);
    }

    private UUID defaultOrg(UUID userId) {
        List<OrganizationDto> organizations = organizationService.findByMember(userId);
        if (organizations.isEmpty()) {
            throw new ValidationException("Kullanicinin bagli oldugu bir calisma alani yok");
        }
        return organizations.getFirst().id();
    }

    private void revokePrevious(UUID orgId, UUID userId) {
        Instant now = Instant.now();
        apiKeyRepository.findByOrgIdAndOwnerIdAndNameAndRevokedAtIsNull(orgId, userId, KEY_NAME)
                .forEach(key -> key.revoke(now));
    }
}

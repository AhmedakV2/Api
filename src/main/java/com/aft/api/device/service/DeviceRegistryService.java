package com.aft.api.device.service;

import com.aft.api.common.audit.AuditAction;
import com.aft.api.common.audit.AuditLogService;
import com.aft.api.common.exception.ForbiddenException;
import com.aft.api.common.exception.NotFoundException;
import com.aft.api.device.dto.CapabilityBulkRequest;
import com.aft.api.device.dto.DeviceCapabilityDto;
import com.aft.api.device.dto.DeviceDto;
import com.aft.api.device.dto.DeviceRegisterRequest;
import com.aft.api.device.entity.ClientDevice;
import com.aft.api.device.entity.DeviceCapability;
import com.aft.api.device.repository.ClientDeviceRepository;
import com.aft.api.device.repository.DeviceCapabilityRepository;
import com.aft.api.security.ApiKeyPrincipal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceRegistryService {
    private static final Logger log = LoggerFactory.getLogger(DeviceRegistryService.class);

    private final ClientDeviceRepository deviceRepository;
    private final DeviceCapabilityRepository capabilityRepository;
    private final AuditLogService auditLog;

    public DeviceRegistryService(ClientDeviceRepository deviceRepository,
                                 DeviceCapabilityRepository capabilityRepository,
                                 AuditLogService auditLog) {
        this.deviceRepository = deviceRepository;
        this.capabilityRepository = capabilityRepository;
        this.auditLog = auditLog;
    }

    @Transactional
    public DeviceDto register(ApiKeyPrincipal apiKey, DeviceRegisterRequest request) {
        ClientDevice device = deviceRepository.findByApiKeyId(apiKey.apiKeyId())
                .map(existing -> {
                    existing.refresh(request.hostname(), request.os(), request.appVersion());
                    return existing;
                })
                .orElseGet(() -> deviceRepository.save(new ClientDevice(apiKey.orgId(), apiKey.ownerId(),
                        request.hostname(), request.os(), request.appVersion(), apiKey.apiKeyId())));

        device.markSeen(Instant.now());
        auditLog.record(AuditAction.DEVICE_REGISTERED, "ClientDevice", device.getId().toString(),
                apiKey.ownerId(), Map.of("hostname", request.hostname()));
        log.info("Istemci kaydedildi id={} org={}", device.getId(), apiKey.orgId());
        return toDto(device);
    }

    @Transactional
    public DeviceDto replaceCapabilities(UUID deviceId, ApiKeyPrincipal apiKey, CapabilityBulkRequest request) {
        ClientDevice device = requireOwnDevice(deviceId, apiKey);

        capabilityRepository.deleteByKeyDeviceId(deviceId);
        capabilityRepository.flush();
        request.capabilities().forEach(dto -> capabilityRepository.save(
                new DeviceCapability(deviceId, dto.toolName(), dto.schemaVersion(), dto.enabled())));

        device.markSeen(Instant.now());
        auditLog.record(AuditAction.DEVICE_CAPABILITIES_UPDATED, "ClientDevice", deviceId.toString(),
                apiKey.ownerId(), Map.of("count", request.capabilities().size()));
        return toDto(device);
    }

    @Transactional(readOnly = true)
    public List<DeviceDto> listByOrg(UUID orgId) {
        return deviceRepository.findByOrgIdOrderByLastSeenAtDesc(orgId).stream().map(this::toDto).toList();
    }

    @Transactional
    public void remove(UUID deviceId, UUID orgId, UUID actorId) {
        ClientDevice device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> NotFoundException.of("ClientDevice", deviceId));
        if (!device.getOrgId().equals(orgId)) {
            throw new ForbiddenException("Cihaz baska bir organizasyona ait");
        }
        capabilityRepository.deleteByKeyDeviceId(deviceId);
        deviceRepository.delete(device);
        auditLog.record(AuditAction.DEVICE_REMOVED, "ClientDevice", deviceId.toString(), actorId, Map.of());
    }

    ClientDevice requireOwnDevice(UUID deviceId, ApiKeyPrincipal apiKey) {
        ClientDevice device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> NotFoundException.of("ClientDevice", deviceId));
        if (!apiKey.apiKeyId().equals(device.getApiKeyId())) {
            throw new ForbiddenException("Bu cihaz uzerinde islem yetkiniz yok");
        }
        return device;
    }

    private DeviceDto toDto(ClientDevice device) {
        List<DeviceCapabilityDto> capabilities = capabilityRepository.findByKeyDeviceId(device.getId()).stream()
                .map(cap -> new DeviceCapabilityDto(cap.getKey().toolName(), cap.getSchemaVersion(),
                        cap.isEnabled()))
                .toList();
        return new DeviceDto(device.getId(), device.getOrgId(), device.getUserId(), device.getHostname(),
                device.getOs(), device.getAppVersion(), device.getStatus().name(),
                device.getLastSeenAt(), capabilities);
    }
}

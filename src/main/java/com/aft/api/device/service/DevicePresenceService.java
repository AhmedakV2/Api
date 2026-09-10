package com.aft.api.device.service;

import com.aft.api.device.entity.ClientDevice;
import com.aft.api.device.entity.DeviceStatus;
import com.aft.api.device.repository.ClientDeviceRepository;
import com.aft.api.security.ApiKeyPrincipal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DevicePresenceService {
    private static final Logger log = LoggerFactory.getLogger(DevicePresenceService.class);
    private static final Duration STALE_AFTER = Duration.ofMinutes(2);

    private final ClientDeviceRepository deviceRepository;
    private final DeviceRegistryService registryService;

    public DevicePresenceService(ClientDeviceRepository deviceRepository, DeviceRegistryService registryService) {
        this.deviceRepository = deviceRepository;
        this.registryService = registryService;
    }

    @Transactional
    public Instant heartbeat(UUID deviceId, ApiKeyPrincipal apiKey) {
        ClientDevice device = registryService.requireOwnDevice(deviceId, apiKey);
        Instant now = Instant.now();
        device.markSeen(now);
        return now;
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void expireStaleDevices() {
        int changed = deviceRepository.markStaleOffline(DeviceStatus.ONLINE, Instant.now().minus(STALE_AFTER));
        if (changed > 0) {
            log.debug("Cevrimdisi isaretlenen istemci adedi={}", changed);
        }
    }
}

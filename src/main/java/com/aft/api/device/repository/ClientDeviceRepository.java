package com.aft.api.device.repository;

import com.aft.api.device.entity.ClientDevice;
import com.aft.api.device.entity.DeviceStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientDeviceRepository extends JpaRepository<ClientDevice, UUID> {
    Optional<ClientDevice> findByApiKeyId(UUID apiKeyId);

    Optional<ClientDevice> findFirstByOrgIdAndUserIdAndHostnameOrderByCreatedAtAsc(UUID orgId, UUID userId,
                                                                                  String hostname);

    List<ClientDevice> findByOrgIdOrderByLastSeenAtDesc(UUID orgId);

    @Modifying
    @Query("""
            UPDATE ClientDevice d SET d.status = com.aft.api.device.entity.DeviceStatus.OFFLINE
            WHERE d.status = :online AND (d.lastSeenAt IS NULL OR d.lastSeenAt < :threshold)
            """)
    int markStaleOffline(@Param("online") DeviceStatus online, @Param("threshold") Instant threshold);
}

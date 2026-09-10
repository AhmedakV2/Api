package com.aft.api.device.repository;

import com.aft.api.device.entity.DeviceCapability;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceCapabilityRepository extends JpaRepository<DeviceCapability, DeviceCapability.Key> {
    List<DeviceCapability> findByKeyDeviceId(UUID deviceId);

    void deleteByKeyDeviceId(UUID deviceId);
}

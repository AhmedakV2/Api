package com.aft.api.realtime;

import com.aft.api.state.KeyValueStore;
import com.aft.api.state.StateNamespaces;
import com.aft.api.state.StateStoreProvider;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class DeviceSessionRegistry {
    private static final Duration TTL = Duration.ofHours(12);

    private final KeyValueStore devices;
    private final String instanceId = UUID.randomUUID().toString();

    public DeviceSessionRegistry(StateStoreProvider stateStores) {
        this.devices = stateStores.keyValue(StateNamespaces.WS_DEVICE);
    }

    public void online(UUID deviceId, String sessionId) {
        devices.put(deviceId.toString(), instanceId + "|" + sessionId, TTL);
    }

    public void offline(UUID deviceId) {
        devices.delete(deviceId.toString());
    }

    public boolean isOnline(UUID deviceId) {
        return devices.contains(deviceId.toString());
    }

    public Optional<String> ownerInstance(UUID deviceId) {
        return devices.get(deviceId.toString()).map(value -> value.split("\\|")[0]);
    }

    public String instanceId() {
        return instanceId;
    }
}

package com.aft.api.device.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "device_capability")
public class DeviceCapability {
    @EmbeddedId
    private Key key;

    @Column(name = "schema_version", nullable = false)
    private int schemaVersion = 1;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    protected DeviceCapability() {
    }

    public DeviceCapability(UUID deviceId, String toolName, int schemaVersion, boolean enabled) {
        this.key = new Key(deviceId, toolName);
        this.schemaVersion = schemaVersion;
        this.enabled = enabled;
    }

    public Key getKey() {
        return key;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Embeddable
    public record Key(@Column(name = "device_id") UUID deviceId,
                      @Column(name = "tool_name", length = 64) String toolName) implements Serializable {
    }
}

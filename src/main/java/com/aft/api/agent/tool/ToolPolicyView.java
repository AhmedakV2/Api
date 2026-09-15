package com.aft.api.agent.tool;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Web katmanina arac katalogunu sunan dar arayuz. */
@Component
public class ToolPolicyView {

    private final ToolRegistry registry;

    public ToolPolicyView(ToolRegistry registry) {
        this.registry = registry;
    }

    public List<ToolSpec> catalog() {
        return registry.catalog();
    }

    public List<ToolSpec> catalogFor(UUID deviceId) {
        return registry.catalogFor(deviceId);
    }
}

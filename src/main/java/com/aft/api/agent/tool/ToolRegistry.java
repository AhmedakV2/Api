package com.aft.api.agent.tool;

import com.aft.api.device.service.DeviceRegistryService;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

@Component
public class ToolRegistry {
    private final Map<String, ToolSpec> specs;
    private final RemoteToolExecutor executor;
    private final DeviceRegistryService deviceRegistry;

    public ToolRegistry(List<ToolSpec> available,
                        RemoteToolExecutor executor,
                        DeviceRegistryService deviceRegistry) {
        this.specs = available.stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(ToolSpec::name, Function.identity()));
        this.executor = executor;
        this.deviceRegistry = deviceRegistry;
    }

    public List<ToolCallback> callbacksFor(UUID deviceId) {
        return callbacksFor(deviceId, Set.of());
    }

    public List<ToolCallback> callbacksFor(UUID deviceId, Set<String> allowed) {
        return catalogFor(deviceId, allowed).stream()
                .map(spec -> (ToolCallback) new RemoteToolCallback(spec, executor))
                .toList();
    }

    public List<ToolSpec> catalog() {
        return specs.values().stream().sorted(Comparator.comparing(ToolSpec::name)).toList();
    }

    public List<ToolSpec> catalogFor(UUID deviceId) {
        return catalogFor(deviceId, Set.of());
    }

    public List<ToolSpec> catalogFor(UUID deviceId, Set<String> allowed) {
        if (deviceId == null) {
            return List.of();
        }
        Set<String> enabled = deviceRegistry.enabledToolNames(deviceId);
        return specs.values().stream()
                .filter(spec -> enabled.contains(spec.name()))
                .filter(spec -> allowed.isEmpty() || allowed.contains(spec.name()))
                .sorted(Comparator.comparing(ToolSpec::name))
                .toList();
    }
}

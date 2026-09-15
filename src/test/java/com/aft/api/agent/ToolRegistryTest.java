package com.aft.api.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.aft.api.agent.tool.RemoteToolExecutor;
import com.aft.api.agent.tool.ToolNames;
import com.aft.api.agent.tool.ToolRegistry;
import com.aft.api.agent.tool.ToolSpec;
import com.aft.api.agent.tool.spec.BrowserCommandSpec;
import com.aft.api.agent.tool.spec.LocalRunHistorySpec;
import com.aft.api.agent.tool.spec.LocalScenarioSearchSpec;
import com.aft.api.device.service.DeviceRegistryService;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.ai.tool.ToolCallback;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ToolRegistryTest {

    private static final UUID DEVICE_ID = UUID.randomUUID();

    @Mock
    private RemoteToolExecutor executor;
    @Mock
    private DeviceRegistryService deviceRegistry;

    private ToolRegistry registry;

    @BeforeEach
    void setUp() {
        List<ToolSpec> specs = List.of(new LocalScenarioSearchSpec(), new LocalRunHistorySpec(),
                new BrowserCommandSpec());
        registry = new ToolRegistry(specs, executor, deviceRegistry);
    }

    @Test
    void modeleYalnizcaCihazinDestekledigiAraclarSunulur() {
        when(deviceRegistry.enabledToolNames(DEVICE_ID))
                .thenReturn(Set.of(ToolNames.LOCAL_SCENARIO_SEARCH, ToolNames.LOCAL_RUN_HISTORY));

        List<ToolCallback> callbacks = registry.callbacksFor(DEVICE_ID);

        assertThat(callbacks).hasSize(2);
        assertThat(callbacks).extracting(c -> c.getToolDefinition().name())
                .containsExactly(ToolNames.LOCAL_RUN_HISTORY, ToolNames.LOCAL_SCENARIO_SEARCH);
    }

    @Test
    void cihazBildirmediyseAracSunulmaz() {
        when(deviceRegistry.enabledToolNames(DEVICE_ID)).thenReturn(Set.of());

        assertThat(registry.callbacksFor(DEVICE_ID)).isEmpty();
    }

    @Test
    void cihazYoksaAracSunulmaz() {
        assertThat(registry.callbacksFor(null)).isEmpty();
        assertThat(registry.catalogFor(null)).isEmpty();
    }

    @Test
    void katalogTumAraclariIcerir() {
        assertThat(registry.catalog()).hasSize(3);
    }

    @Test
    void aracTanimiSemaVeAciklamaTasir() {
        when(deviceRegistry.enabledToolNames(DEVICE_ID)).thenReturn(Set.of(ToolNames.LOCAL_SCENARIO_SEARCH));

        var definition = registry.callbacksFor(DEVICE_ID).getFirst().getToolDefinition();

        assertThat(definition.name()).isEqualTo(ToolNames.LOCAL_SCENARIO_SEARCH);
        assertThat(definition.description()).isNotBlank();
        assertThat(definition.inputSchema()).contains("\"query\"");
    }

    @Test
    void yazmaEtkisiOlanAraclarIsaretlidir() {
        assertThat(registry.catalog()).filteredOn(ToolSpec::writeEffect)
                .extracting(ToolSpec::name)
                .containsExactly(ToolNames.BROWSER_COMMAND);
    }
}

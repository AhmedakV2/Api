package com.aft.api.agent;

import static org.assertj.core.api.Assertions.assertThat;

import com.aft.api.agent.guard.ToolPolicy;
import com.aft.api.agent.tool.spec.BrowserCommandSpec;
import com.aft.api.agent.tool.spec.LocalScenarioSearchSpec;
import com.aft.api.agent.tool.spec.ScenarioDraftWriteSpec;
import com.aft.api.config.AiProperties;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class ToolPolicyTest {

    private final ToolPolicy policy = new ToolPolicy(new AiProperties(null, "FAST",
            Duration.ofSeconds(120), 40, 24000, Duration.ofSeconds(30), 12, 262144));

    @Test
    void yazmaEtkisiOlanAraclarOnayIster() {
        assertThat(policy.requiresApproval(new BrowserCommandSpec())).isTrue();
        assertThat(policy.requiresApproval(new ScenarioDraftWriteSpec())).isTrue();
    }

    @Test
    void saltOkunurAraclarOnayIstemez() {
        assertThat(policy.requiresApproval(new LocalScenarioSearchSpec())).isFalse();
    }

    @Test
    void dokumandakiSinirlarUygulanir() {
        assertThat(policy.maxHops()).isEqualTo(12);
        assertThat(policy.timeoutMs()).isEqualTo(30_000L);
        assertThat(policy.maxResultBytes()).isEqualTo(256 * 1024);
    }

    @Test
    void varsayilanlarYapilandirmasizDaGecerli() {
        ToolPolicy fallback = new ToolPolicy(new AiProperties(null, null, null, 0, 0, null, 0, 0));

        assertThat(fallback.maxHops()).isEqualTo(12);
        assertThat(fallback.timeoutMs()).isEqualTo(30_000L);
        assertThat(fallback.maxResultBytes()).isEqualTo(262144);
    }
}

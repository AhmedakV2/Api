package com.aft.api.agent.guard;

import com.aft.api.agent.tool.ToolSpec;
import com.aft.api.config.AiProperties;
import org.springframework.stereotype.Component;

@Component
public class ToolPolicy {
    private final AiProperties properties;

    public ToolPolicy(AiProperties properties) {
        this.properties = properties;
    }

    public boolean requiresApproval(ToolSpec spec) {
        return spec.writeEffect();
    }

    public int maxHops() {
        return properties.maxToolHops();
    }

    public long timeoutMs() {
        return properties.toolTimeout().toMillis();
    }

    public int maxResultBytes() {
        return properties.maxToolResultBytes();
    }
}

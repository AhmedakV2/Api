package com.aft.api.agent.tool.spec;

import com.aft.api.agent.tool.ToolNames;
import com.aft.api.agent.tool.ToolSpec;
import org.springframework.stereotype.Component;

@Component
public class LocalScenarioRunSpec implements ToolSpec {

    @Override
    public String name() {
        return ToolNames.LOCAL_SCENARIO_RUN;
    }

    @Override
    public String description() {
        return "Bir senaryoyu tarayicida bastan sona kosar ve adim adim sonucunu doner. "
                + "Kullanici onayi gerektirir.";
    }

    @Override
    public String inputSchema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "scenarioId": { "type": "string", "description": "Kosulacak senaryo kimligi" },
                    "scenario": { "type": "object", "description": "Kayitli degilse dogrudan senaryo govdesi" },
                    "headless": { "type": "boolean", "default": false },
                    "stopOnFailure": { "type": "boolean", "default": true },
                    "stepTimeoutMs": { "type": "integer", "minimum": 0 }
                  }
                }
                """;
    }

    @Override
    public boolean writeEffect() {
        return true;
    }

    @Override
    public long timeoutMs() {
        return 600_000L;
    }
}

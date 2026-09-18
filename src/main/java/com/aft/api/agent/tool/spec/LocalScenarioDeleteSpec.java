package com.aft.api.agent.tool.spec;

import com.aft.api.agent.tool.ToolNames;
import com.aft.api.agent.tool.ToolSpec;
import org.springframework.stereotype.Component;

@Component
public class LocalScenarioDeleteSpec implements ToolSpec {

    @Override
    public String name() {
        return ToolNames.LOCAL_SCENARIO_DELETE;
    }

    @Override
    public String description() {
        return "Bir senaryoyu kutuphaneden kalici olarak siler. Kullanici onayi gerektirir.";
    }

    @Override
    public String inputSchema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "scenarioId": { "type": "string" }
                  },
                  "required": ["scenarioId"]
                }
                """;
    }

    @Override
    public boolean writeEffect() {
        return true;
    }
}

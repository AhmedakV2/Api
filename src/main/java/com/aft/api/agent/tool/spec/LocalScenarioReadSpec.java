package com.aft.api.agent.tool.spec;

import com.aft.api.agent.tool.ToolNames;
import com.aft.api.agent.tool.ToolSpec;
import org.springframework.stereotype.Component;

@Component
public class LocalScenarioReadSpec implements ToolSpec {

    @Override
    public String name() {
        return ToolNames.LOCAL_SCENARIO_READ;
    }

    @Override
    public String description() {
        return "Bir senaryonun adim listesini ve ust verisini okur.";
    }

    @Override
    public String inputSchema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "scenarioId": { "type": "string", "description": "Senaryo kimligi" }
                  },
                  "required": ["scenarioId"]
                }
                """;
    }
}

package com.aft.api.agent.tool.spec;

import com.aft.api.agent.tool.ToolNames;
import com.aft.api.agent.tool.ToolSpec;
import org.springframework.stereotype.Component;

@Component
public class LocalScenarioValidateSpec implements ToolSpec {

    @Override
    public String name() {
        return ToolNames.LOCAL_SCENARIO_VALIDATE;
    }

    @Override
    public String description() {
        return "Bir senaryoyu calistirmadan dogrular ve hata, uyari listesini doner. "
                + "scenarioId verilirse kayitli senaryo, scenario verilirse taslak dogrulanir.";
    }

    @Override
    public String inputSchema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "scenarioId": { "type": "string", "description": "Kayitli senaryo kimligi" },
                    "scenario": { "type": "object", "description": "Dogrulanacak senaryo taslagi" }
                  }
                }
                """;
    }
}

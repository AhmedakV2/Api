package com.aft.api.agent.tool.spec;

import com.aft.api.agent.tool.ToolNames;
import com.aft.api.agent.tool.ToolSpec;
import org.springframework.stereotype.Component;

@Component
public class LocalScenarioListSpec implements ToolSpec {

    @Override
    public String name() {
        return ToolNames.LOCAL_SCENARIO_LIST;
    }

    @Override
    public String description() {
        return "Kayitli tum senaryolari ve klasor agacini listeler. Kutuphanede ne var sorusu icin bunu kullan.";
    }

    @Override
    public String inputSchema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "folder": { "type": "string", "description": "Verilirse yalnizca bu klasordeki senaryolar" },
                    "limit": { "type": "integer", "minimum": 1, "maximum": 500, "default": 100 }
                  }
                }
                """;
    }
}

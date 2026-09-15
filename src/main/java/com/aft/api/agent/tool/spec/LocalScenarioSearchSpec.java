package com.aft.api.agent.tool.spec;

import com.aft.api.agent.tool.ToolNames;
import com.aft.api.agent.tool.ToolSpec;
import org.springframework.stereotype.Component;

@Component
public class LocalScenarioSearchSpec implements ToolSpec {

    @Override
    public String name() {
        return ToolNames.LOCAL_SCENARIO_SEARCH;
    }

    @Override
    public String description() {
        return "Yerel senaryo kutuphanesinde metne gore arama yapar ve eslesen senaryolarin ozetini doner.";
    }

    @Override
    public String inputSchema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "query": { "type": "string", "description": "Aranacak metin" },
                    "limit": { "type": "integer", "minimum": 1, "maximum": 50, "default": 10 }
                  },
                  "required": ["query"]
                }
                """;
    }
}

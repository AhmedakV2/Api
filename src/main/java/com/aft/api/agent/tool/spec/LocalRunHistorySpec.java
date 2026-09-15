package com.aft.api.agent.tool.spec;

import com.aft.api.agent.tool.ToolNames;
import com.aft.api.agent.tool.ToolSpec;
import org.springframework.stereotype.Component;

@Component
public class LocalRunHistorySpec implements ToolSpec {

    @Override
    public String name() {
        return ToolNames.LOCAL_RUN_HISTORY;
    }

    @Override
    public String description() {
        return "Bir senaryonun kosum gecmisini getirir. Durum filtresi ve sayfalama destekler.";
    }

    @Override
    public String inputSchema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "scenarioId": { "type": "string", "description": "Bos birakilirsa tum senaryolar" },
                    "status": {
                      "type": "string",
                      "enum": ["passed", "failed", "errored", "aborted"],
                      "description": "Verilmezse tum durumlar"
                    },
                    "limit": { "type": "integer", "minimum": 1, "maximum": 200, "default": 50 },
                    "offset": { "type": "integer", "minimum": 0, "default": 0 }
                  }
                }
                """;
    }
}

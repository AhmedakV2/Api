package com.aft.api.agent.tool.spec;

import com.aft.api.agent.tool.ToolNames;
import com.aft.api.agent.tool.ToolSpec;
import org.springframework.stereotype.Component;

@Component
public class LocalRunDetailSpec implements ToolSpec {

    @Override
    public String name() {
        return ToolNames.LOCAL_RUN_DETAIL;
    }

    @Override
    public String description() {
        return "Bir kosumun tum adimlarini, sureleri ve uretilen baglam paketlerini doner. "
                + "runId bos birakilirsa son kosum okunur.";
    }

    @Override
    public String inputSchema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "runId": { "type": "string", "description": "Bos birakilirsa son kosum" }
                  }
                }
                """;
    }
}

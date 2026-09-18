package com.aft.api.agent.tool.spec;

import com.aft.api.agent.tool.ToolNames;
import com.aft.api.agent.tool.ToolSpec;
import org.springframework.stereotype.Component;

@Component
public class PageSnapshotSpec implements ToolSpec {

    @Override
    public String name() {
        return ToolNames.PAGE_SNAPSHOT;
    }

    @Override
    public String description() {
        return "Acik sayfanin element grafigini cikarir. Tarama seviyesi arttikca daha fazla ayrinti gelir.";
    }

    @Override
    public String inputSchema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "level": {
                      "type": "integer",
                      "enum": [0, 1, 2, 3],
                      "default": 1,
                      "description": "0 en yuzeysel, 3 en ayrintili tarama"
                    },
                    "force": {
                      "type": "boolean",
                      "default": false,
                      "description": "Onbellekteki grafigi yok sayip yeniden tara"
                    }
                  }
                }
                """;
    }

    @Override
    public long timeoutMs() {
        return 120_000L;
    }
}

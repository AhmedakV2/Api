package com.aft.api.agent.tool.spec;

import com.aft.api.agent.tool.ToolNames;
import com.aft.api.agent.tool.ToolSpec;
import org.springframework.stereotype.Component;

@Component
public class LocalDescriptorSearchSpec implements ToolSpec {

    @Override
    public String name() {
        return ToolNames.LOCAL_DESCRIPTOR_SEARCH;
    }

    @Override
    public String description() {
        return "Adres kalibina gore descriptor katalogunda arama yapar. "
                + "element verilirse sonuc istemcide ad, rol ve etiket uzerinden suzulur.";
    }

    @Override
    public String inputSchema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "urlPattern": { "type": "string", "description": "Adres kalibi" },
                    "element": { "type": "string", "description": "Istege bagli ad, rol veya etiket suzgeci" },
                    "limit": { "type": "integer", "minimum": 1, "maximum": 100, "default": 25 }
                  },
                  "required": ["urlPattern"]
                }
                """;
    }
}

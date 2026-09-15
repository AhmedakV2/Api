package com.aft.api.agent.tool.spec;

import com.aft.api.agent.tool.ToolNames;
import com.aft.api.agent.tool.ToolSpec;
import org.springframework.stereotype.Component;

@Component
public class ScenarioDraftWriteSpec implements ToolSpec {

    @Override
    public String name() {
        return ToolNames.SCENARIO_DRAFT_WRITE;
    }

    @Override
    public String description() {
        return "Senaryo taslagini istemciye yazar. id bos birakilirsa yeni senaryo acilir. "
                + "Kullanici onayi gerektirir.";
    }

    @Override
    public String inputSchema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "id": { "type": "string", "description": "Bos birakilirsa istemci yeni kimlik uretir" },
                    "title": { "type": "string" },
                    "description": { "type": "string", "default": "" },
                    "baseUrl": { "type": "string", "description": "Senaryonun basladigi adres" },
                    "steps": {
                      "type": "array",
                      "items": { "type": "object" },
                      "description": "ScenarioStep listesi"
                    },
                    "folder": { "type": "string", "description": "Hedef klasor, bos ise kok" }
                  },
                  "required": ["title", "baseUrl", "steps"]
                }
                """;
    }

    @Override
    public boolean writeEffect() {
        return true;
    }
}

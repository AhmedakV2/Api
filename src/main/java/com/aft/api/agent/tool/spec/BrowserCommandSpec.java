package com.aft.api.agent.tool.spec;

import com.aft.api.agent.tool.ToolNames;
import com.aft.api.agent.tool.ToolSpec;
import org.springframework.stereotype.Component;

@Component
public class BrowserCommandSpec implements ToolSpec {

    @Override
    public String name() {
        return ToolNames.BROWSER_COMMAND;
    }

    @Override
    public String description() {
        return "Tarayicida tek bir eylem calistirir ve sonucunu doner. Hedefi page_snapshot ciktisindaki "
                + "element.ref degeriyle ver; descriptorId veya ordinal de kullanilabilir. "
                + "Kalici bir akis kurmak icin bunun yerine scenario_draft_write kullan. "
                + "Kullanici onayi gerektirir.";
    }

    @Override
    public String inputSchema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "kind": {
                      "type": "string",
                      "enum": ["click", "double-click", "right-click", "hover", "type", "clear-type",
                               "press-key", "scroll", "select-option", "upload", "navigate", "wait",
                               "refresh"]
                    },
                    "ref": { "type": "string", "description": "Element grafigindeki referans" },
                    "descriptorId": { "type": "string", "description": "Katalogdaki descriptor kimligi" },
                    "ordinal": { "type": "integer", "minimum": 0 },
                    "text": { "type": "string", "description": "type ve clear-type icin yazilacak metin" },
                    "key": { "type": "string", "description": "press-key icin tus adi" },
                    "url": { "type": "string", "description": "navigate icin adres" },
                    "deltaY": { "type": "integer", "description": "scroll icin dikey mesafe" },
                    "optionValue": { "type": "string", "description": "select-option icin deger" },
                    "timeoutMs": { "type": "integer", "minimum": 0 },
                    "waitMs": { "type": "integer", "minimum": 0, "description": "wait icin sure" },
                    "force": { "type": "boolean", "default": false },
                    "mode": { "type": "string", "enum": ["real-input", "direct-call"] }
                  },
                  "required": ["kind"]
                }
                """;
    }

    @Override
    public boolean writeEffect() {
        return true;
    }
}

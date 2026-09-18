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
        return "Acik sayfayi tarar ve etkilesime girilebilen elementleri sade bir liste halinde doner. "
                + "Her elementte ref, tag, role, name, text, testId, elementId, fieldName alanlari ve "
                + "hazir bir target nesnesi bulunur. browser_command icin element.ref degerini, "
                + "scenario_draft_write adimlari icin element.target nesnesini oldugu gibi kullan. "
                + "Liste uzunsa limit dusur veya filter ile daralt.";
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
                    },
                    "filter": {
                      "type": "string",
                      "description": "Etiket, metin, rol, ad veya id uzerinde gecen metin suzgeci"
                    },
                    "limit": {
                      "type": "integer",
                      "minimum": 1,
                      "maximum": 400,
                      "default": 120,
                      "description": "Donecek element sayisi"
                    },
                    "allElements": {
                      "type": "boolean",
                      "default": false,
                      "description": "true ise yalnizca etkilesimli olanlar degil tum elementler doner"
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

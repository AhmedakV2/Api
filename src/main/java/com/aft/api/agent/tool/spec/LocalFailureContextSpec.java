package com.aft.api.agent.tool.spec;

import com.aft.api.agent.tool.ToolNames;
import com.aft.api.agent.tool.ToolSpec;
import org.springframework.stereotype.Component;

@Component
public class LocalFailureContextSpec implements ToolSpec {

    @Override
    public String name() {
        return ToolNames.LOCAL_FAILURE_CONTEXT;
    }

    @Override
    public String description() {
        return "Bir basarisizlik baglam paketini okur: adres, hata mesaji, eslesme durumu, en iyi adaylar ve "
                + "o anki elementlerin ilk 40 tanesi. Ekran goruntusu boyut nedeniyle donmez, "
                + "yalnizca bayt sayisi bildirilir. contextId degerini local_run_detail ciktisindan al.";
    }

    @Override
    public String inputSchema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "contextId": { "type": "string" }
                  },
                  "required": ["contextId"]
                }
                """;
    }
}

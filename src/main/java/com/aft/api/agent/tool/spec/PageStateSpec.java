package com.aft.api.agent.tool.spec;

import com.aft.api.agent.tool.ToolNames;
import com.aft.api.agent.tool.ToolSpec;
import org.springframework.stereotype.Component;

@Component
public class PageStateSpec implements ToolSpec {

    @Override
    public String name() {
        return ToolNames.PAGE_STATE;
    }

    @Override
    public String description() {
        return "Acik sayfanin adresini, basligini ve yuklenme durumunu doner. Tarama yapmaz, hizlidir. "
                + "Hangi sayfadayim turu sorular icin once bunu kullan.";
    }

    @Override
    public String inputSchema() {
        return """
                {
                  "type": "object",
                  "properties": {}
                }
                """;
    }
}

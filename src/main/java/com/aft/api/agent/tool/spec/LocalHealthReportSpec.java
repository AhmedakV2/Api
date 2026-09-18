package com.aft.api.agent.tool.spec;

import com.aft.api.agent.tool.ToolNames;
import com.aft.api.agent.tool.ToolSpec;
import org.springframework.stereotype.Component;

@Component
public class LocalHealthReportSpec implements ToolSpec {

    @Override
    public String name() {
        return ToolNames.LOCAL_HEALTH_REPORT;
    }

    @Override
    public String description() {
        return "Kutuphanenin genel sagligini doner: senaryo ve kosum sayilari, basari orani, "
                + "en kirilgan adimlar. Nasil gidiyoruz turu sorular icin bunu kullan.";
    }

    @Override
    public String inputSchema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "fragileLimit": { "type": "integer", "minimum": 1, "maximum": 100, "default": 10 }
                  }
                }
                """;
    }
}

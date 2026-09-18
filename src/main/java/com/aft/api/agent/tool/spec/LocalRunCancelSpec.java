package com.aft.api.agent.tool.spec;

import com.aft.api.agent.tool.ToolNames;
import com.aft.api.agent.tool.ToolSpec;
import org.springframework.stereotype.Component;

@Component
public class LocalRunCancelSpec implements ToolSpec {

    @Override
    public String name() {
        return ToolNames.LOCAL_RUN_CANCEL;
    }

    @Override
    public String description() {
        return "Suren kosumu durdurur ve kosumun hala calisip calismadigini doner. "
                + "Kullanici onayi gerektirir.";
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

    @Override
    public boolean writeEffect() {
        return true;
    }
}

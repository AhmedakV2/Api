package com.aft.api.agent.tool;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

/** Model bir araci cagirdiginda yurutme bu sinif uzerinden istemciye gider. */
public class RemoteToolCallback implements ToolCallback {

    private final ToolSpec spec;
    private final RemoteToolExecutor executor;
    private final ToolDefinition definition;

    public RemoteToolCallback(ToolSpec spec, RemoteToolExecutor executor) {
        this.spec = spec;
        this.executor = executor;
        this.definition = ToolDefinition.builder()
                .name(spec.name())
                .description(spec.description())
                .inputSchema(spec.inputSchema())
                .build();
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return definition;
    }

    @Override
    public String call(String toolInput) {
        return call(toolInput, null);
    }

    @Override
    public String call(String toolInput, ToolContext toolContext) {
        ToolCallContext context = context(toolContext);
        if (context == null) {
            return "{\"ok\":false,\"error\":\"Arac baglami bulunamadi\"}";
        }
        return executor.invoke(spec, toolInput, context);
    }

    private ToolCallContext context(ToolContext toolContext) {
        if (toolContext == null || toolContext.getContext() == null) {
            return null;
        }
        Object value = toolContext.getContext().get(ToolCallContext.KEY);
        return value instanceof ToolCallContext ctx ? ctx : null;
    }
}

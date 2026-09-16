package com.aft.api.realtime;

import com.aft.api.agent.tool.PendingToolRegistry;
import com.aft.api.agent.tool.ToolResult;
import com.aft.api.state.ToolResultBus;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class ToolResultRelay {
    private static final Logger log = LoggerFactory.getLogger(ToolResultRelay.class);

    private final PendingToolRegistry pendingRegistry;
    private final ToolResultBus bus;
    private final JsonMapper jsonMapper;

    public ToolResultRelay(PendingToolRegistry pendingRegistry, ToolResultBus bus, JsonMapper jsonMapper) {
        this.pendingRegistry = pendingRegistry;
        this.bus = bus;
        this.jsonMapper = jsonMapper;
    }

    @PostConstruct
    void listen() {
        bus.subscribe(this::onMessage);
    }

    public void publish(ToolResult result) {
        if (pendingRegistry.complete(result)) {
            return;
        }
        bus.publish(jsonMapper.writeValueAsString(result));
    }

    void onMessage(String payload) {
        try {
            pendingRegistry.complete(jsonMapper.readValue(payload, ToolResult.class));
        } catch (RuntimeException e) {
            log.warn("Arac sonucu cozulemedi", e);
        }
    }
}

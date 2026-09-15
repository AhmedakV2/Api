package com.aft.api.realtime;

import com.aft.api.agent.tool.PendingToolRegistry;
import com.aft.api.agent.tool.ToolResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class ToolResultRelay implements MessageListener {
    public static final String CHANNEL = "aft:tool:result";

    private static final Logger log = LoggerFactory.getLogger(ToolResultRelay.class);

    private final PendingToolRegistry pendingRegistry;
    private final StringRedisTemplate redis;
    private final JsonMapper jsonMapper;

    public ToolResultRelay(PendingToolRegistry pendingRegistry, StringRedisTemplate redis, JsonMapper jsonMapper) {
        this.pendingRegistry = pendingRegistry;
        this.redis = redis;
        this.jsonMapper = jsonMapper;
    }

    public void publish(ToolResult result) {
        if (pendingRegistry.complete(result)) {
            return;
        }
        redis.convertAndSend(CHANNEL, jsonMapper.writeValueAsString(result));
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            ToolResult result = jsonMapper.readValue(message.getBody(), ToolResult.class);
            pendingRegistry.complete(result);
        } catch (RuntimeException e) {
            log.warn("Arac sonucu cozulemedi", e);
        }
    }
}

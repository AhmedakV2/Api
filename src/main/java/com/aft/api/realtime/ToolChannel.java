package com.aft.api.realtime;

import com.aft.api.agent.tool.ToolInvocation;
import com.aft.api.agent.tool.ToolResult;
import com.aft.api.common.exception.ApiException;
import com.aft.api.common.exception.ErrorCode;
import com.aft.api.config.WebSocketConfig;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class ToolChannel {
    private static final Logger log = LoggerFactory.getLogger(ToolChannel.class);

    private final SimpMessagingTemplate messaging;
    private final DeviceSessionRegistry sessions;
    private final ToolResultRelay relay;

    public ToolChannel(SimpMessagingTemplate messaging, DeviceSessionRegistry sessions, ToolResultRelay relay) {
        this.messaging = messaging;
        this.sessions = sessions;
        this.relay = relay;
    }

    public void send(UUID deviceId, ToolInvocation invocation) {
        if (!sessions.isOnline(deviceId)) {
            throw new ApiException(ErrorCode.TOOL_FAILED, "Istemci bagli degil");
        }
        messaging.convertAndSendToUser(deviceId.toString(), WebSocketConfig.TOOL_QUEUE, invocation);
        log.debug("Arac cagrisi gonderildi deviceId={} tool={} callId={}",
                deviceId, invocation.toolName(), invocation.callId());
    }

    public void deliver(ToolResult result) {
        relay.publish(result);
    }
}

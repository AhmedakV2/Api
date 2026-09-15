package com.aft.api.realtime;

import com.aft.api.agent.tool.ToolResult;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

/** Arac sonucunun normal yolu: WebSocket uzerinden geri bildirim. */
@Controller
public class ToolResultSocketController {

    private final ToolChannel toolChannel;

    public ToolResultSocketController(ToolChannel toolChannel) {
        this.toolChannel = toolChannel;
    }

    @MessageMapping("/tool-results")
    public void onResult(@Payload ToolResult result, DevicePrincipal principal) {
        toolChannel.deliver(result);
    }
}

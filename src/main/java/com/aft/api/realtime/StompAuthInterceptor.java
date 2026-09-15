package com.aft.api.realtime;

import com.aft.api.device.service.DeviceRegistryService;
import com.aft.api.security.service.WsTicketService;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class StompAuthInterceptor implements ChannelInterceptor {
    private static final Logger log = LoggerFactory.getLogger(StompAuthInterceptor.class);
    private static final String TICKET_HEADER = "X-Aft-Ticket";
    private static final String DEVICE_HEADER = "X-Aft-Device";

    private final WsTicketService ticketService;
    private final DeviceRegistryService deviceRegistry;
    private final DeviceSessionRegistry sessionRegistry;

    public StompAuthInterceptor(WsTicketService ticketService,
                                DeviceRegistryService deviceRegistry,
                                DeviceSessionRegistry sessionRegistry) {
        this.ticketService = ticketService;
        this.deviceRegistry = deviceRegistry;
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            DevicePrincipal principal = authenticate(accessor);
            accessor.setUser(principal);
            sessionRegistry.online(principal.deviceId(), accessor.getSessionId());
            log.debug("Arac kanali acildi deviceId={}", principal.deviceId());
            return org.springframework.messaging.support.MessageBuilder
                    .createMessage(message.getPayload(), accessor.getMessageHeaders());
        }

        if (StompCommand.DISCONNECT.equals(command) && accessor.getUser() instanceof DevicePrincipal principal) {
            sessionRegistry.offline(principal.deviceId());
        }
        return message;
    }

    private DevicePrincipal authenticate(StompHeaderAccessor accessor) {
        String ticket = accessor.getFirstNativeHeader(TICKET_HEADER);
        String deviceHeader = accessor.getFirstNativeHeader(DEVICE_HEADER);
        if (ticket == null || deviceHeader == null) {
            throw new IllegalArgumentException("Bilet veya cihaz basligi eksik");
        }

        UUID userId = ticketService.consume(ticket);
        UUID deviceId = UUID.fromString(deviceHeader);
        if (!deviceRegistry.belongsToUser(deviceId, userId)) {
            throw new IllegalArgumentException("Cihaz bu kullaniciya ait degil");
        }
        return new DevicePrincipal(userId, deviceId);
    }
}

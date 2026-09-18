package com.aft.api.realtime;

import com.aft.api.agent.dto.ChatFrame;
import com.aft.api.config.WebSocketConfig;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChatChannel {
    private static final Logger log = LoggerFactory.getLogger(ChatChannel.class);

    private final SimpMessagingTemplate messaging;
    private final DeviceSessionRegistry sessions;
    private final Map<UUID, Turn> running = new ConcurrentHashMap<>();

    public ChatChannel(SimpMessagingTemplate messaging, DeviceSessionRegistry sessions) {
        this.messaging = messaging;
        this.sessions = sessions;
    }

    public boolean isReachable(UUID deviceId) {
        return deviceId != null && sessions.isOnline(deviceId);
    }

    public void begin(UUID sessionId, String turnId, Runnable onCancel) {
        Turn previous = running.put(sessionId, new Turn(turnId, onCancel));
        if (previous != null) {
            previous.cancel();
        }
    }

    public void finish(UUID sessionId, String turnId) {
        running.computeIfPresent(sessionId, (key, turn) -> turn.turnId().equals(turnId) ? null : turn);
    }

    public boolean cancel(UUID sessionId) {
        Turn turn = running.remove(sessionId);
        if (turn == null) {
            return false;
        }
        turn.cancel();
        return true;
    }

    public void send(UUID deviceId, ChatFrame frame) {
        if (!isReachable(deviceId)) {
            log.debug("Sohbet karesi dusuruldu, istemci bagli degil deviceId={}", deviceId);
            return;
        }
        messaging.convertAndSendToUser(deviceId.toString(), WebSocketConfig.CHAT_QUEUE, frame);
    }

    private record Turn(String turnId, Runnable onCancel) {
        void cancel() {
            if (onCancel != null) {
                onCancel.run();
            }
        }
    }
}

package com.aft.api.realtime;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;

@Component
public class SseEmitterRegistry {

    private static final Logger log = LoggerFactory.getLogger(SseEmitterRegistry.class);

    private final Map<UUID, Entry> channels = new ConcurrentHashMap<>();

    public SseEmitter open(UUID sessionId, long timeoutMillis) {
        close(sessionId);
        SseEmitter emitter = new SseEmitter(timeoutMillis);
        Entry entry = new Entry(emitter);
        channels.put(sessionId, entry);

        emitter.onCompletion(() -> channels.remove(sessionId, entry));
        emitter.onTimeout(() -> {
            entry.cancel();
            channels.remove(sessionId, entry);
        });
        emitter.onError(error -> {
            entry.cancel();
            channels.remove(sessionId, entry);
        });
        return emitter;
    }

    public boolean isOpen(UUID sessionId) {
        return channels.containsKey(sessionId);
    }

    public void attach(UUID sessionId, Disposable subscription) {
        Entry entry = channels.get(sessionId);
        if (entry != null) {
            entry.subscription = subscription;
        }
    }

    public void send(UUID sessionId, String event, Object payload) {
        Entry entry = channels.get(sessionId);
        if (entry == null) {
            return;
        }
        try {
            entry.emitter.send(SseEmitter.event().name(event).data(payload));
        } catch (IOException | IllegalStateException e) {
            log.debug("SSE gonderimi basarisiz sessionId={}", sessionId);
            entry.cancel();
            channels.remove(sessionId, entry);
        }
    }

    public void complete(UUID sessionId) {
        Entry entry = channels.remove(sessionId);
        if (entry != null) {
            entry.emitter.complete();
        }
    }

    public void completeWithError(UUID sessionId, Throwable error) {
        Entry entry = channels.remove(sessionId);
        if (entry != null) {
            entry.emitter.completeWithError(error);
        }
    }

    public boolean cancel(UUID sessionId) {
        Entry entry = channels.remove(sessionId);
        if (entry == null) {
            return false;
        }
        entry.cancel();
        entry.emitter.complete();
        return true;
    }

    private void close(UUID sessionId) {
        Entry previous = channels.remove(sessionId);
        if (previous != null) {
            previous.cancel();
            previous.emitter.complete();
        }
    }

    private static final class Entry {

        private final SseEmitter emitter;
        private volatile Disposable subscription;

        private Entry(SseEmitter emitter) {
            this.emitter = emitter;
        }

        private void cancel() {
            Disposable current = subscription;
            if (current != null && !current.isDisposed()) {
                current.dispose();
            }
        }
    }
}

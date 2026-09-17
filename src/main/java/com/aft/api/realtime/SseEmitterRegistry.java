package com.aft.api.realtime;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
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

        if (!entry.write(SseEmitter.event().name("open").data(sessionId.toString()))) {
            drop(sessionId, entry);
        }
        return emitter;
    }

    @Scheduled(fixedDelay = 15_000)
    void keepAlive() {
        channels.forEach((sessionId, entry) -> {
            if (entry.write(SseEmitter.event().comment("ping"))) {
                return;
            }
            log.debug("SSE canli tutma basarisiz sessionId={}", sessionId);
            drop(sessionId, entry);
        });
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
        if (entry.write(SseEmitter.event().name(event).data(payload))) {
            return;
        }
        log.debug("SSE gonderimi basarisiz sessionId={}", sessionId);
        drop(sessionId, entry);
    }

    public void complete(UUID sessionId) {
        Entry entry = channels.remove(sessionId);
        if (entry != null) {
            entry.finish();
        }
    }

    public void fail(UUID sessionId, String message) {
        Entry entry = channels.remove(sessionId);
        if (entry == null) {
            return;
        }
        entry.cancel();
        entry.write(SseEmitter.event().name("error").data(message));
        entry.finish();
    }

    public boolean cancel(UUID sessionId) {
        Entry entry = channels.remove(sessionId);
        if (entry == null) {
            return false;
        }
        entry.cancel();
        entry.finish();
        return true;
    }

    private void drop(UUID sessionId, Entry entry) {
        channels.remove(sessionId, entry);
        entry.cancel();
        entry.finish();
    }

    private void close(UUID sessionId) {
        Entry previous = channels.remove(sessionId);
        if (previous != null) {
            previous.cancel();
            previous.finish();
        }
    }

    private static final class Entry {
        private final SseEmitter emitter;
        private final Object lock = new Object();
        private volatile Disposable subscription;
        private boolean closed;

        private Entry(SseEmitter emitter) {
            this.emitter = emitter;
        }

        private boolean write(SseEmitter.SseEventBuilder event) {
            synchronized (lock) {
                if (closed) {
                    return false;
                }
                try {
                    emitter.send(event);
                    return true;
                } catch (IOException | IllegalStateException e) {
                    closed = true;
                    return false;
                }
            }
        }

        private void finish() {
            synchronized (lock) {
                if (closed) {
                    return;
                }
                closed = true;
                emitter.complete();
            }
        }

        private void cancel() {
            Disposable current = subscription;
            if (current != null && !current.isDisposed()) {
                current.dispose();
            }
        }
    }
}

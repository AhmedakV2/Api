package com.aft.api.state.coherence;

import com.aft.api.state.ToolResultBus;
import com.tangosol.net.Session;
import com.tangosol.net.topic.NamedTopic;
import com.tangosol.net.topic.Publisher;
import com.tangosol.net.topic.Subscriber;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoherenceToolResultBus implements ToolResultBus {
    private static final Logger log = LoggerFactory.getLogger(CoherenceToolResultBus.class);

    private final NamedTopic<String> topic;
    private final Publisher<String> publisher;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private volatile Subscriber<String> subscriber;

    public CoherenceToolResultBus(Session session, String topicName) {
        this.topic = session.getTopic(topicName);
        this.publisher = topic.createPublisher();
    }

    @Override
    public void publish(String payload) {
        publisher.publish(payload);
    }

    @Override
    public void subscribe(Consumer<String> handler) {
        subscriber = topic.createSubscriber();
        receiveNext(handler);
    }

    private void receiveNext(Consumer<String> handler) {
        if (!running.get()) {
            return;
        }
        subscriber.receive().handle((element, error) -> {
            if (error != null) {
                if (running.get()) {
                    log.warn("Arac sonucu kanali okunamadi", error);
                }
                return null;
            }
            try {
                handler.accept(element.getValue());
            } catch (RuntimeException e) {
                log.warn("Arac sonucu islenemedi", e);
            }
            receiveNext(handler);
            return null;
        });
    }

    @PreDestroy
    public void close() {
        running.set(false);
        Subscriber<String> current = subscriber;
        if (current != null) {
            current.close();
        }
        publisher.close();
    }
}

package com.aft.api.state.local;

import com.aft.api.state.ToolResultBus;
import java.util.function.Consumer;

public class LocalToolResultBus implements ToolResultBus {
    private volatile Consumer<String> handler;

    @Override
    public void publish(String payload) {
        Consumer<String> current = handler;
        if (current != null) {
            current.accept(payload);
        }
    }

    @Override
    public void subscribe(Consumer<String> consumer) {
        this.handler = consumer;
    }
}

package com.aft.api.state;

import java.util.function.Consumer;

public interface ToolResultBus {
    void publish(String payload);

    void subscribe(Consumer<String> handler);
}

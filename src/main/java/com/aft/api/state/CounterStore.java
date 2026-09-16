package com.aft.api.state;

import java.time.Duration;

public interface CounterStore {
    Window increment(String key, Duration window);

    void reset(String key, Duration window);

    record Window(long count, Duration remaining) {
    }
}

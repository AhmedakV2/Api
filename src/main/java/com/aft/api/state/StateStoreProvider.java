package com.aft.api.state;

public interface StateStoreProvider {
    KeyValueStore keyValue(String namespace);

    CounterStore counter(String namespace);
}

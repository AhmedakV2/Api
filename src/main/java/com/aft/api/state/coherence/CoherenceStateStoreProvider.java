package com.aft.api.state.coherence;

import com.aft.api.state.CounterStore;
import com.aft.api.state.KeyValueStore;
import com.aft.api.state.StateStoreProvider;
import com.tangosol.net.Session;

public class CoherenceStateStoreProvider implements StateStoreProvider {
    private final Session session;
    private final String prefix;

    public CoherenceStateStoreProvider(Session session, String prefix) {
        this.session = session;
        this.prefix = prefix;
    }

    @Override
    public KeyValueStore keyValue(String namespace) {
        return new CoherenceKeyValueStore(session.getCache(prefix + namespace));
    }

    @Override
    public CounterStore counter(String namespace) {
        return new CoherenceCounterStore(session.getCache(prefix + namespace));
    }
}

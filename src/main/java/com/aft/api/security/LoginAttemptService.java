package com.aft.api.security;

import com.aft.api.config.SecurityProperties;
import com.aft.api.state.CounterStore;
import com.aft.api.state.KeyValueStore;
import com.aft.api.state.StateNamespaces;
import com.aft.api.state.StateStoreProvider;
import java.time.Duration;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {
    private final CounterStore failures;
    private final KeyValueStore locks;
    private final SecurityProperties properties;

    public LoginAttemptService(StateStoreProvider stateStores, SecurityProperties properties) {
        this.failures = stateStores.counter(StateNamespaces.LOGIN_FAIL);
        this.locks = stateStores.keyValue(StateNamespaces.LOGIN_LOCK);
        this.properties = properties;
    }

    public boolean isLocked(String email) {
        return remainingLock(email).compareTo(Duration.ZERO) > 0;
    }

    public void recordFailure(String email) {
        String key = key(email);
        Duration duration = properties.lockout().duration();
        CounterStore.Window window = failures.increment(key, duration);
        if (window.count() >= properties.lockout().maxAttempts()) {
            long until = System.currentTimeMillis() + duration.toMillis();
            locks.put(key, Long.toString(until), duration);
        }
    }

    public void reset(String email) {
        String key = key(email);
        failures.reset(key, properties.lockout().duration());
        locks.delete(key);
    }

    public Duration remainingLock(String email) {
        return locks.get(key(email))
                .map(value -> Duration.ofMillis(Long.parseLong(value) - System.currentTimeMillis()))
                .filter(remaining -> remaining.compareTo(Duration.ZERO) > 0)
                .orElse(Duration.ZERO);
    }

    private String key(String email) {
        return email.toLowerCase(Locale.ROOT);
    }
}

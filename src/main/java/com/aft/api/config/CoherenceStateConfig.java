package com.aft.api.config;

import com.aft.api.state.StateStoreProvider;
import com.aft.api.state.ToolResultBus;
import com.aft.api.state.coherence.CoherenceStateStoreProvider;
import com.aft.api.state.coherence.CoherenceToolResultBus;
import com.tangosol.net.Coherence;
import com.tangosol.net.Session;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "aft.state.provider", havingValue = "coherence")
public class CoherenceStateConfig {

    @Bean(destroyMethod = "close")
    Coherence coherence(StateProperties properties) throws Exception {
        StateProperties.Coherence settings = properties.coherence();
        applyIfPresent("coherence.client", settings.client());
        applyIfPresent("coherence.cacheconfig", settings.cacheConfig());
        applyIfPresent("coherence.cluster", settings.clusterName());
        applyIfPresent("coherence.extend.address", settings.proxyAddress());
        applyIfPresent("coherence.extend.port",
                settings.proxyPort() == null ? null : String.valueOf(settings.proxyPort()));

        Coherence coherence = "remote".equals(settings.client())
                ? Coherence.client()
                : Coherence.clusterMember();
        coherence.start().get(120, TimeUnit.SECONDS);
        return coherence;
    }

    @Bean
    Session coherenceSession(Coherence coherence, StateProperties properties) {
        String name = properties.coherence().sessionName();
        return (name == null || name.isBlank())
                ? coherence.getSession()
                : coherence.getSession(name);
    }

    @Bean
    StateStoreProvider stateStoreProvider(Session session, StateProperties properties) {
        return new CoherenceStateStoreProvider(session, properties.coherence().cachePrefix());
    }

    @Bean
    ToolResultBus toolResultBus(Session session, StateProperties properties) {
        return new CoherenceToolResultBus(session, properties.coherence().topicName());
    }

    private void applyIfPresent(String key, String value) {
        if (value != null && !value.isBlank() && System.getProperty(key) == null) {
            System.setProperty(key, value);
        }
    }
}

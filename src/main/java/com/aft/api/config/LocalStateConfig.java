package com.aft.api.config;

import com.aft.api.state.StateStoreProvider;
import com.aft.api.state.ToolResultBus;
import com.aft.api.state.local.LocalStateStoreProvider;
import com.aft.api.state.local.LocalToolResultBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LocalStateConfig {

    @Bean(destroyMethod = "close")
    StateStoreProvider stateStoreProvider() {
        return new LocalStateStoreProvider();
    }

    @Bean
    ToolResultBus toolResultBus() {
        return new LocalToolResultBus();
    }
}

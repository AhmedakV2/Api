package com.aft.api.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "aft.state")
public record StateProperties(@NotBlank String provider, Coherence coherence, Valkey valkey) {

    public record Coherence(String cachePrefix,
                            String topicName,
                            String sessionName,
                            String client,
                            String proxyAddress,
                            Integer proxyPort,
                            String clusterName,
                            String cacheConfig) {
    }

    public record Valkey(String host,
                         Integer port,
                         String password,
                         Integer database,
                         String keyPrefix,
                         String toolResultChannel) {
        public Valkey {
            host = (host == null || host.isBlank()) ? "127.0.0.1" : host;
            port = port == null ? 6379 : port;
            database = database == null ? 0 : database;
            keyPrefix = (keyPrefix == null || keyPrefix.isBlank()) ? "aft:" : keyPrefix;
            toolResultChannel = (toolResultChannel == null || toolResultChannel.isBlank())
                    ? "aft:tool:result"
                    : toolResultChannel;
        }
    }
}

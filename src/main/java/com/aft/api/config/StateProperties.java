package com.aft.api.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "aft.state")
public record StateProperties(@NotBlank String provider, Coherence coherence) {

    public record Coherence(@NotBlank String cachePrefix,
                            @NotBlank String topicName,
                            String sessionName,
                            String client,
                            String proxyAddress,
                            Integer proxyPort,
                            String clusterName,
                            String cacheConfig) {
    }
}

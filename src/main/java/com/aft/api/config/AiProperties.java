package com.aft.api.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aft.ai")
public record AiProperties(String provider,
                           Models models,
                           Duration requestTimeout,
                           int maxWindowMessages,
                           int maxWindowTokens) {

    public AiProperties {
        provider = (provider == null || provider.isBlank()) ? "ollama" : provider.toLowerCase();
        models = models == null ? new Models(null, null, null) : models;
        requestTimeout = requestTimeout == null ? Duration.ofSeconds(120) : requestTimeout;
        maxWindowMessages = maxWindowMessages <= 0 ? 40 : maxWindowMessages;
        maxWindowTokens = maxWindowTokens <= 0 ? 24000 : maxWindowTokens;
    }

    public record Models(String planner, String fast, String embedding) {

        public Models {
            planner = (planner == null || planner.isBlank()) ? "llama3.1:8b" : planner;
            fast = (fast == null || fast.isBlank()) ? planner : fast;
        }
    }
}

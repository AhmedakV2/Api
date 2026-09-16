package com.aft.api.config;

import java.util.Locale;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aft.ai")
public record AiProperties(String provider,
                           Models models,
                           Duration requestTimeout,
                           int maxWindowMessages,
                           int maxWindowTokens,
                           Duration toolTimeout,
                           int maxToolHops,
                           int maxToolResultBytes) {
    public AiProperties {
        provider = (provider == null || provider.isBlank()) ? "ollama" : provider.toLowerCase(Locale.ROOT);
        models = models == null ? new Models(null, null, null) : models;
        requestTimeout = requestTimeout == null ? Duration.ofSeconds(120) : requestTimeout;
        maxWindowMessages = maxWindowMessages <= 0 ? 40 : maxWindowMessages;
        maxWindowTokens = maxWindowTokens <= 0 ? 24000 : maxWindowTokens;
        toolTimeout = toolTimeout == null ? Duration.ofSeconds(30) : toolTimeout;
        maxToolHops = maxToolHops <= 0 ? 12 : maxToolHops;
        maxToolResultBytes = maxToolResultBytes <= 0 ? 262144 : maxToolResultBytes;
    }

    public record Models(String planner, String fast, String embedding) {
        public Models {
            planner = (planner == null || planner.isBlank()) ? "llama3.1:8b" : planner;
            fast = (fast == null || fast.isBlank()) ? planner : fast;
        }
    }
}

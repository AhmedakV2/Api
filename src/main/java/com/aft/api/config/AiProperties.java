package com.aft.api.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aft.ai")
public record AiProperties(Models models,
                           String defaultTier,
                           Duration requestTimeout,
                           int maxWindowMessages,
                           int maxWindowTokens,
                           Duration toolTimeout,
                           int maxToolHops,
                           int maxToolResultBytes) {
    public AiProperties {
        models = models == null ? new Models(null, null, null, null) : models;
        defaultTier = (defaultTier == null || defaultTier.isBlank()) ? "FAST" : defaultTier;
        requestTimeout = requestTimeout == null ? Duration.ofSeconds(300) : requestTimeout;
        maxWindowMessages = maxWindowMessages <= 0 ? 40 : maxWindowMessages;
        maxWindowTokens = maxWindowTokens <= 0 ? 24000 : maxWindowTokens;
        toolTimeout = toolTimeout == null ? Duration.ofSeconds(30) : toolTimeout;
        maxToolHops = maxToolHops <= 0 ? 12 : maxToolHops;
        maxToolResultBytes = maxToolResultBytes <= 0 ? 262144 : maxToolResultBytes;
    }

    public record Models(String fast, String slow, String ultra, String embedding) {
        public Models {
            fast = (fast == null || fast.isBlank()) ? "Akgun-Q3.6" : fast;
            slow = (slow == null || slow.isBlank()) ? fast : slow;
            ultra = (ultra == null || ultra.isBlank()) ? slow : ultra;
        }
    }
}

package com.aft.api.agent.provider;

import java.util.Locale;

public enum ModelTier {
    FAST("Hizli"),
    SLOW("Yavas"),
    ULTRA("Ultra");

    private final String label;

    ModelTier(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static ModelTier parse(String raw, ModelTier fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}

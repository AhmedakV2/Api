package com.aft.api.agent.guard;

import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class PromptSanitizer {
    private static final List<Pattern> INJECTION_PATTERNS = List.of(
            Pattern.compile("(?i)\\b(ignore|disregard|forget)\\s+(all\\s+)?(previous|prior|above)\\s+"
                    + "(instructions?|prompts?|rules?)"),
            Pattern.compile("(?i)\\b(onceki|yukaridaki|tum)\\s+(talimatlari|kurallari|komutlari)\\s+"
                    + "(yok\\s*say|unut|gormezden\\s*gel)"),
            Pattern.compile("(?i)^\\s*(system|assistant|developer)\\s*:", Pattern.MULTILINE),
            Pattern.compile("(?i)<\\s*/?\\s*(system|instructions?|prompt)\\s*>"),
            Pattern.compile("(?i)\\byou\\s+are\\s+now\\b"),
            Pattern.compile("(?i)\\bartik\\s+sen\\b"));

    private static final String MASK = "[temizlendi]";

    public String sanitize(String raw) {
        if (raw == null || raw.isBlank()) {
            return raw;
        }
        String result = raw;
        for (Pattern pattern : INJECTION_PATTERNS) {
            result = pattern.matcher(result).replaceAll(MASK);
        }
        return result;
    }

    public Trimmed trim(String content, int maxBytes) {
        if (content == null) {
            return new Trimmed("", false);
        }
        byte[] bytes = content.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        if (bytes.length <= maxBytes) {
            return new Trimmed(content, false);
        }
        String cut = new String(bytes, 0, maxBytes, java.nio.charset.StandardCharsets.UTF_8);
        return new Trimmed(cut, true);
    }

    public record Trimmed(String content, boolean truncated) {
    }
}

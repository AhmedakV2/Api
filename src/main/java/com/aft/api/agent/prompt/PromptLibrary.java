package com.aft.api.agent.prompt;

import com.aft.api.agent.entity.SessionMode;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class PromptLibrary {

    private final Map<SystemPrompts, String> templates = new EnumMap<>(SystemPrompts.class);

    public PromptLibrary() {
        for (SystemPrompts prompt : SystemPrompts.values()) {
            templates.put(prompt, read(prompt.location()));
        }
    }

    public String system(SystemPrompts prompt, SessionMode mode) {
        return templates.get(prompt).replace("{mode}", mode.name());
    }

    private String read(String location) {
        try (var stream = new ClassPathResource(location).getInputStream()) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Istem sablonu okunamadi: " + location, e);
        }
    }

}

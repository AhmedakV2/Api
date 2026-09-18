package com.aft.api.agent.prompt;

import com.aft.api.agent.entity.SessionMode;
import com.aft.api.agent.tool.ToolSpec;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class PromptLibrary {
    private static final String NO_TOOLS = """
            - (Bu tur icin arac sunulmadi.)
            - Soruyu dogrudan metinle cevapla, arac cagirmayi deneme. Kullanici bir islem
              istiyorsa ne yapmak istedigini tek cumleyle sor.""";

    private final Map<SystemPrompts, String> templates = new EnumMap<>(SystemPrompts.class);

    public PromptLibrary() {
        for (SystemPrompts prompt : SystemPrompts.values()) {
            templates.put(prompt, read(prompt.location()));
        }
    }

    public String system(SystemPrompts prompt, SessionMode mode, List<ToolSpec> tools) {
        return templates.get(prompt)
                .replace("{mode}", mode.name())
                .replace("{tools}", catalog(tools));
    }

    private String catalog(List<ToolSpec> tools) {
        if (tools == null || tools.isEmpty()) {
            return NO_TOOLS;
        }
        return tools.stream().map(PromptLibrary::line).collect(Collectors.joining("\n"));
    }

    private static String line(ToolSpec tool) {
        String suffix = tool.writeEffect() ? " [yazma etkisi, kullanici onayi ister]" : "";
        return "- " + tool.name() + suffix + ": " + tool.description();
    }

    private String read(String location) {
        try (var stream = new ClassPathResource(location).getInputStream()) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Istem sablonu okunamadi: " + location, e);
        }
    }
}

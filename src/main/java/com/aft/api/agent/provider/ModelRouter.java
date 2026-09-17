package com.aft.api.agent.provider;

import java.util.Locale;
import com.aft.api.common.exception.ApiException;
import com.aft.api.common.exception.ErrorCode;
import com.aft.api.config.AiProperties;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ModelRouter {
    private static final Logger log = LoggerFactory.getLogger(ModelRouter.class);

    private final Map<ProviderName, ModelProvider> providers = new EnumMap<>(ProviderName.class);
    private final AiProperties properties;

    public ModelRouter(List<ModelProvider> available, AiProperties properties) {
        available.forEach(provider -> providers.put(provider.name(), provider));
        this.properties = properties;
        log.info("Etkin model saglayicilari: {}", describeActive());
    }

    public ModelProvider provider() {
        ProviderName configured = parse(properties.provider());
        ModelProvider provider = providers.get(configured);
        if (provider == null) {
            throw new ApiException(ErrorCode.AI_PROVIDER_ERROR,
                    "Yapilandirilan saglayici etkin degil: " + properties.provider()
                            + " (etkin olanlar: " + describeActive() + ")");
        }
        return provider;
    }

    public String modelFor(TaskKind kind) {
        return kind == TaskKind.PLANNING ? properties.models().planner() : properties.models().fast();
    }

    public Set<ProviderName> activeProviders() {
        return providers.keySet();
    }

    public List<String> availableModels() {
        return List.of(properties.models().planner(), properties.models().fast()).stream().distinct().toList();
    }

    private String describeActive() {
        return providers.isEmpty() ? "yok"
                : providers.keySet().stream().map(ProviderName::name).collect(Collectors.joining(", "));
    }

    private ProviderName parse(String raw) {
        try {
            return ProviderName.valueOf(raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.AI_PROVIDER_ERROR, "Bilinmeyen saglayici: " + raw);
        }
    }
}

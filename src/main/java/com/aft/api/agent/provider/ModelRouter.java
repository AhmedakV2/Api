package com.aft.api.agent.provider;

import com.aft.api.common.exception.ApiException;
import com.aft.api.common.exception.ErrorCode;
import com.aft.api.config.AiProperties;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ModelRouter {
    private final Map<ProviderName, ModelProvider> providers = new EnumMap<>(ProviderName.class);
    private final AiProperties properties;

    public ModelRouter(List<ModelProvider> available, AiProperties properties) {
        available.forEach(provider -> providers.put(provider.name(), provider));
        this.properties = properties;
    }

    public ModelProvider provider() {
        ProviderName configured = parse(properties.provider());
        ModelProvider provider = providers.get(configured);
        if (provider == null) {
            throw new ApiException(ErrorCode.AI_PROVIDER_ERROR,
                    "Yapilandirilan saglayici etkin degil: " + properties.provider());
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

    private ProviderName parse(String raw) {
        try {
            return ProviderName.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.AI_PROVIDER_ERROR, "Bilinmeyen saglayici: " + raw);
        }
    }
}

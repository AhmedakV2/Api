package com.aft.api.agent.provider;

import com.aft.api.agent.dto.ModelTierDto;
import com.aft.api.common.exception.ApiException;
import com.aft.api.common.exception.ErrorCode;
import com.aft.api.config.AiProperties;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class ModelRouter {
    private static final Logger log = LoggerFactory.getLogger(ModelRouter.class);

    private final ObjectProvider<OllmProvider> ollm;
    private final AiProperties properties;

    public ModelRouter(ObjectProvider<OllmProvider> ollm, AiProperties properties) {
        this.ollm = ollm;
        this.properties = properties;
        log.info("Ollm profilleri: {}", describeTiers());
    }

    public OllmProvider provider() {
        OllmProvider provider = ollm.getIfAvailable();
        if (provider == null) {
            throw new ApiException(ErrorCode.AI_PROVIDER_ERROR,
                    "Ollm sohbet modeli yapilandirilmadi, OLLM_BASE_URL ve OLLM_API_KEY degerlerini kontrol edin");
        }
        return provider;
    }

    public boolean isReady() {
        return ollm.getIfAvailable() != null;
    }

    public String modelFor(ModelTier tier) {
        AiProperties.Models models = properties.models();
        return switch (tier) {
            case FAST -> models.fast();
            case SLOW -> models.slow();
            case ULTRA -> models.ultra();
        };
    }

    public ModelTier defaultTier() {
        return ModelTier.parse(properties.defaultTier(), ModelTier.FAST);
    }

    public ModelTier tierOfModel(String model) {
        return Arrays.stream(ModelTier.values())
                .filter(tier -> modelFor(tier).equals(model))
                .findFirst()
                .orElse(defaultTier());
    }

    public List<ModelTierDto> tiers() {
        return Arrays.stream(ModelTier.values())
                .map(tier -> new ModelTierDto(tier.name(), tier.label(), modelFor(tier)))
                .toList();
    }

    private String describeTiers() {
        return String.join(", ", Arrays.stream(ModelTier.values())
                .map(tier -> tier.name() + "=" + modelFor(tier)).toList());
    }
}

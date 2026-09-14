package com.aft.api.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aft.api.agent.provider.ModelRouter;
import com.aft.api.agent.provider.ProviderName;
import com.aft.api.agent.provider.TaskKind;
import com.aft.api.common.exception.ApiException;
import com.aft.api.config.AiProperties;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class ModelRouterTest {

    private AiProperties properties(String provider, String planner, String fast) {
        return new AiProperties(provider, new AiProperties.Models(planner, fast, null),
                Duration.ofSeconds(30), 40, 24000);
    }

    @Test
    void planlamaVeHizliGorevFarkliModelSecer() {
        ModelRouter router = new ModelRouter(List.of(new StubModelProvider(List.of())),
                properties("ollama", "buyuk-model", "kucuk-model"));

        assertThat(router.modelFor(TaskKind.PLANNING)).isEqualTo("buyuk-model");
        assertThat(router.modelFor(TaskKind.FAST)).isEqualTo("kucuk-model");
    }

    @Test
    void hizliModelVerilmezsePlanlamaModeliKullanilir() {
        ModelRouter router = new ModelRouter(List.of(new StubModelProvider(List.of())),
                properties("ollama", "tek-model", null));

        assertThat(router.modelFor(TaskKind.FAST)).isEqualTo("tek-model");
        assertThat(router.availableModels()).containsExactly("tek-model");
    }

    @Test
    void yapilandirilanSaglayiciDoner() {
        ModelRouter router = new ModelRouter(List.of(new StubModelProvider(List.of())),
                properties("ollama", "model", "model"));

        assertThat(router.provider().name()).isEqualTo(ProviderName.OLLAMA);
        assertThat(router.activeProviders()).containsExactly(ProviderName.OLLAMA);
    }

    @Test
    void etkinOlmayanSaglayiciHataVerir() {
        ModelRouter router = new ModelRouter(List.of(new StubModelProvider(List.of())),
                properties("anthropic", "model", "model"));

        assertThatThrownBy(router::provider)
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("etkin degil");
    }

    @Test
    void bilinmeyenSaglayiciAdiHataVerir() {
        ModelRouter router = new ModelRouter(List.of(new StubModelProvider(List.of())),
                properties("bilinmeyen", "model", "model"));

        assertThatThrownBy(router::provider)
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Bilinmeyen saglayici");
    }
}

package com.aft.api.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aft.api.agent.provider.ModelRouter;
import com.aft.api.agent.provider.ModelTier;
import com.aft.api.agent.provider.OllmProvider;
import com.aft.api.common.exception.ApiException;
import com.aft.api.config.AiProperties;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

class ModelRouterTest {

    private AiProperties properties(String tier, String fast, String slow, String ultra) {
        return new AiProperties(new AiProperties.Models(fast, slow, ultra, null), tier,
                Duration.ofSeconds(300), 40, 24000, Duration.ofSeconds(30), 12, 262144);
    }

    private ModelRouter router(AiProperties properties, OllmProvider provider) {
        return new ModelRouter(new ObjectProvider<>() {
            @Override
            public OllmProvider getObject() {
                return provider;
            }

            @Override
            public OllmProvider getIfAvailable() {
                return provider;
            }
        }, properties);
    }

    @Test
    void ucProfilAyriModelSecer() {
        ModelRouter router = router(properties("FAST", "hizli-m", "yavas-m", "ultra-m"),
                new StubOllmProvider(List.of()));

        assertThat(router.modelFor(ModelTier.FAST)).isEqualTo("hizli-m");
        assertThat(router.modelFor(ModelTier.SLOW)).isEqualTo("yavas-m");
        assertThat(router.modelFor(ModelTier.ULTRA)).isEqualTo("ultra-m");
    }

    @Test
    void ustProfillerVerilmezseBirAlttakineDuser() {
        ModelRouter router = router(properties("FAST", "tek-model", null, null),
                new StubOllmProvider(List.of()));

        assertThat(router.modelFor(ModelTier.SLOW)).isEqualTo("tek-model");
        assertThat(router.modelFor(ModelTier.ULTRA)).isEqualTo("tek-model");
    }

    @Test
    void katalogUcProfiliEtiketleriyleDoner() {
        ModelRouter router = router(properties("SLOW", "hizli-m", "yavas-m", "ultra-m"),
                new StubOllmProvider(List.of()));

        assertThat(router.tiers()).hasSize(3);
        assertThat(router.tiers()).extracting("tier").containsExactly("FAST", "SLOW", "ULTRA");
        assertThat(router.tiers()).extracting("label").containsExactly("Hizli", "Yavas", "Ultra");
        assertThat(router.defaultTier()).isEqualTo(ModelTier.SLOW);
    }

    @Test
    void bilinmeyenVarsayilanProfilHizliyaDuser() {
        ModelRouter router = router(properties("bilinmeyen", "hizli-m", "yavas-m", "ultra-m"),
                new StubOllmProvider(List.of()));

        assertThat(router.defaultTier()).isEqualTo(ModelTier.FAST);
    }

    @Test
    void modelAdindanProfilBulunur() {
        ModelRouter router = router(properties("FAST", "hizli-m", "yavas-m", "ultra-m"),
                new StubOllmProvider(List.of()));

        assertThat(router.tierOfModel("ultra-m")).isEqualTo(ModelTier.ULTRA);
        assertThat(router.tierOfModel("bilinmeyen")).isEqualTo(ModelTier.FAST);
    }

    @Test
    void modelYapilandirilmamissaAnlasilirHataVerir() {
        ModelRouter router = router(properties("FAST", "m", "m", "m"), null);

        assertThat(router.isReady()).isFalse();
        assertThatThrownBy(router::provider)
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("OLLM_BASE_URL");
    }
}

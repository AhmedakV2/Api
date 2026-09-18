package com.aft.api.agent;

import static org.assertj.core.api.Assertions.assertThat;

import com.aft.api.agent.routing.IntentRouter;
import com.aft.api.agent.routing.ToolIntent;
import com.aft.api.agent.tool.ToolNames;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class IntentRouterTest {
    private final IntentRouter router = new IntentRouter();

    @ParameterizedTest
    @ValueSource(strings = {
            "merhaba",
            "Selam, nasılsın?",
            "Teşekkürler",
            "Sen kimsin",
            "Neler yapabilirsin?",
            "Regresyon testi nedir",
            "İyi günler"
    })
    void sohbetIstekleriIcinAracSunulmaz(String text) {
        ToolIntent intent = router.route(text, false);

        assertThat(intent).isEqualTo(ToolIntent.CHAT);
        assertThat(router.offersTools(intent)).isFalse();
        assertThat(router.toolsFor(intent)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Bu sayfayı tara",
            "Giriş butonuna tıkla",
            "Formdaki alanları listele"
    })
    void tarayiciIstekleriTarayiciAraclariniAcar(String text) {
        ToolIntent intent = router.route(text, false);

        assertThat(intent).isEqualTo(ToolIntent.BROWSER);
        assertThat(router.toolsFor(intent)).contains(ToolNames.PAGE_SNAPSHOT, ToolNames.BROWSER_COMMAND);
    }

    @Test
    void senaryoIstegiSenaryoAraclariniAcar() {
        ToolIntent intent = router.route("Giriş senaryosunun adımlarını göster", false);

        assertThat(intent).isEqualTo(ToolIntent.SCENARIO);
        assertThat(router.toolsFor(intent)).contains(ToolNames.LOCAL_SCENARIO_READ,
                ToolNames.LOCAL_SCENARIO_LIST);
    }

    @Test
    void kosumIstegiKosumAraclariniAcar() {
        ToolIntent intent = router.route("Son koşumun sonucunu ver", false);

        assertThat(intent).isEqualTo(ToolIntent.RUN);
        assertThat(router.toolsFor(intent)).contains(ToolNames.LOCAL_RUN_DETAIL,
                ToolNames.LOCAL_RUN_HISTORY);
    }

    @Test
    void saglikSorusuTanilamaAraclariniAcar() {
        ToolIntent intent = router.route("Kırılgan adımların sağlık özetini çıkar", false);

        assertThat(intent).isEqualTo(ToolIntent.DIAGNOSTIC);
        assertThat(router.toolsFor(intent)).contains(ToolNames.LOCAL_HEALTH_REPORT);
    }

    @Test
    void belirsizIstekTumAraclariAcar() {
        ToolIntent intent = router.route("Senaryoyu tarayıcıda koş ve sağlık raporunu ver", false);

        assertThat(intent).isEqualTo(ToolIntent.ANY);
        assertThat(router.offersTools(intent)).isTrue();
        assertThat(router.toolsFor(intent)).isEmpty();
    }

    @Test
    void gecmisVarkenKisaOnayEylemSayilir() {
        assertThat(router.route("evet", true)).isEqualTo(ToolIntent.ANY);
        assertThat(router.route("tamam devam et", true)).isEqualTo(ToolIntent.ANY);
    }

    @Test
    void gecmisYokkenKisaOnaySohbettir() {
        assertThat(router.route("evet", false)).isEqualTo(ToolIntent.CHAT);
    }

    @Test
    void daraltilmisKumeKesifAraclariniHepIcerir() {
        assertThat(router.toolsFor(ToolIntent.RUN)).contains(ToolNames.PAGE_STATE,
                ToolNames.LOCAL_SCENARIO_LIST, ToolNames.LOCAL_SCENARIO_SEARCH);
    }

    @Test
    void bosIstekSohbettir() {
        assertThat(router.route("", false)).isEqualTo(ToolIntent.CHAT);
        assertThat(router.route(null, false)).isEqualTo(ToolIntent.CHAT);
    }
}

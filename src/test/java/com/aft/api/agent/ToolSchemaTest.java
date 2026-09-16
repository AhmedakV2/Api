package com.aft.api.agent;

import static org.assertj.core.api.Assertions.assertThat;

import com.aft.api.agent.tool.ToolNames;
import com.aft.api.agent.tool.ToolSpec;
import com.aft.api.agent.tool.spec.BrowserCommandSpec;
import com.aft.api.agent.tool.spec.LocalDescriptorSearchSpec;
import com.aft.api.agent.tool.spec.LocalFailureContextSpec;
import com.aft.api.agent.tool.spec.LocalRunHistorySpec;
import com.aft.api.agent.tool.spec.LocalScenarioReadSpec;
import com.aft.api.agent.tool.spec.LocalScenarioSearchSpec;
import com.aft.api.agent.tool.spec.PageSnapshotSpec;
import com.aft.api.agent.tool.spec.ScenarioDraftWriteSpec;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class ToolSchemaTest {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private final List<ToolSpec> specs = List.of(
            new LocalScenarioSearchSpec(), new LocalScenarioReadSpec(), new LocalRunHistorySpec(),
            new LocalDescriptorSearchSpec(), new LocalFailureContextSpec(), new PageSnapshotSpec(),
            new BrowserCommandSpec(), new ScenarioDraftWriteSpec());

    private JsonNode schema(ToolSpec spec) {
        return MAPPER.readTree(spec.inputSchema());
    }

    @Test
    void tumSemalarGecerliJsondur() {
        specs.forEach(spec -> assertThat(schema(spec).path("type").asString()).isEqualTo("object"));
    }

    @Test
    void tumAraclarinAdiVeAciklamasiVardir() {
        specs.forEach(spec -> {
            assertThat(spec.name()).isNotBlank();
            assertThat(spec.description()).isNotBlank();
        });
    }

    @Test
    void sekizAracTanimlidir() {
        assertThat(specs).hasSize(8);
        assertThat(specs).extracting(ToolSpec::name).doesNotHaveDuplicates();
    }

    @Test
    void kosumGecmisiIstemcininDestekledigiAlanlariKullanir() {
        JsonNode properties = schema(new LocalRunHistorySpec()).path("properties");

        assertThat(properties.has("scenarioId")).isTrue();
        assertThat(properties.has("status")).isTrue();
        assertThat(properties.has("limit")).isTrue();
        assertThat(properties.has("offset")).isTrue();
        assertThat(properties.has("from")).isFalse();
        assertThat(properties.has("to")).isFalse();
    }

    @Test
    void taramaSeviyesiSayisaldir() {
        JsonNode level = schema(new PageSnapshotSpec()).path("properties").path("level");

        assertThat(level.path("type").asString()).isEqualTo("integer");
        assertThat(level.path("enum")).hasSize(4);
    }

    @Test
    void tarayiciKomutuGercekEylemTurleriniTasir() {
        JsonNode kind = schema(new BrowserCommandSpec()).path("properties").path("kind");
        List<String> kinds = kind.path("enum").valueStream().map(JsonNode::asString).toList();

        assertThat(kinds).hasSize(13);
        assertThat(kinds).contains("click", "type", "clear-type", "press-key", "select-option",
                "upload", "navigate", "wait", "refresh", "double-click", "right-click", "hover", "scroll");
    }

    @Test
    void senaryoTaslagiZorunluAlanlariIster() {
        JsonNode required = schema(new ScenarioDraftWriteSpec()).path("required");
        List<String> names = required.valueStream().map(JsonNode::asString).toList();

        assertThat(names).containsExactlyInAnyOrder("title", "baseUrl", "steps");
    }

    @Test
    void yalnizcaIkiAracOnayIster() {
        assertThat(specs).filteredOn(ToolSpec::writeEffect).extracting(ToolSpec::name)
                .containsExactlyInAnyOrder(ToolNames.BROWSER_COMMAND, ToolNames.SCENARIO_DRAFT_WRITE);
    }
}

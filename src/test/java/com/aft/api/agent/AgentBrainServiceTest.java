package com.aft.api.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aft.api.agent.dto.AgentResponse;
import com.aft.api.agent.entity.AgentMessage;
import com.aft.api.agent.entity.AgentSession;
import com.aft.api.agent.entity.MessageRole;
import com.aft.api.agent.entity.SessionMode;
import com.aft.api.agent.memory.ConversationWindow;
import com.aft.api.agent.prompt.PromptLibrary;
import com.aft.api.agent.provider.ModelRouter;
import com.aft.api.agent.provider.ModelTier;
import com.aft.api.agent.routing.IntentRouter;
import com.aft.api.agent.service.AgentBrainService;
import com.aft.api.agent.service.AgentSessionManager;
import com.aft.api.agent.service.ModelUsageService;
import com.aft.api.agent.tool.RemoteToolCallback;
import com.aft.api.agent.tool.ToolCallContext;
import com.aft.api.agent.tool.ToolRegistry;
import com.aft.api.agent.tool.ToolSpec;
import com.aft.api.agent.tool.spec.LocalScenarioReadSpec;
import com.aft.api.common.exception.ApiException;
import com.aft.api.config.AiProperties;
import com.aft.api.realtime.SseEmitterRegistry;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AgentBrainServiceTest {
    private static final UUID SESSION_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID ORG_ID = UUID.randomUUID();
    private static final UUID MESSAGE_ID = UUID.randomUUID();

    @Mock
    private AgentSessionManager sessionManager;
    @Mock
    private ModelRouter modelRouter;
    @Mock
    private ModelUsageService usageService;
    @Mock
    private ToolRegistry toolRegistry;

    private AiProperties properties;
    private AgentBrainService brainService;
    private AgentSession session;

    @BeforeEach
    void setUp() {
        properties = new AiProperties(new AiProperties.Models("buyuk", "yavas", "ultra", null), "FAST",
                Duration.ofSeconds(300), 40, 24000, Duration.ofSeconds(30), 12, 262144);
        session = new AgentSession(ORG_ID, USER_ID, null, "test", SessionMode.CHAT, "buyuk");
        ReflectionTestUtils.setField(session, "id", SESSION_ID);

        brainService = new AgentBrainService(sessionManager, new ConversationWindow(properties),
                new PromptLibrary(), modelRouter, usageService, new SseEmitterRegistry(JsonMapper.builder().build()), toolRegistry, new IntentRouter(),
                properties);

        when(sessionManager.retune(eq(SESSION_ID), eq(USER_ID), any())).thenReturn(session);
        when(sessionManager.recentHistory(any(), anyInt())).thenReturn(List.of());
        when(toolRegistry.callbacksFor(any(), any())).thenReturn(List.of());
        when(sessionManager.append(any(), any(), any(), anyInt()))
                .thenAnswer(call -> stored(call.getArgument(1), call.getArgument(2), call.getArgument(3)));
        when(sessionManager.revise(any(), any(), anyInt()))
                .thenAnswer(call -> stored(MessageRole.ASSISTANT, call.getArgument(1), call.getArgument(2)));
    }

    @Test
    void yanitUretilirVeMesajlarKaydedilir() {
        when(modelRouter.provider()).thenReturn(new StubOllmProvider(List.of("merhaba ", "dunya")));

        AgentResponse response = brainService.respond(SESSION_ID, USER_ID, "selam", null);

        assertThat(response.content()).isEqualTo("merhaba dunya");
        assertThat(response.model()).isEqualTo("buyuk");
        assertThat(response.tokenIn()).isEqualTo(11);
        assertThat(response.tokenOut()).isEqualTo(7);
        verify(sessionManager).append(SESSION_ID, MessageRole.USER, "selam", ConversationWindow.estimate("selam"));
        verify(sessionManager).append(eq(SESSION_ID), eq(MessageRole.ASSISTANT), eq(""), anyInt());
        verify(sessionManager).revise(eq(MESSAGE_ID), eq("merhaba dunya"), anyInt());
    }

    @Test
    void saglayiciHatasindaBosYanitMesajiSilinir() {
        when(modelRouter.provider()).thenReturn(
                StubOllmProvider.failing(new IllegalStateException("baglanti yok")));

        assertThatThrownBy(() -> brainService.respond(SESSION_ID, USER_ID, "Senaryolari listele", null))
                .isInstanceOf(ApiException.class);
        verify(sessionManager).discard(MESSAGE_ID);
    }

    @Test
    void aracKataloguIstemeYazilir() {
        StubOllmProvider provider = new StubOllmProvider(List.of("yanit"));
        when(modelRouter.provider()).thenReturn(provider);
        when(toolRegistry.catalogFor(any(), any())).thenReturn(List.of(new ToolSpec() {
            @Override
            public String name() {
                return "local_scenario_run";
            }

            @Override
            public String description() {
                return "Senaryoyu kosar";
            }

            @Override
            public String inputSchema() {
                return "{}";
            }

            @Override
            public boolean writeEffect() {
                return true;
            }
        }));

        brainService.respond(SESSION_ID, USER_ID, "Senaryolari listele", null);

        String system = provider.lastPrompt().getInstructions().getFirst().getText();
        assertThat(system).contains("local_scenario_run");
        assertThat(system).contains("Senaryoyu kosar");
        assertThat(system).contains("kullanici onayi ister");
        assertThat(system).doesNotContain("{tools}");
    }

    @Test
    void istemciYokkenAracListesiBosBildirilir() {
        StubOllmProvider provider = new StubOllmProvider(List.of("yanit"));
        when(modelRouter.provider()).thenReturn(provider);
        when(toolRegistry.catalogFor(any(), any())).thenReturn(List.of());

        brainService.respond(SESSION_ID, USER_ID, "Senaryolari listele", null);

        String system = provider.lastPrompt().getInstructions().getFirst().getText();
        assertThat(system).contains("arac sunulmadi");
        assertThat(system).doesNotContain("{tools}");
    }

    @Test
    void kullanimKaydiOrganizasyonBazindaYazilir() {
        when(modelRouter.provider()).thenReturn(new StubOllmProvider(List.of("yanit")));

        brainService.respond(SESSION_ID, USER_ID, "Senaryolari listele", null);

        verify(usageService).recordCounts(ORG_ID, USER_ID, SESSION_ID, "buyuk", 11, 7);
    }

    @Test
    void istemSistemMesajiylaBaslar() {
        StubOllmProvider provider = new StubOllmProvider(List.of("yanit"));
        when(modelRouter.provider()).thenReturn(provider);

        brainService.respond(SESSION_ID, USER_ID, "Senaryolari listele", null);

        assertThat(provider.lastPrompt().getInstructions().getFirst().getMessageType())
                .isEqualTo(MessageType.SYSTEM);
    }

    @Test
    void saglayiciHatasiAiProviderErrorAVirilir() {
        when(modelRouter.provider()).thenReturn(
                StubOllmProvider.failing(new IllegalStateException("baglanti yok")));

        assertThatThrownBy(() -> brainService.respond(SESSION_ID, USER_ID, "Senaryolari listele", null))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Model saglayici yanit vermedi");
        verify(sessionManager, never()).markFailed(SESSION_ID);
    }

    @Test
    void acikKanalYokkenAkisReddedilir() {
        assertThatThrownBy(() -> brainService.streamInto(SESSION_ID, USER_ID, "soru", null))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("acik bir akis kanali yok");
    }

    @Test
    void secenekTipiBagliModelinKendisindenGelir() {
        StubOllmProvider provider = new StubOllmProvider(List.of("yanit"), OpenAiChatOptions.builder().temperature(0.3).build());
        when(modelRouter.provider()).thenReturn(provider);
        when(toolRegistry.callbacksFor(any(), any())).thenReturn(List.of(callback()));

        brainService.respond(SESSION_ID, USER_ID, "Senaryolari listele", null);

        ChatOptions options = provider.lastPrompt().getOptions();
        assertThat(options).isInstanceOf(OpenAiChatOptions.class);
        assertThat(options.getModel()).isEqualTo("buyuk");
        assertThat(((ToolCallingChatOptions) options).getToolCallbacks()).hasSize(1);
        assertThat(((ToolCallingChatOptions) options).getToolContext()).containsKey(ToolCallContext.KEY);
    }

    @Test
    void bilinmeyenSaglayicidaGenelTipKullanilir() {
        StubOllmProvider provider = new StubOllmProvider(List.of("yanit"));
        when(modelRouter.provider()).thenReturn(provider);
        when(toolRegistry.callbacksFor(any(), any())).thenReturn(List.of(callback()));

        brainService.respond(SESSION_ID, USER_ID, "Senaryolari listele", null);

        ChatOptions options = provider.lastPrompt().getOptions();
        assertThat(options).isInstanceOf(ToolCallingChatOptions.class);
        assertThat(((ToolCallingChatOptions) options).getToolCallbacks()).hasSize(1);
        assertThat(((ToolCallingChatOptions) options).getToolContext()).containsKey(ToolCallContext.KEY);
    }

    private static ToolCallback callback() {
        return new RemoteToolCallback(new LocalScenarioReadSpec(), null);
    }

    private static AgentMessage stored(MessageRole role, String content, int tokenCount) {
        AgentMessage message = new AgentMessage(SESSION_ID, 1, role, content, tokenCount);
        ReflectionTestUtils.setField(message, "id", MESSAGE_ID);
        return message;
    }
}

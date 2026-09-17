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
import com.aft.api.agent.service.AgentBrainService;
import com.aft.api.agent.service.AgentSessionManager;
import com.aft.api.agent.service.ModelUsageService;
import com.aft.api.agent.tool.ToolRegistry;
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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AgentBrainServiceTest {
    private static final UUID SESSION_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID ORG_ID = UUID.randomUUID();

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
        properties = new AiProperties("ollama", new AiProperties.Models("buyuk", "kucuk", null),
                Duration.ofSeconds(30), 40, 24000, Duration.ofSeconds(30), 12, 262144);
        session = new AgentSession(ORG_ID, USER_ID, null, "test", SessionMode.CHAT, "buyuk");
        ReflectionTestUtils.setField(session, "id", SESSION_ID);

        brainService = new AgentBrainService(sessionManager, new ConversationWindow(properties),
                new PromptLibrary(), modelRouter, usageService, new SseEmitterRegistry(), toolRegistry, properties);

        when(sessionManager.requireOpen(SESSION_ID, USER_ID)).thenReturn(session);
        when(sessionManager.recentHistory(any(), anyInt())).thenReturn(List.of());
        when(toolRegistry.callbacksFor(any())).thenReturn(List.of());
        when(sessionManager.append(any(), any(), any(), anyInt()))
                .thenAnswer(call -> new AgentMessage(SESSION_ID, 1, call.getArgument(1),
                        call.getArgument(2), call.getArgument(3)));
    }

    @Test
    void yanitUretilirVeMesajlarKaydedilir() {
        when(modelRouter.provider()).thenReturn(new StubModelProvider(List.of("merhaba ", "dunya")));

        AgentResponse response = brainService.respond(SESSION_ID, USER_ID, "selam");

        assertThat(response.content()).isEqualTo("merhaba dunya");
        assertThat(response.model()).isEqualTo("buyuk");
        assertThat(response.tokenIn()).isEqualTo(11);
        assertThat(response.tokenOut()).isEqualTo(7);
        verify(sessionManager).append(SESSION_ID, MessageRole.USER, "selam", ConversationWindow.estimate("selam"));
        verify(sessionManager).append(eq(SESSION_ID), eq(MessageRole.ASSISTANT), eq("merhaba dunya"), anyInt());
    }

    @Test
    void kullanimKaydiOrganizasyonBazindaYazilir() {
        when(modelRouter.provider()).thenReturn(new StubModelProvider(List.of("yanit")));

        brainService.respond(SESSION_ID, USER_ID, "soru");

        verify(usageService).recordCounts(ORG_ID, USER_ID, SESSION_ID, "buyuk", 11, 7);
    }

    @Test
    void istemSistemMesajiylaBaslar() {
        StubModelProvider provider = new StubModelProvider(List.of("yanit"));
        when(modelRouter.provider()).thenReturn(provider);

        brainService.respond(SESSION_ID, USER_ID, "soru");

        assertThat(provider.lastPrompt().getInstructions().getFirst().getMessageType())
                .isEqualTo(MessageType.SYSTEM);
    }

    @Test
    void saglayiciHatasiAiProviderErrorAVirilir() {
        when(modelRouter.provider()).thenReturn(
                StubModelProvider.failing(new IllegalStateException("baglanti yok")));

        assertThatThrownBy(() -> brainService.respond(SESSION_ID, USER_ID, "soru"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Model saglayici yanit vermedi");
        verify(sessionManager, never()).markFailed(SESSION_ID);
    }

    @Test
    void acikKanalYokkenAkisReddedilir() {
        assertThatThrownBy(() -> brainService.streamInto(SESSION_ID, USER_ID, "soru"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("acik bir akis kanali yok");
    }
}

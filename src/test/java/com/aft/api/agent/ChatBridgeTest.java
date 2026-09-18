package com.aft.api.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.aft.api.agent.dto.ChatFrame;
import com.aft.api.agent.entity.AgentMessage;
import com.aft.api.agent.entity.AgentSession;
import com.aft.api.agent.entity.MessageRole;
import com.aft.api.agent.entity.SessionMode;
import com.aft.api.agent.memory.ConversationWindow;
import com.aft.api.agent.prompt.PromptLibrary;
import com.aft.api.agent.provider.ModelRouter;
import com.aft.api.agent.routing.IntentRouter;
import com.aft.api.agent.service.AgentBrainService;
import com.aft.api.agent.service.AgentSessionManager;
import com.aft.api.agent.service.ModelUsageService;
import com.aft.api.agent.tool.ToolRegistry;
import com.aft.api.config.AiProperties;
import com.aft.api.realtime.ChatChannel;
import com.aft.api.realtime.DeviceSessionRegistry;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChatBridgeTest {
    private static final UUID SESSION_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID ORG_ID = UUID.randomUUID();
    private static final UUID DEVICE_ID = UUID.randomUUID();
    private static final UUID MESSAGE_ID = UUID.randomUUID();

    @Mock
    private AgentSessionManager sessionManager;
    @Mock
    private ModelRouter modelRouter;
    @Mock
    private ModelUsageService usageService;
    @Mock
    private ToolRegistry toolRegistry;
    @Mock
    private SimpMessagingTemplate messaging;
    @Mock
    private DeviceSessionRegistry sessions;

    private final List<ChatFrame> sent = new ArrayList<>();
    private AgentBrainService brainService;
    private ChatChannel chat;

    @BeforeEach
    void setUp() {
        AiProperties properties = new AiProperties(
                new AiProperties.Models("m", "m", "m", null), "FAST",
                Duration.ofSeconds(5), 40, 24000, Duration.ofSeconds(30), 12, 262144);

        AgentSession session = new AgentSession(ORG_ID, USER_ID, DEVICE_ID, "t", SessionMode.CHAT, "m");
        ReflectionTestUtils.setField(session, "id", SESSION_ID);

        chat = new ChatChannel(messaging, sessions);
        brainService = new AgentBrainService(sessionManager, new ConversationWindow(properties),
                new PromptLibrary(), modelRouter, usageService, chat, toolRegistry,
                new IntentRouter(), Runnable::run, properties);

        when(sessions.isOnline(DEVICE_ID)).thenReturn(true);
        when(sessionManager.retune(eq(SESSION_ID), eq(USER_ID), any())).thenReturn(session);
        when(sessionManager.recentHistory(any(), anyInt())).thenReturn(List.of());
        when(toolRegistry.callbacksFor(any(), any())).thenReturn(List.of());
        when(toolRegistry.catalogFor(any(), any())).thenReturn(List.of());
        when(sessionManager.append(any(), any(), any(), anyInt())).thenAnswer(call -> stored());
        when(sessionManager.revise(any(), any(), anyInt())).thenAnswer(call -> stored());

        org.mockito.Mockito.doAnswer(call -> {
            sent.add(call.getArgument(2, ChatFrame.class));
            return null;
        }).when(messaging).convertAndSendToUser(any(), any(), any(Object.class));
    }

    private static AgentMessage stored() {
        AgentMessage message = new AgentMessage(SESSION_ID, 1, MessageRole.ASSISTANT, "", 0);
        ReflectionTestUtils.setField(message, "id", MESSAGE_ID);
        return message;
    }

    @Test
    void sohbetTuruDeltaVeDoneKaresiGonderir() {
        when(modelRouter.provider())
                .thenReturn(new StubOllmProvider(List.of("Merhaba", "! Size", " nasil yardimci olabilirim?")));

        brainService.streamInto(SESSION_ID, USER_ID, "merhaba", null, "t-1");

        assertThat(sent).extracting(ChatFrame::kind).containsExactly("delta", "delta", "delta", "done");
        assertThat(sent).allMatch(frame -> "t-1".equals(frame.turnId()));
        assertThat(sent.stream().filter(f -> "delta".equals(f.kind())).map(ChatFrame::text)
                .reduce("", String::concat)).isEqualTo("Merhaba! Size nasil yardimci olabilirim?");
        assertThat(sent.getLast().messageId()).isEqualTo(MESSAGE_ID.toString());
    }

    @Test
    void aracliTurTekParcaVeDoneGonderir() {
        when(modelRouter.provider()).thenReturn(new StubOllmProvider(List.of("liste hazir")));

        brainService.streamInto(SESSION_ID, USER_ID, "Senaryolari listele", null, "t-2");

        assertThat(sent).extracting(ChatFrame::kind).containsExactly("delta", "done");
        assertThat(sent.getFirst().text()).isEqualTo("liste hazir");
    }

    @Test
    void modelHatasiErrorKaresiGonderir() {
        when(modelRouter.provider())
                .thenReturn(StubOllmProvider.failing(new IllegalStateException("baglanti yok")));

        brainService.streamInto(SESSION_ID, USER_ID, "merhaba", null, "t-3");

        assertThat(sent).extracting(ChatFrame::kind).containsExactly("error");
        assertThat(sent.getFirst().text()).contains("baglanti yok");
    }

    @Test
    void istemciBagliDegilseIstekReddedilir() {
        when(sessions.isOnline(DEVICE_ID)).thenReturn(false);

        org.assertj.core.api.Assertions
                .assertThatThrownBy(() -> brainService.streamInto(SESSION_ID, USER_ID, "merhaba", null, "t-4"))
                .hasMessageContaining("bagli bir istemci kanali yok");
        assertThat(sent).isEmpty();
    }
}

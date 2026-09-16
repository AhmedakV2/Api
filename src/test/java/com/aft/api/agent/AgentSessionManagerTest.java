package com.aft.api.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aft.api.agent.dto.CreateSessionRequest;
import com.aft.api.agent.entity.AgentMessage;
import com.aft.api.agent.entity.AgentSession;
import com.aft.api.agent.entity.MessageRole;
import com.aft.api.agent.entity.SessionMode;
import com.aft.api.agent.provider.ModelRouter;
import com.aft.api.agent.provider.TaskKind;
import com.aft.api.agent.repository.AgentMessageRepository;
import com.aft.api.agent.repository.AgentSessionRepository;
import com.aft.api.agent.service.AgentSessionManager;
import com.aft.api.common.audit.AuditLogService;
import com.aft.api.common.exception.ForbiddenException;
import com.aft.api.common.exception.NotFoundException;
import com.aft.api.common.exception.ValidationException;
import com.aft.api.device.service.DeviceRegistryService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AgentSessionManagerTest {
    private static final UUID SESSION_ID = UUID.randomUUID();
    private static final UUID OWNER_ID = UUID.randomUUID();
    private static final UUID OTHER_ID = UUID.randomUUID();
    private static final UUID ORG_ID = UUID.randomUUID();
    private static final UUID DEVICE_ID = UUID.randomUUID();

    @Mock
    private AgentSessionRepository sessionRepository;
    @Mock
    private AgentMessageRepository messageRepository;
    @Mock
    private ModelRouter modelRouter;
    @Mock
    private DeviceRegistryService deviceRegistry;
    @Mock
    private AuditLogService auditLog;

    private AgentSessionManager manager;
    private AgentSession session;

    @BeforeEach
    void setUp() {
        manager = new AgentSessionManager(sessionRepository, messageRepository, modelRouter,
                deviceRegistry, auditLog);

        session = new AgentSession(ORG_ID, OWNER_ID, null, "test", SessionMode.CHAT, "buyuk");
        ReflectionTestUtils.setField(session, "id", SESSION_ID);

        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(AgentSession.class))).thenAnswer(call -> {
            AgentSession saved = call.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", UUID.randomUUID());
            return saved;
        });
        when(modelRouter.modelFor(TaskKind.PLANNING)).thenReturn("varsayilan-model");
    }

    @Test
    void baskaKullanicininOturumunaErisimEngellenir() {
        assertThatThrownBy(() -> manager.requireOwned(SESSION_ID, OTHER_ID))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void sahibiKendiOturumunaErisebilir() {
        assertThatCode(() -> manager.requireOwned(SESSION_ID, OWNER_ID)).doesNotThrowAnyException();
    }

    @Test
    void bilinmeyenOturumBulunamadiHatasiVerir() {
        UUID missing = UUID.randomUUID();
        when(sessionRepository.findById(missing)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> manager.requireOwned(missing, OWNER_ID))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void kapaliOturumaMesajYazilamaz() {
        session.close(java.time.Instant.now());

        assertThatThrownBy(() -> manager.requireOpen(SESSION_ID, OWNER_ID))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("kapali");
    }

    @Test
    void baskaOrganizasyonunCihaziReddedilir() {
        when(deviceRegistry.existsInOrg(DEVICE_ID, ORG_ID)).thenReturn(false);

        assertThatThrownBy(() -> manager.create(
                new CreateSessionRequest(ORG_ID, DEVICE_ID, "test", SessionMode.CHAT, null), OWNER_ID))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("organizasyona ait degil");
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void ayniOrganizasyonunCihaziKabulEdilir() {
        when(deviceRegistry.existsInOrg(DEVICE_ID, ORG_ID)).thenReturn(true);

        assertThatCode(() -> manager.create(
                new CreateSessionRequest(ORG_ID, DEVICE_ID, "test", SessionMode.CHAT, null), OWNER_ID))
                .doesNotThrowAnyException();
    }

    @Test
    void modelVerilmezseVarsayilanPlanlamaModeliKullanilir() {
        var created = manager.create(
                new CreateSessionRequest(ORG_ID, null, "test", SessionMode.CHAT, null), OWNER_ID);

        assertThat(created.model()).isEqualTo("varsayilan-model");
    }

    @Test
    void siraNumarasiSonMesajdanDevamEder() {
        when(messageRepository.findMaxSeq(SESSION_ID)).thenReturn(7);
        when(messageRepository.save(any(AgentMessage.class))).thenAnswer(call -> call.getArgument(0));

        AgentMessage saved = manager.append(SESSION_ID, MessageRole.USER, "merhaba", 3);

        assertThat(saved.getSeq()).isEqualTo(8);
    }

    @Test
    void gecmisKronolojikSiradaDoner() {
        AgentMessage ucuncu = new AgentMessage(SESSION_ID, 3, MessageRole.ASSISTANT, "ucuncu", 1);
        AgentMessage ikinci = new AgentMessage(SESSION_ID, 2, MessageRole.USER, "ikinci", 1);
        AgentMessage birinci = new AgentMessage(SESSION_ID, 1, MessageRole.ASSISTANT, "birinci", 1);
        when(messageRepository.findRecent(any(), anyInt())).thenReturn(List.of(ucuncu, ikinci, birinci));

        List<AgentMessage> history = manager.recentHistory(SESSION_ID, 10);

        assertThat(history).extracting(AgentMessage::getSeq).containsExactly(1, 2, 3);
    }
}

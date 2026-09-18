package com.aft.api.agent.service;

import com.aft.api.agent.dto.CreateSessionRequest;
import com.aft.api.agent.dto.MessageDto;
import com.aft.api.agent.dto.SessionDetailDto;
import com.aft.api.agent.dto.SessionDto;
import com.aft.api.agent.entity.AgentMessage;
import com.aft.api.agent.entity.AgentSession;
import com.aft.api.agent.entity.MessageRole;
import com.aft.api.agent.entity.SessionStatus;
import com.aft.api.agent.provider.ModelRouter;
import com.aft.api.agent.provider.TaskKind;
import com.aft.api.agent.repository.AgentMessageRepository;
import com.aft.api.agent.repository.AgentSessionRepository;
import com.aft.api.common.audit.AuditAction;
import com.aft.api.common.audit.AuditLogService;
import com.aft.api.common.exception.ForbiddenException;
import com.aft.api.common.exception.NotFoundException;
import com.aft.api.device.service.DeviceRegistryService;
import com.aft.api.common.exception.ValidationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgentSessionManager {
    private final AgentSessionRepository sessionRepository;
    private final AgentMessageRepository messageRepository;
    private final ModelRouter modelRouter;
    private final DeviceRegistryService deviceRegistry;
    private final AuditLogService auditLog;

    public AgentSessionManager(AgentSessionRepository sessionRepository,
                               AgentMessageRepository messageRepository,
                               ModelRouter modelRouter,
                               DeviceRegistryService deviceRegistry,
                               AuditLogService auditLog) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.modelRouter = modelRouter;
        this.deviceRegistry = deviceRegistry;
        this.auditLog = auditLog;
    }

    @Transactional
    public SessionDto create(CreateSessionRequest request, UUID userId) {
        if (request.deviceId() != null && !deviceRegistry.existsInOrg(request.deviceId(), request.orgId())) {
            throw new ValidationException("Cihaz bu organizasyona ait degil");
        }
        String model = (request.model() == null || request.model().isBlank())
                ? modelRouter.modelFor(TaskKind.PLANNING) : request.model();

        AgentSession session = sessionRepository.save(new AgentSession(request.orgId(), userId,
                request.deviceId(), request.title(), request.mode(), model));

        auditLog.record(AuditAction.AGENT_SESSION_OPENED, "AgentSession", session.getId().toString(),
                userId, Map.of("model", model));
        return toDto(session);
    }

    @Transactional(readOnly = true)
    public Page<SessionDto> list(UUID userId, UUID orgId, SessionStatus status, Pageable pageable) {
        return sessionRepository.findOwned(userId, orgId, status, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public SessionDetailDto detail(UUID sessionId, UUID userId) {
        AgentSession session = requireOwned(sessionId, userId);
        List<MessageDto> messages = messageRepository.findBySessionIdOrderBySeqAsc(sessionId).stream()
                .map(this::toDto).toList();
        return new SessionDetailDto(toDto(session), messages);
    }

    @Transactional
    public void delete(UUID sessionId, UUID userId) {
        AgentSession session = requireOwned(sessionId, userId);
        messageRepository.deleteBySessionId(sessionId);
        sessionRepository.delete(session);
        auditLog.record(AuditAction.AGENT_SESSION_DELETED, "AgentSession", sessionId.toString(),
                userId, Map.of());
    }

    @Transactional
    public AgentMessage append(UUID sessionId, MessageRole role, String content, int tokenCount) {
        int nextSeq = messageRepository.findMaxSeq(sessionId) + 1;
        return messageRepository.save(new AgentMessage(sessionId, nextSeq, role, content, tokenCount));
    }

    @Transactional
    public AgentMessage revise(UUID messageId, String content, int tokenCount) {
        AgentMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> NotFoundException.of("AgentMessage", messageId));
        message.revise(content, tokenCount);
        return messageRepository.save(message);
    }

    @Transactional
    public void discard(UUID messageId) {
        messageRepository.findById(messageId).ifPresent(messageRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<AgentMessage> recentHistory(UUID sessionId, int limit) {
        List<AgentMessage> recent = new ArrayList<>(messageRepository.findRecent(sessionId, limit));
        Collections.reverse(recent);
        return recent;
    }

    @Transactional(readOnly = true)
    public AgentSession requireOwned(UUID sessionId, UUID userId) {
        AgentSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> NotFoundException.of("AgentSession", sessionId));
        if (!session.getUserId().equals(userId)) {
            throw new ForbiddenException("Bu oturum size ait degil");
        }
        return session;
    }

    @Transactional(readOnly = true)
    public AgentSession requireOpen(UUID sessionId, UUID userId) {
        AgentSession session = requireOwned(sessionId, userId);
        if (!session.isOpen()) {
            throw new ValidationException("Oturum kapali");
        }
        return session;
    }

    @Transactional
    public void markFailed(UUID sessionId) {
        sessionRepository.findById(sessionId).ifPresent(session -> session.fail(Instant.now()));
    }

    public SessionDto toDto(AgentSession session) {
        return new SessionDto(session.getId(), session.getOrgId(), session.getUserId(), session.getDeviceId(),
                session.getTitle(), session.getMode().name(), session.getModel(), session.getStatus().name(),
                session.getCreatedAt(), session.getClosedAt());
    }

    public MessageDto toDto(AgentMessage message) {
        return new MessageDto(message.getId(), message.getSeq(), message.getRole().name(),
                message.getContent(), message.getTokenCount(), message.getCreatedAt());
    }
}

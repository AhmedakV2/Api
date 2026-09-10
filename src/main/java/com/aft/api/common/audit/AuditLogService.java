package com.aft.api.common.audit;

import com.aft.api.common.audit.entity.AuditLog;
import com.aft.api.common.audit.repository.AuditLogRepository;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class AuditLogService {
    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository repository;

    public AuditLogService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public void record(AuditAction action, String entityType, String entityId,
                       UUID actorId, Map<String, Object> detail) {
        writeAsync(action.name(), entityType, entityId, actorId, currentIp(), detail);
    }

    @Async("auditExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void writeAsync(String action, String entityType, String entityId,
                           UUID actorId, String ip, Map<String, Object> detail) {
        try {
            repository.save(new AuditLog(actorId, action, entityType, entityId, ip, detail));
        } catch (RuntimeException e) {
            log.error("Denetim kaydi yazilamadi action={} entity={}", action, entityType, e);
        }
    }

    private String currentIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            String forwarded = attributes.getRequest().getHeader("X-Forwarded-For");
            return (forwarded != null && !forwarded.isBlank())
                    ? forwarded.split(",")[0].trim() : attributes.getRequest().getRemoteAddr();
        }
        return null;
    }
}

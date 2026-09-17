package com.aft.api.common.audit;

import com.aft.api.common.audit.entity.AuditLog;
import com.aft.api.common.audit.repository.AuditLogRepository;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuditLogWriter {
    private static final Logger log = LoggerFactory.getLogger(AuditLogWriter.class);

    private final AuditLogRepository repository;

    public AuditLogWriter(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Async("auditExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void write(String action, String entityType, String entityId,
                      UUID actorId, String ip, Map<String, Object> detail) {
        try {
            repository.save(new AuditLog(actorId, action, entityType, entityId, ip, detail));
        } catch (RuntimeException e) {
            log.error("Denetim kaydi yazilamadi action={} entity={}", action, entityType, e);
        }
    }
}
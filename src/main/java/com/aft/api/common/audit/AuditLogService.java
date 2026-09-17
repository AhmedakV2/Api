package com.aft.api.common.audit;

import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class AuditLogService {
    private final AuditLogWriter writer;

    public AuditLogService(AuditLogWriter writer) {
        this.writer = writer;
    }

    public void record(AuditAction action, String entityType, String entityId,
                       UUID actorId, Map<String, Object> detail) {
        writer.write(action.name(), entityType, entityId, actorId, currentIp(), detail);
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
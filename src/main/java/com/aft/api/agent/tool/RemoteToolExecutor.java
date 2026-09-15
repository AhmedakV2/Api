package com.aft.api.agent.tool;

import com.aft.api.agent.entity.AgentToolCall;
import com.aft.api.agent.guard.PromptSanitizer;
import com.aft.api.agent.guard.ToolPolicy;
import com.aft.api.agent.repository.AgentToolCallRepository;
import com.aft.api.common.exception.ApiException;
import com.aft.api.common.exception.ErrorCode;
import com.aft.api.realtime.ToolChannel;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/** Arac cagrisini istemciye yonlendirir ve sonucu bekler. Yurutme sunucuda yapilmaz. */
@Component
public class RemoteToolExecutor {

    private static final Logger log = LoggerFactory.getLogger(RemoteToolExecutor.class);

    private final ToolChannel toolChannel;
    private final PendingToolRegistry pendingRegistry;
    private final ToolPolicy policy;
    private final PromptSanitizer sanitizer;
    private final AgentToolCallRepository callRepository;
    private final JsonMapper jsonMapper;

    public RemoteToolExecutor(ToolChannel toolChannel,
                              PendingToolRegistry pendingRegistry,
                              ToolPolicy policy,
                              PromptSanitizer sanitizer,
                              AgentToolCallRepository callRepository,
                              JsonMapper jsonMapper) {
        this.toolChannel = toolChannel;
        this.pendingRegistry = pendingRegistry;
        this.policy = policy;
        this.sanitizer = sanitizer;
        this.callRepository = callRepository;
        this.jsonMapper = jsonMapper;
    }

    public String invoke(ToolSpec spec, String argumentsJson, ToolCallContext context) {
        if (context.deviceId() == null) {
            return error("Bu oturuma bagli bir istemci yok");
        }
        if (context.nextHop() > policy.maxHops()) {
            log.warn("Arac cagrisi siniri asildi sessionId={} sinir={}", context.sessionId(), policy.maxHops());
            return error("Ardisik arac cagrisi siniri asildi, dongu kesildi");
        }

        UUID callId = UUID.randomUUID();
        long started = System.nanoTime();
        AgentToolCall record = persist(context, spec, argumentsJson);

        try {
            CompletableFuture<ToolResult> future = pendingRegistry.register(callId);
            toolChannel.send(context.deviceId(), new ToolInvocation(callId, context.sessionId(),
                    spec.name(), argumentsJson, policy.requiresApproval(spec), policy.timeoutMs()));

            ToolResult result = future.get(policy.timeoutMs(), TimeUnit.MILLISECONDS);
            return finish(record, result, started);
        } catch (TimeoutException e) {
            pendingRegistry.cancel(callId);
            finalizeFailure(record, "Zaman asimi", started);
            return error("Istemci arac cagrisini suresinde yanitlamadi");
        } catch (ApiException e) {
            pendingRegistry.cancel(callId);
            finalizeFailure(record, e.getMessage(), started);
            return error(e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            pendingRegistry.cancel(callId);
            finalizeFailure(record, "Kesildi", started);
            return error("Arac cagrisi kesildi");
        } catch (ExecutionException e) {
            pendingRegistry.cancel(callId);
            finalizeFailure(record, e.getMessage(), started);
            return error("Arac cagrisi basarisiz oldu");
        }
    }

    private String finish(AgentToolCall record, ToolResult result, long started) {
        int durationMs = elapsed(started);
        if (!result.ok()) {
            finalizeFailure(record, result.error(), started);
            return error(result.error() == null ? "Arac calistirilamadi" : result.error());
        }

        PromptSanitizer.Trimmed trimmed = sanitizer.trim(result.contentJson(), policy.maxResultBytes());
        String clean = sanitizer.sanitize(trimmed.content());
        boolean truncated = trimmed.truncated() || result.truncated();

        finalizeSuccess(record, clean, durationMs);
        return truncated ? clean + "\n[icerik boyut siniri nedeniyle kirpildi]" : clean;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected AgentToolCall persist(ToolCallContext context, ToolSpec spec, String argumentsJson) {
        if (context.messageId() == null) {
            return null;
        }
        return callRepository.save(new AgentToolCall(context.messageId(), context.deviceId(),
                spec.name(), parse(argumentsJson)));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void finalizeSuccess(AgentToolCall record, String summary, int durationMs) {
        if (record == null) {
            return;
        }
        record.succeed(summary, durationMs);
        callRepository.save(record);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void finalizeFailure(AgentToolCall record, String errorText, long started) {
        if (record == null) {
            return;
        }
        record.fail(errorText, elapsed(started));
        callRepository.save(record);
    }

    private Map<String, Object> parse(String argumentsJson) {
        if (argumentsJson == null || argumentsJson.isBlank()) {
            return Map.of();
        }
        try {
            return jsonMapper.readValue(argumentsJson, new TypeReference<Map<String, Object>>() {
            });
        } catch (RuntimeException e) {
            return Map.of("raw", argumentsJson);
        }
    }

    private int elapsed(long startedNanos) {
        return (int) ((System.nanoTime() - startedNanos) / 1_000_000L);
    }

    private String error(String message) {
        return "{\"ok\":false,\"error\":" + jsonMapper.writeValueAsString(message) + "}";
    }

    public ErrorCode timeoutCode() {
        return ErrorCode.TOOL_TIMEOUT;
    }
}

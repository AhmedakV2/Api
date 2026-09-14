package com.aft.api.realtime.web;

import com.aft.api.agent.service.AgentBrainService;
import com.aft.api.agent.service.AgentSessionManager;
import com.aft.api.realtime.SseEmitterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.aft.api.security.AftPrincipal;

@RestController
@RequestMapping("/api/v1/agent/sessions")
@Tag(name = "Agent Akis", description = "Sunucudan istemciye yanit akisi")
public class StreamController {
    private final AgentSessionManager sessionManager;
    private final AgentBrainService brainService;
    private final SseEmitterRegistry emitters;

    public StreamController(AgentSessionManager sessionManager,
                            AgentBrainService brainService,
                            SseEmitterRegistry emitters) {
        this.sessionManager = sessionManager;
        this.brainService = brainService;
        this.emitters = emitters;
    }

    @GetMapping(value = "/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Yanit akisi")
    public SseEmitter stream(@PathVariable UUID id, @AuthenticationPrincipal AftPrincipal principal) {
        sessionManager.requireOpen(id, principal.userId());
        return emitters.open(id, brainService.streamTimeoutMillis());
    }
}

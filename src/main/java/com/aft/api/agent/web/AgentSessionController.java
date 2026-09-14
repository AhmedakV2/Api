package com.aft.api.agent.web;

import com.aft.api.agent.dto.AgentRequest;
import com.aft.api.agent.dto.AgentResponse;
import com.aft.api.agent.dto.CreateSessionRequest;
import com.aft.api.agent.dto.SessionDetailDto;
import com.aft.api.agent.dto.SessionDto;
import com.aft.api.agent.entity.SessionStatus;
import com.aft.api.agent.service.AgentBrainService;
import com.aft.api.agent.service.AgentSessionManager;
import com.aft.api.common.dto.PageResponse;
import com.aft.api.realtime.SseEmitterRegistry;
import com.aft.api.security.AftPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/agent/sessions")
@Tag(name = "Agent Oturumu", description = "Oturum yasam dongusu ve mesajlasma")
public class AgentSessionController {
    private final AgentSessionManager sessionManager;
    private final AgentBrainService brainService;
    private final SseEmitterRegistry emitters;

    public AgentSessionController(AgentSessionManager sessionManager,
                                  AgentBrainService brainService,
                                  SseEmitterRegistry emitters) {
        this.sessionManager = sessionManager;
        this.brainService = brainService;
        this.emitters = emitters;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN','OWNER') and @aft.inOrg(#request.orgId(), authentication)")
    @Operation(summary = "Yeni oturum acma")
    public ResponseEntity<SessionDto> create(@Valid @RequestBody CreateSessionRequest request,
                                             @AuthenticationPrincipal AftPrincipal principal) {
        return ResponseEntity.status(201).body(sessionManager.create(request, principal.userId()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated() and @aft.inOrg(#orgId, authentication)")
    @Operation(summary = "Oturum listesi")
    public PageResponse<SessionDto> list(@RequestParam UUID orgId,
                                         @RequestParam(required = false) SessionStatus status,
                                         @ParameterObject @PageableDefault(size = 20, sort = "createdAt",
                                                 direction = Sort.Direction.DESC) Pageable pageable,
                                         @AuthenticationPrincipal AftPrincipal principal) {
        return PageResponse.of(sessionManager.list(principal.userId(), orgId, status, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Oturum gecmisi")
    public SessionDetailDto detail(@PathVariable UUID id, @AuthenticationPrincipal AftPrincipal principal) {
        return sessionManager.detail(id, principal.userId());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Oturum silme")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal AftPrincipal principal) {
        sessionManager.delete(id, principal.userId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/messages")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mesaj gonderme")
    public ResponseEntity<AgentResponse> message(@PathVariable UUID id,
                                                 @RequestParam(defaultValue = "false") boolean stream,
                                                 @Valid @RequestBody AgentRequest request,
                                                 @AuthenticationPrincipal AftPrincipal principal) {
        if (stream) {
            brainService.streamInto(id, principal.userId(), request.content());
            return ResponseEntity.accepted().build();
        }
        return ResponseEntity.ok(brainService.respond(id, principal.userId(), request.content()));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Devam eden uretimi durdurma")
    public Map<String, Boolean> cancel(@PathVariable UUID id,
                                       @AuthenticationPrincipal AftPrincipal principal) {
        sessionManager.requireOwned(id, principal.userId());
        return Map.of("cancelled", emitters.cancel(id));
    }
}

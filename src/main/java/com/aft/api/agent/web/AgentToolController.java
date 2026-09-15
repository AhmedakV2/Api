package com.aft.api.agent.web;

import com.aft.api.agent.dto.ToolResultRequest;
import com.aft.api.agent.dto.ToolSpecDto;
import com.aft.api.agent.tool.ToolPolicyView;
import com.aft.api.agent.tool.ToolResult;
import com.aft.api.agent.tool.ToolSpec;
import com.aft.api.realtime.ToolChannel;
import com.aft.api.security.ApiKeyPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/agent")
@Tag(name = "Agent Arac", description = "Arac katalogu ve sonuc geri bildirimi")
public class AgentToolController {

    private final ToolPolicyView toolView;
    private final ToolChannel toolChannel;

    public AgentToolController(ToolPolicyView toolView, ToolChannel toolChannel) {
        this.toolView = toolView;
        this.toolChannel = toolChannel;
    }

    @GetMapping("/tools")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Kayitli arac semalari")
    public List<ToolSpecDto> tools(@RequestParam(required = false) UUID deviceId) {
        List<ToolSpec> specs = deviceId == null ? toolView.catalog() : toolView.catalogFor(deviceId);
        return specs.stream()
                .map(spec -> new ToolSpecDto(spec.name(), spec.description(),
                        spec.inputSchema(), spec.writeEffect()))
                .toList();
    }

    @PostMapping("/tool-results")
    @PreAuthorize("hasRole('DEVICE')")
    @Operation(summary = "Arac sonucunu HTTP ile geri verme")
    public ResponseEntity<Void> result(@Valid @RequestBody ToolResultRequest request,
                                       @AuthenticationPrincipal ApiKeyPrincipal apiKey) {
        toolChannel.deliver(new ToolResult(request.callId(), request.ok(),
                request.contentJson(), request.error(), request.truncated()));
        return ResponseEntity.accepted().build();
    }
}

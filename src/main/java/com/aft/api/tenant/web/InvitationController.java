package com.aft.api.tenant.web;

import com.aft.api.tenant.dto.AcceptInvitationRequest;
import com.aft.api.tenant.service.InvitationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/invitations")
@Tag(name = "Davet", description = "Davet kabul akisi")
public class InvitationController {
    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping("/accept")
    @SecurityRequirements
    @Operation(summary = "Daveti kabul etme")
    public ResponseEntity<Void> accept(@Valid @RequestBody AcceptInvitationRequest request) {
        invitationService.accept(request);
        return ResponseEntity.noContent().build();
    }
}

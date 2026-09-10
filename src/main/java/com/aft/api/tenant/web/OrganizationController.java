package com.aft.api.tenant.web;

import com.aft.api.security.AftPrincipal;
import com.aft.api.tenant.dto.CreateOrganizationRequest;
import com.aft.api.tenant.dto.InvitationDto;
import com.aft.api.tenant.dto.InviteRequest;
import com.aft.api.tenant.dto.MemberDto;
import com.aft.api.tenant.dto.OrganizationDto;
import com.aft.api.tenant.dto.UpdateOrganizationRequest;
import com.aft.api.tenant.service.InvitationService;
import com.aft.api.tenant.service.MembershipService;
import com.aft.api.tenant.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organizations")
@Tag(name = "Organizasyon", description = "Organizasyon, uyelik ve davet yonetimi")
public class OrganizationController {
    private final OrganizationService organizationService;
    private final MembershipService membershipService;
    private final InvitationService invitationService;

    public OrganizationController(OrganizationService organizationService,
                                  MembershipService membershipService,
                                  InvitationService invitationService) {
        this.organizationService = organizationService;
        this.membershipService = membershipService;
        this.invitationService = invitationService;
    }

    @GetMapping
    @Operation(summary = "Uyesi olunan organizasyonlar")
    public List<OrganizationDto> list(@AuthenticationPrincipal AftPrincipal principal) {
        return organizationService.findByMember(principal.userId());
    }

    @PostMapping
    @Operation(summary = "Organizasyon olusturma")
    public ResponseEntity<OrganizationDto> create(@Valid @RequestBody CreateOrganizationRequest request,
                                                  @AuthenticationPrincipal AftPrincipal principal) {
        return ResponseEntity.status(201).body(organizationService.create(request, principal.userId()));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') and @aft.inOrg(#id, authentication)")
    @Operation(summary = "Ayar ve butce guncelleme")
    public OrganizationDto update(@PathVariable UUID id,
                                  @Valid @RequestBody UpdateOrganizationRequest request,
                                  @AuthenticationPrincipal AftPrincipal principal) {
        return organizationService.update(id, request, principal.userId());
    }

    @GetMapping("/{id}/members")
    @PreAuthorize("hasRole('ADMIN') and @aft.inOrg(#id, authentication)")
    @Operation(summary = "Uye listesi")
    public List<MemberDto> members(@PathVariable UUID id) {
        return membershipService.listMembers(id);
    }

    @PostMapping("/{id}/invitations")
    @PreAuthorize("hasRole('ADMIN') and @aft.inOrg(#id, authentication)")
    @Operation(summary = "Davet gonderme")
    public ResponseEntity<InvitationDto> invite(@PathVariable UUID id,
                                                @Valid @RequestBody InviteRequest request,
                                                @AuthenticationPrincipal AftPrincipal principal) {
        return ResponseEntity.status(201).body(invitationService.invite(id, request, principal.userId()));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasRole('ADMIN') and @aft.inOrg(#id, authentication)")
    @Operation(summary = "Uyelik sonlandirma")
    public ResponseEntity<Void> removeMember(@PathVariable UUID id, @PathVariable UUID userId,
                                             @AuthenticationPrincipal AftPrincipal principal) {
        membershipService.removeMember(id, userId, principal.userId());
        return ResponseEntity.noContent().build();
    }
}

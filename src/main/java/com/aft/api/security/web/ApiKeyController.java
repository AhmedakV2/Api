package com.aft.api.security.web;

import com.aft.api.security.AftPrincipal;
import com.aft.api.security.dto.ApiKeyCreatedDto;
import com.aft.api.security.dto.ApiKeyDto;
import com.aft.api.security.dto.CreateApiKeyRequest;
import com.aft.api.security.service.ApiKeyService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/api-keys")
@Tag(name = "Anahtar", description = "Ajan ve CI anahtari yonetimi")
public class ApiKeyController {
    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') and @aft.inOrg(#orgId, authentication)")
    @Operation(summary = "Anahtar listesi")
    public List<ApiKeyDto> list(@RequestParam UUID orgId) {
        return apiKeyService.list(orgId);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') and @aft.inOrg(#request.orgId(), authentication)")
    @Operation(summary = "Anahtar uretme, gizli deger bir kez doner")
    public ResponseEntity<ApiKeyCreatedDto> create(@Valid @RequestBody CreateApiKeyRequest request,
                                                   @AuthenticationPrincipal AftPrincipal principal) {
        return ResponseEntity.status(201).body(apiKeyService.create(principal.userId(), request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') and @aft.inOrg(#orgId, authentication)")
    @Operation(summary = "Anahtar iptali")
    public ResponseEntity<Void> revoke(@PathVariable UUID id, @RequestParam UUID orgId,
                                       @AuthenticationPrincipal AftPrincipal principal) {
        apiKeyService.revoke(id, orgId, principal.userId());
        return ResponseEntity.noContent().build();
    }
}

package com.aft.api.device.web;

import com.aft.api.device.dto.CapabilityBulkRequest;
import com.aft.api.device.dto.DeviceDto;
import com.aft.api.device.dto.DeviceProvisionDto;
import com.aft.api.device.dto.DeviceRegisterRequest;
import com.aft.api.device.service.DevicePresenceService;
import com.aft.api.device.service.DeviceProvisioningService;
import com.aft.api.device.service.DeviceRegistryService;
import com.aft.api.security.AftPrincipal;
import com.aft.api.security.ApiKeyPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/devices")
@Tag(name = "Cihaz", description = "Masaustu istemci kaydi ve canlilik")
public class DeviceController {
    private final DeviceRegistryService registryService;
    private final DevicePresenceService presenceService;
    private final DeviceProvisioningService provisioningService;

    public DeviceController(DeviceRegistryService registryService,
                            DevicePresenceService presenceService,
                            DeviceProvisioningService provisioningService) {
        this.registryService = registryService;
        this.presenceService = presenceService;
        this.provisioningService = provisioningService;
    }

    @PostMapping("/provision")
    @PreAuthorize("hasAnyRole('USER','ADMIN','OWNER')")
    @Operation(summary = "Oturum jetonu ile cihaz anahtari ve kaydi uretme")
    public DeviceProvisionDto provision(@Valid @RequestBody DeviceRegisterRequest request,
                                        @AuthenticationPrincipal AftPrincipal principal) {
        return provisioningService.provision(principal.userId(), request);
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('DEVICE')")
    @Operation(summary = "Masaustu istemci kaydi", description = "X-Aft-Key basligi ile kimliklenir")
    public DeviceDto register(@Valid @RequestBody DeviceRegisterRequest request,
                              @AuthenticationPrincipal ApiKeyPrincipal apiKey) {
        return registryService.register(apiKey, request);
    }

    @PostMapping("/{id}/heartbeat")
    @PreAuthorize("hasRole('DEVICE')")
    @Operation(summary = "Canlilik bildirimi")
    public Map<String, Instant> heartbeat(@PathVariable UUID id,
                                          @AuthenticationPrincipal ApiKeyPrincipal apiKey) {
        return Map.of("seenAt", presenceService.heartbeat(id, apiKey));
    }

    @PutMapping("/{id}/capabilities")
    @PreAuthorize("hasRole('DEVICE')")
    @Operation(summary = "Desteklenen araclari bildirme")
    public DeviceDto capabilities(@PathVariable UUID id,
                                  @Valid @RequestBody CapabilityBulkRequest request,
                                  @AuthenticationPrincipal ApiKeyPrincipal apiKey) {
        return registryService.replaceCapabilities(id, apiKey, request);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated() and @aft.inOrg(#orgId, authentication)")
    @Operation(summary = "Bagli istemciler")
    public List<DeviceDto> list(@RequestParam UUID orgId) {
        return registryService.listByOrg(orgId);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') and @aft.inOrg(#orgId, authentication)")
    @Operation(summary = "Cihaz kaydini silme")
    public ResponseEntity<Void> remove(@PathVariable UUID id, @RequestParam UUID orgId,
                                       @AuthenticationPrincipal AftPrincipal principal) {
        registryService.remove(id, orgId, principal.userId());
        return ResponseEntity.noContent().build();
    }
}

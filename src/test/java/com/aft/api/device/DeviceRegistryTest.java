package com.aft.api.device;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aft.api.common.exception.ForbiddenException;
import com.aft.api.device.dto.CapabilityBulkRequest;
import com.aft.api.device.dto.DeviceCapabilityDto;
import com.aft.api.device.dto.DeviceDto;
import com.aft.api.device.dto.DeviceRegisterRequest;
import com.aft.api.device.service.DeviceRegistryService;
import com.aft.api.security.ApiKeyPrincipal;
import com.aft.api.support.AbstractIntegrationTest;
import com.aft.api.tenant.dto.CreateOrganizationRequest;
import com.aft.api.tenant.service.OrganizationService;
import com.aft.api.user.dto.CreateUserRequest;
import com.aft.api.user.entity.RoleCode;
import com.aft.api.user.service.UserService;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DeviceRegistryTest extends AbstractIntegrationTest {
    @Autowired
    private DeviceRegistryService registryService;
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private UserService userService;

    private ApiKeyPrincipal apiKey;

    @BeforeEach
    void hazirla() {
        UUID ownerId = userService.create(new CreateUserRequest("cihaz-" + UUID.randomUUID() + "@aft.local",
                "Kalkan-2026-Gizli!", "Cihaz Sahibi", "tr", Set.of(RoleCode.ADMIN))).id();
        UUID orgId = organizationService.create(
                new CreateOrganizationRequest("Cihaz Birimi " + UUID.randomUUID()), ownerId).id();
        apiKey = new ApiKeyPrincipal(UUID.randomUUID(), ownerId, orgId, "test");
    }

    @Test
    void kayitAyniAnahtarIcinIdempotenttir() {
        DeviceDto first = registryService.register(apiKey,
                new DeviceRegisterRequest("kule-01", "Windows 11", "1.0.0"));
        DeviceDto second = registryService.register(apiKey,
                new DeviceRegisterRequest("kule-01", "Windows 11", "1.1.0"));

        assertThat(second.id()).isEqualTo(first.id());
        assertThat(second.appVersion()).isEqualTo("1.1.0");
        assertThat(second.status()).isEqualTo("ONLINE");
    }

    @Test
    void yetenekBildirimiMevcutKumeyiDegistirir() {
        DeviceDto device = registryService.register(apiKey,
                new DeviceRegisterRequest("kule-02", "Ubuntu 24.04", "1.0.0"));

        registryService.replaceCapabilities(device.id(), apiKey, new CapabilityBulkRequest(List.of(
                new DeviceCapabilityDto("local_scenario_search", 1, true),
                new DeviceCapabilityDto("browser_command", 1, false))));
        DeviceDto updated = registryService.replaceCapabilities(device.id(), apiKey,
                new CapabilityBulkRequest(List.of(new DeviceCapabilityDto("local_run_history", 2, true))));

        assertThat(updated.capabilities()).singleElement()
                .satisfies(cap -> assertThat(cap.toolName()).isEqualTo("local_run_history"));
    }

    @Test
    void baskaAnahtarBaskaCihazaDokunamaz() {
        DeviceDto device = registryService.register(apiKey,
                new DeviceRegisterRequest("kule-03", "macOS 15", "1.0.0"));
        ApiKeyPrincipal foreign = new ApiKeyPrincipal(UUID.randomUUID(), apiKey.ownerId(), apiKey.orgId(), "yabanci");

        assertThatThrownBy(() -> registryService.replaceCapabilities(device.id(), foreign,
                new CapabilityBulkRequest(List.of())))
                .isInstanceOf(ForbiddenException.class);
    }
}

package com.aft.api.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.aft.api.security.dto.ApiKeyCreatedDto;
import com.aft.api.security.dto.CreateApiKeyRequest;
import com.aft.api.security.service.ApiKeyService;
import com.aft.api.support.AbstractIntegrationTest;
import com.aft.api.tenant.dto.CreateOrganizationRequest;
import com.aft.api.tenant.service.OrganizationService;
import com.aft.api.user.dto.CreateUserRequest;
import com.aft.api.user.entity.RoleCode;
import com.aft.api.user.service.UserService;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ApiKeyServiceTest extends AbstractIntegrationTest {
    @Autowired
    private ApiKeyService apiKeyService;
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private UserService userService;

    private UUID ownerId;
    private UUID orgId;

    @BeforeEach
    void hazirla() {
        ownerId = userService.create(new CreateUserRequest("anahtar-" + UUID.randomUUID() + "@aft.local",
                "Kalkan-2026-Gizli!", "Anahtar Sahibi", "tr", Set.of(RoleCode.ADMIN))).id();
        orgId = organizationService.create(
                new CreateOrganizationRequest("Test Birimi " + UUID.randomUUID()), ownerId).id();
    }

    @Test
    void uretilenAnahtarDogrulanirVeGizliDegerBirKezDoner() {
        ApiKeyCreatedDto created = apiKeyService.create(ownerId,
                new CreateApiKeyRequest("CI anahtari", orgId, Set.of("device"), null));

        assertThat(created.secret()).startsWith("aft_");
        assertThat(apiKeyService.authenticate(created.secret())).isPresent();

        assertThat(apiKeyService.list(orgId)).singleElement()
                .satisfies(dto -> assertThat(dto.name()).isEqualTo("CI anahtari"));
    }

    @Test
    void iptalEdilenAnahtarDogrulanmaz() {
        ApiKeyCreatedDto created = apiKeyService.create(ownerId,
                new CreateApiKeyRequest("Iptal edilecek", orgId, Set.of("device"), null));

        apiKeyService.revoke(created.key().id(), orgId, ownerId);

        assertThat(apiKeyService.authenticate(created.secret())).isEmpty();
    }

    @Test
    void bilinmeyenAnahtarDogrulanmaz() {
        assertThat(apiKeyService.authenticate("aft_gecersiz_deger")).isEmpty();
    }
}

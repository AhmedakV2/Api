package com.aft.api.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aft.api.common.exception.ValidationException;
import com.aft.api.support.AbstractIntegrationTest;
import com.aft.api.tenant.dto.CreateOrganizationRequest;
import com.aft.api.tenant.service.MembershipService;
import com.aft.api.tenant.service.OrganizationService;
import com.aft.api.user.dto.CreateUserRequest;
import com.aft.api.user.entity.RoleCode;
import com.aft.api.user.service.UserService;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MembershipServiceTest extends AbstractIntegrationTest {
    @Autowired
    private MembershipService membershipService;
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private UserService userService;

    private UUID ownerId;
    private UUID orgId;

    @BeforeEach
    void hazirla() {
        ownerId = createUser("sahip");
        orgId = organizationService.create(
                new CreateOrganizationRequest("Uyelik Birimi " + UUID.randomUUID()), ownerId).id();
    }

    @Test
    void kurucuOtomatikOlarakSahipUyeOlur() {
        assertThat(membershipService.listMembers(orgId)).singleElement()
                .satisfies(member -> {
                    assertThat(member.userId()).isEqualTo(ownerId);
                    assertThat(member.role()).isEqualTo("OWNER");
                });
    }

    @Test
    void uyeEklemeIkinciCagridaCoklamaz() {
        UUID memberId = createUser("uye");
        membershipService.addMember(orgId, memberId, RoleCode.USER);
        membershipService.addMember(orgId, memberId, RoleCode.USER);

        assertThat(membershipService.listMembers(orgId)).hasSize(2);
    }

    @Test
    void sonSahipCikarilamaz() {
        assertThatThrownBy(() -> membershipService.removeMember(orgId, ownerId, ownerId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("son sahibi");
    }

    @Test
    void siradanUyeCikarilabilir() {
        UUID memberId = createUser("gecici");
        membershipService.addMember(orgId, memberId, RoleCode.USER);

        membershipService.removeMember(orgId, memberId, ownerId);

        assertThat(membershipService.listMembers(orgId)).hasSize(1);
    }

    private UUID createUser(String prefix) {
        String username = prefix + UUID.randomUUID().toString().replace("-", "");
        return userService.create(new CreateUserRequest(username, username + "@aft.local",
                "Kalkan-2026-Gizli!", prefix, "tr", Set.of(RoleCode.USER))).id();
    }
}

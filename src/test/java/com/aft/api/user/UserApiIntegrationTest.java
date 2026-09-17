package com.aft.api.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.aft.api.support.AbstractIntegrationTest;
import com.aft.api.user.dto.CreateUserRequest;
import com.aft.api.user.dto.UserDto;
import com.aft.api.user.entity.RoleCode;
import com.aft.api.user.entity.UserStatus;
import com.aft.api.user.service.UserService;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

class UserApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void kullaniciOlusturulurVeAramadaGorunur() {
        CreateUserRequest request = new CreateUserRequest("entegrasyon", "entegrasyon@aft.local",
                "Kalkan-2026-Gizli!", "Entegrasyon Kullanicisi", "tr", Set.of(RoleCode.USER));

        UserDto created = userService.create(request);

        assertThat(created.id()).isNotNull();
        assertThat(created.roles()).containsExactly("USER");
        assertThat(userService.search("entegrasyon", UserStatus.ACTIVE, PageRequest.of(0, 10))
                .getTotalElements()).isEqualTo(1);
    }
}

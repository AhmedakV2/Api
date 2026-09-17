package com.aft.api.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.aft.api.common.exception.ApiException;
import com.aft.api.security.dto.LoginRequest;
import com.aft.api.security.dto.TokenResponse;
import com.aft.api.security.service.AuthService;
import com.aft.api.support.AbstractIntegrationTest;
import com.aft.api.user.dto.CreateUserRequest;
import com.aft.api.user.entity.RoleCode;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AuthFlowTest extends AbstractIntegrationTest {
    private static final String PASSWORD = "Kalkan-2026-Gizli!";

    @Autowired
    private AuthService authService;

    @Autowired
    private com.aft.api.user.service.UserService userService;

    @Autowired
    private LoginAttemptService loginAttemptService;

    private String username;

    @BeforeEach
    void kullaniciOlustur() {
        username = "akis" + UUID.randomUUID().toString().replace("-", "");
        userService.create(new CreateUserRequest(username, username + "@aft.local", PASSWORD,
                "Akis Testi", "tr", Set.of(RoleCode.USER)));
        loginAttemptService.reset(username);
    }

    @Test
    void girisErisimVeYenilemeJetonuDondurur() {
        TokenResponse response = authService.login(new LoginRequest(username, PASSWORD), "junit", "127.0.0.1");

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(900);
    }

    @Test
    void yenilemeJetonuTekKullanimliktir() {
        TokenResponse first = authService.login(new LoginRequest(username, PASSWORD), "junit", "127.0.0.1");
        TokenResponse second = authService.refresh(first.refreshToken(), "junit", "127.0.0.1");

        assertThat(second.refreshToken()).isNotEqualTo(first.refreshToken());
        assertThatThrownBy(() -> authService.refresh(first.refreshToken(), "junit", "127.0.0.1"))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void jetonYenidenKullanildigindaAileninTamamiIptalEdilir() {
        TokenResponse first = authService.login(new LoginRequest(username, PASSWORD), "junit", "127.0.0.1");
        TokenResponse second = authService.refresh(first.refreshToken(), "junit", "127.0.0.1");

        assertThatThrownBy(() -> authService.refresh(first.refreshToken(), "junit", "127.0.0.1"))
                .isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> authService.refresh(second.refreshToken(), "junit", "127.0.0.1"))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void hataliParolaKimlikDogrulamaHatasiVerir() {
        assertThatThrownBy(() -> authService.login(new LoginRequest(username, "Yanlis-Parola-123!"),
                "junit", "127.0.0.1"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Kullanici adi veya parola hatali");
    }

    @Test
    void cikistanSonraYenilemeReddedilir() {
        TokenResponse response = authService.login(new LoginRequest(username, PASSWORD), "junit", "127.0.0.1");
        authService.logout(response.refreshToken(), null);

        assertThatThrownBy(() -> authService.refresh(response.refreshToken(), "junit", "127.0.0.1"))
                .isInstanceOf(ApiException.class);
    }
}

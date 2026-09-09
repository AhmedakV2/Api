package com.aft.api.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aft.api.common.exception.ValidationException;
import com.aft.api.user.entity.PasswordHistory;
import com.aft.api.user.repository.PasswordHistoryRepository;
import com.aft.api.user.service.PasswordPolicyService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Limit;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PasswordPolicyTest {

    @Mock
    private PasswordHistoryRepository historyRepository;

    private PasswordEncoder passwordEncoder;
    private PasswordPolicyService policy;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(4);
        policy = new PasswordPolicyService(historyRepository, passwordEncoder);
    }

    @Test
    void kisaParolaReddedilir() {
        assertThatThrownBy(() -> policy.validateFormat("Kisa1!", "ahmet@aft.local"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("12");
    }

    @Test
    void karisimEksikParolaReddedilir() {
        assertThatThrownBy(() -> policy.validateFormat("yalnizcakucukharf", "ahmet@aft.local"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void epostaIcerenParolaReddedilir() {
        assertThatThrownBy(() -> policy.validateFormat("Ahmet-2026-Gizli!", "ahmet@aft.local"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("e-posta");
    }

    @Test
    void gecerliParolaKabulEdilir() {
        assertThatCode(() -> policy.validateFormat("Kalkan-2026-Gizli!", "ahmet@aft.local"))
                .doesNotThrowAnyException();
    }

    @Test
    void sonBesParolaTekrariReddedilir() {
        UUID userId = UUID.randomUUID();
        String raw = "Kalkan-2026-Gizli!";
        when(historyRepository.findByUserIdOrderByCreatedAtDesc(userId, Limit.of(5)))
                .thenReturn(List.of(new PasswordHistory(userId, passwordEncoder.encode(raw))));

        assertThatThrownBy(() -> policy.validateChange(userId, raw, "ahmet@aft.local"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("tekrari");
    }

    @Test
    void yeniOzetGecmiseYazilirVeEskilerBudanir() {
        UUID userId = UUID.randomUUID();
        policy.remember(userId, "$2a$04$ornekozet");

        verify(historyRepository).save(any(PasswordHistory.class));
        verify(historyRepository).deleteOlderThan(userId, 5);
        assertThat(PasswordPolicyService.HISTORY_DEPTH).isEqualTo(5);
    }
}

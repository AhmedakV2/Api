package com.aft.api.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.aft.api.config.SecurityProperties;
import com.aft.api.support.AbstractIntegrationTest;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class LoginAttemptServiceTest extends AbstractIntegrationTest {
    private static final String EMAIL = "kilit@aft.local";

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Autowired
    private SecurityProperties properties;

    @BeforeEach
    void temizle() {
        loginAttemptService.reset(EMAIL);
    }

    @Test
    void besinciDenemedenSonraHesapKilitlenir() {
        int max = properties.lockout().maxAttempts();
        for (int i = 0; i < max - 1; i++) {
            loginAttemptService.recordFailure(EMAIL);
        }
        assertThat(loginAttemptService.isLocked(EMAIL)).isFalse();

        loginAttemptService.recordFailure(EMAIL);
        assertThat(loginAttemptService.isLocked(EMAIL)).isTrue();
    }

    @Test
    void basariliGirisSayaciSifirlar() {
        loginAttemptService.recordFailure(EMAIL);
        loginAttemptService.reset(EMAIL);

        assertThat(loginAttemptService.isLocked(EMAIL)).isFalse();
        assertThat(loginAttemptService.remainingLock(EMAIL)).isEqualTo(Duration.ZERO);
    }

    @Test
    void kilitSuresiIlkBasarisizDenemedeBaslar() {
        loginAttemptService.recordFailure(EMAIL);
        Duration first = loginAttemptService.remainingLock(EMAIL);

        loginAttemptService.recordFailure(EMAIL);

        assertThat(loginAttemptService.remainingLock(EMAIL)).isLessThanOrEqualTo(first);
    }
}

package com.aft.api.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.aft.api.config.JwtProperties;
import com.aft.api.user.entity.UserStatus;
import io.jsonwebtoken.Claims;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class JwtTokenProviderTest {
    private static final String SECRET = "cok-gizli-test-anahtari-en-az-otuz-iki-karakter";

    private final JwtTokenProvider provider = new JwtTokenProvider(
            new JwtProperties(SECRET, Duration.ofMinutes(15), Duration.ofDays(30), "aft-api"));

    private AftPrincipal principal(UUID userId, UUID orgId) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        return new AftPrincipal(userId, "ahmet@aft.local", null, UserStatus.ACTIVE, Set.of(orgId), authorities);
    }

    @Test
    void jetonKimlikRolVeKapsamiTasir() {
        UUID userId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();

        Claims claims = provider.parse(provider.createAccessToken(principal(userId, orgId)));

        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.get("email", String.class)).isEqualTo("ahmet@aft.local");
        assertThat(claims.get("roles", List.class)).containsExactly("ROLE_ADMIN");
        assertThat(claims.get("orgs", List.class)).containsExactly(orgId.toString());
    }

    @Test
    void baskaAnahtarlaImzalananJetonReddedilir() {
        String token = provider.createAccessToken(principal(UUID.randomUUID(), UUID.randomUUID()));
        JwtTokenProvider other = new JwtTokenProvider(
                new JwtProperties("bambaska-bir-anahtar-en-az-otuz-iki-karakter", null, null, "aft-api"));

        assertThat(other.parse(token)).isNull();
    }

    @Test
    void bozulmusJetonReddedilir() {
        String token = provider.createAccessToken(principal(UUID.randomUUID(), UUID.randomUUID()));

        assertThat(provider.parse(token.substring(0, token.length() - 3) + "abc")).isNull();
    }

    @Test
    void suresiDolmusJetonReddedilir() {
        JwtTokenProvider shortLived = new JwtTokenProvider(
                new JwtProperties(SECRET, Duration.ofSeconds(-1), null, "aft-api"));

        assertThat(shortLived.parse(shortLived.createAccessToken(
                principal(UUID.randomUUID(), UUID.randomUUID())))).isNull();
    }
}

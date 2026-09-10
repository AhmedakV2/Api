package com.aft.api.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TokenHashingTest {
    @Test
    void uretilenGizliDegerlerBenzersizdir() {
        assertThat(TokenHashing.randomSecret()).isNotEqualTo(TokenHashing.randomSecret());
    }

    @Test
    void ozetSabitUzunluktaVeKararlidir() {
        String hash = TokenHashing.sha256Hex("aft_ornek_anahtar");

        assertThat(hash).hasSize(64);
        assertThat(TokenHashing.sha256Hex("aft_ornek_anahtar")).isEqualTo(hash);
    }

    @Test
    void dogruDegerEslesirYanlisDegerEslesmez() {
        String raw = TokenHashing.randomSecret();
        String hash = TokenHashing.sha256Hex(raw);

        assertThat(TokenHashing.matches(raw, hash)).isTrue();
        assertThat(TokenHashing.matches(raw + "x", hash)).isFalse();
    }
}

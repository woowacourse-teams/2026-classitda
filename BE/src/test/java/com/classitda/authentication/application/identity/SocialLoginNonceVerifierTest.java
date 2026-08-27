package com.classitda.authentication.application.identity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SocialLoginNonceVerifierTest {

    private static final String RAW_NONCE = "A".repeat(43);
    private static final String HASHED_NONCE = "0f007385b6f9d4b7eeb2748605afe1a984a0a3bfa3f014d09e2a784ce9e5cd1a";

    private final SocialLoginNonceVerifier verifier = new SocialLoginNonceVerifier();

    @Test
    void rawNonce의_SHA_256_해시와_토큰_nonce가_일치하면_검증에_성공한다() {
        // given / when
        boolean matches = verifier.matches(RAW_NONCE, HASHED_NONCE);

        // then
        assertThat(matches).isTrue();
    }

    @Test
    void rawNonce의_SHA_256_해시와_토큰_nonce가_다르면_검증에_실패한다() {
        // given
        String differentHashedNonce = "a".repeat(64);

        // when
        boolean matches = verifier.matches(RAW_NONCE, differentHashedNonce);

        // then
        assertThat(matches).isFalse();
    }

    @Test
    void rawNonce나_토큰_nonce가_없으면_검증에_실패한다() {
        // given / when / then
        assertThat(verifier.matches(null, HASHED_NONCE)).isFalse();
        assertThat(verifier.matches(RAW_NONCE, null)).isFalse();
        assertThat(verifier.matches(RAW_NONCE, " ")).isFalse();
    }

    @Test
    void 토큰_nonce가_소문자_16진수_64자리가_아니면_검증에_실패한다() {
        // given
        String[] invalidNonceClaims = {
                HASHED_NONCE.toUpperCase(),
                "a".repeat(63),
                "a".repeat(65),
                "g".repeat(64)
        };

        // when / then
        for (String invalidNonceClaim : invalidNonceClaims) {
            assertThat(verifier.matches(RAW_NONCE, invalidNonceClaim)).isFalse();
        }
    }
}

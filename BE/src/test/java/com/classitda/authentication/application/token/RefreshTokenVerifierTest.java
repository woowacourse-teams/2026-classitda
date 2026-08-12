package com.classitda.authentication.application.token;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class RefreshTokenVerifierTest {

    private static final String VALID_TOKEN =
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA.BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB";
    private static final String VALID_HASH =
            "0dbe9f51d54189a8a6988a54182f17476b5e80fc720e735e0abb24807ad87bf9";

    private final RefreshTokenVerifier refreshTokenVerifier = new RefreshTokenVerifier();

    @Test
    void 정확한_opaque_토큰에서_session_ID를_추출하고_저장된_SHA_256_해시를_검증한다() {
        // given / when
        String sessionId = refreshTokenVerifier.extractSessionId(VALID_TOKEN);

        // then
        assertThat(sessionId).isEqualTo("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        assertThat(refreshTokenVerifier.matches(VALID_TOKEN, VALID_HASH)).isTrue();
        assertThat(refreshTokenVerifier.matches(VALID_TOKEN, "f".repeat(64))).isFalse();
    }

    @Test
    void null과_공백과_segment_수_길이_문자_전체길이_위반을_모두_거부한다() {
        // given
        List<String> invalidTokens = java.util.Arrays.asList(
                null,
                "",
                " ",
                "A".repeat(43),
                "A".repeat(43) + ".",
                "." + "B".repeat(43),
                "A".repeat(43) + "." + "B".repeat(43) + "." + "C".repeat(43),
                "A".repeat(42) + "." + "B".repeat(43),
                "A".repeat(44) + "." + "B".repeat(43),
                "A".repeat(43) + "." + "B".repeat(42),
                "A".repeat(43) + "." + "B".repeat(44),
                "+" + "A".repeat(42) + "." + "B".repeat(43),
                "A".repeat(43) + "." + "B".repeat(43) + "C"
        );

        // when / then
        for (String invalidToken : invalidTokens) {
            assertThatThrownBy(() -> refreshTokenVerifier.extractSessionId(invalidToken))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("리프레시 토큰 형식이 올바르지 않습니다.");
            assertThatThrownBy(() -> refreshTokenVerifier.matches(invalidToken, VALID_HASH))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("리프레시 토큰 형식이 올바르지 않습니다.");
        }
    }

    @Test
    void 저장된_해시의_형식이_잘못되면_비교를_거부한다() {
        // given / when / then
        assertThatThrownBy(() -> refreshTokenVerifier.matches(VALID_TOKEN, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> refreshTokenVerifier.matches(VALID_TOKEN, "A".repeat(64)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> refreshTokenVerifier.matches(VALID_TOKEN, "a".repeat(63)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

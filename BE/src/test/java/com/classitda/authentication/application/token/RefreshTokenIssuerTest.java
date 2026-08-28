package com.classitda.authentication.application.token;

import static org.assertj.core.api.Assertions.assertThat;

import com.classitda.authentication.application.token.result.IssuedRefreshToken;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;

class RefreshTokenIssuerTest {

    private final RefreshTokenIssuer refreshTokenIssuer = new RefreshTokenIssuer();

    @Test
    void 발급한_토큰은_각_43자_segment의_URL_safe_형식이고_원문_SHA_256_해시를_가진다() throws Exception {
        // given / when
        IssuedRefreshToken first = refreshTokenIssuer.issue();
        IssuedRefreshToken second = refreshTokenIssuer.issue();

        // then
        assertThat(first.refreshToken()).matches("^[A-Za-z0-9_-]{43}\\.[A-Za-z0-9_-]{43}$");
        assertThat(first.sessionId())
                .hasSize(43)
                .isEqualTo(first.refreshToken().substring(0, 43));
        assertThat(first.tokenHash())
                .matches("^[0-9a-f]{64}$")
                .isEqualTo(sha256(first.refreshToken()));
        assertThat(second.refreshToken()).isNotEqualTo(first.refreshToken());
        assertThat(second.sessionId()).isNotEqualTo(first.sessionId());
    }

    private String sha256(String value) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(messageDigest.digest(value.getBytes(StandardCharsets.UTF_8)));
    }
}

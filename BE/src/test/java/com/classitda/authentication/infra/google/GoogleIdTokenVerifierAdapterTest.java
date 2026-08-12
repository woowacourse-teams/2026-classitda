package com.classitda.authentication.infra.google;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.authentication.exception.AuthErrorCode;
import com.classitda.authentication.exception.AuthException;
import org.junit.jupiter.api.Test;

class GoogleIdTokenVerifierAdapterTest {

    private final GoogleIdTokenVerifierAdapter verifier =
            new GoogleIdTokenVerifierAdapter("test-web-client-id");

    @Test
    void 빈_Google_ID_토큰은_AUTH_006으로_거부한다() {
        // given / when / then
        assertThatThrownBy(() -> verifier.verify(" "))
                .isInstanceOf(AuthException.class)
                .extracting(exception -> ((AuthException) exception).getErrorCode())
                .isEqualTo(AuthErrorCode.GOOGLE_ID_TOKEN_INVALID);
    }

    @Test
    void 형식이_잘못된_Google_ID_토큰은_AUTH_006으로_거부한다() {
        // given / when / then
        assertThatThrownBy(() -> verifier.verify("malformed-token"))
                .isInstanceOf(AuthException.class)
                .extracting(exception -> ((AuthException) exception).getErrorCode())
                .isEqualTo(AuthErrorCode.GOOGLE_ID_TOKEN_INVALID);
    }
}

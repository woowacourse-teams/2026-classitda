package com.classitda.authentication.application.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.authentication.exception.AuthErrorCode;
import com.classitda.authentication.exception.AuthException;
import org.junit.jupiter.api.Test;

class GoogleIdentityTest {

    @Test
    void provider_subject는_255자까지_허용한다() {
        // given
        String providerSubject = "a".repeat(255);

        // when
        GoogleIdentity identity = GoogleIdentity.of(providerSubject, "member@example.com");

        // then
        assertThat(identity.providerSubject()).isEqualTo(providerSubject);
    }

    @Test
    void provider_subject가_255자를_초과하면_AUTH_006과_401로_거부한다() {
        // given
        String providerSubject = "a".repeat(256);

        // when / then
        assertThatThrownBy(() -> GoogleIdentity.of(providerSubject, "member@example.com"))
                .isInstanceOf(AuthException.class)
                .satisfies(exception -> {
                    AuthErrorCode errorCode = (AuthErrorCode) ((AuthException) exception).getErrorCode();
                    assertThat(errorCode).isEqualTo(AuthErrorCode.GOOGLE_ID_TOKEN_INVALID);
                    assertThat(errorCode.getStatus().value()).isEqualTo(401);
                });
    }
}

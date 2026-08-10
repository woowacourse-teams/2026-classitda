package com.classitda.authentication.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.authentication.exception.AuthErrorCode;
import com.classitda.authentication.exception.AuthException;
import com.classitda.authentication.fixture.AuthAccountFixture;
import org.junit.jupiter.api.Test;

class AuthAccountTest {

    @Test
    void 제공자_이메일은_null을_허용한다() {
        // given / when
        AuthAccount authAccount = AuthAccountFixture.이메일이_없는_인증_계정();

        // then
        assertThat(authAccount.getProviderEmail()).isNull();
    }

    @Test
    void 빈_제공자_이메일은_거부한다() {
        // given / when / then
        assertThatThrownBy(() -> AuthAccountFixture.이메일이_있는_인증_계정(" "))
                .isInstanceOf(AuthException.class)
                .extracting(exception -> ((AuthException) exception).getErrorCode())
                .isEqualTo(AuthErrorCode.AUTH_ACCOUNT_PROVIDER_EMAIL_INVALID);
    }

    @Test
    void 제공자_이메일이_254자를_초과하면_거부한다() {
        // given
        String tooLongEmail = "a".repeat(255);

        // when / then
        assertThatThrownBy(() -> AuthAccountFixture.이메일이_있는_인증_계정(tooLongEmail))
                .isInstanceOf(AuthException.class)
                .extracting(exception -> ((AuthException) exception).getErrorCode())
                .isEqualTo(AuthErrorCode.AUTH_ACCOUNT_PROVIDER_EMAIL_INVALID);
    }

    @Test
    void 제공자_이메일을_254자로_수정할_수_있다() {
        // given
        AuthAccount authAccount = AuthAccountFixture.기본_인증_계정();
        String providerEmail = "a".repeat(254);

        // when
        authAccount.updateProviderEmail(providerEmail);

        // then
        assertThat(authAccount.getProviderEmail()).isEqualTo(providerEmail);
    }
}

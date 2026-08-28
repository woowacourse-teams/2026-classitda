package com.classitda.authentication.application.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.authentication.domain.OauthProvider;
import org.junit.jupiter.api.Test;

class SignupSessionTest {

    @Test
    void 유효한_가입_세션은_서버_소셜_신원을_그대로_보존한다() {
        // given
        String providerSubject = "a".repeat(255);
        String providerEmail = "b".repeat(254);

        // when
        SignupSession session = new SignupSession(
                OauthProvider.GOOGLE,
                providerSubject,
                providerEmail
        );
        SignupSession sessionWithoutEmail = new SignupSession(
                OauthProvider.GOOGLE,
                "provider-subject",
                null
        );

        // then
        assertThat(session.provider()).isEqualTo(OauthProvider.GOOGLE);
        assertThat(session.providerSubject()).isEqualTo(providerSubject);
        assertThat(session.providerEmail()).isEqualTo(providerEmail);
        assertThat(sessionWithoutEmail.providerEmail()).isNull();
    }

    @Test
    void provider나_provider_subject가_유효하지_않으면_고정_오류로_거부한다() {
        // given / when / then
        assertInvalidSession(() -> new SignupSession(null, "provider-subject", "member@example.com"));
        assertInvalidSession(() -> new SignupSession(OauthProvider.GOOGLE, null, "member@example.com"));
        assertInvalidSession(() -> new SignupSession(OauthProvider.GOOGLE, " ", "member@example.com"));
        assertInvalidSession(() -> new SignupSession(
                OauthProvider.GOOGLE,
                "a".repeat(256),
                "member@example.com"
        ));
    }

    @Test
    void provider_email이_비어있거나_254자를_초과하면_고정_오류로_거부한다() {
        // given / when / then
        assertInvalidSession(() -> new SignupSession(OauthProvider.GOOGLE, "provider-subject", " "));
        assertInvalidSession(() -> new SignupSession(
                OauthProvider.GOOGLE,
                "provider-subject",
                "a".repeat(255)
        ));
    }

    private void assertInvalidSession(Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("가입 세션이 올바르지 않습니다.")
                .hasNoCause();
    }
}

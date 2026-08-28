package com.classitda.authentication.application.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.authentication.domain.OauthProvider;
import org.junit.jupiter.api.Test;

class SocialIdentityTest {

    @Test
    void 제공자와_사용자_식별자와_이메일을_보관한다() {
        // given / when
        SocialIdentity identity = SocialIdentity.of(
                OauthProvider.GOOGLE,
                "provider-subject",
                "member@example.com");

        // then
        assertThat(identity.provider()).isEqualTo(OauthProvider.GOOGLE);
        assertThat(identity.providerSubject()).isEqualTo("provider-subject");
        assertThat(identity.providerEmail()).isEqualTo("member@example.com");
    }

    @Test
    void provider_subject는_255자까지_허용한다() {
        // given
        String providerSubject = "a".repeat(255);

        // when
        SocialIdentity identity = SocialIdentity.of(OauthProvider.GOOGLE, providerSubject, null);

        // then
        assertThat(identity.providerSubject()).isEqualTo(providerSubject);
        assertThat(identity.providerEmail()).isNull();
    }

    @Test
    void provider_subject가_255자를_초과하면_거부한다() {
        // given
        String providerSubject = "a".repeat(256);

        // when / then
        assertThatThrownBy(() -> SocialIdentity.of(OauthProvider.GOOGLE, providerSubject, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("OAuth 제공자 사용자 식별자는 255자 이하여야 합니다.");
    }

    @Test
    void provider가_없으면_거부한다() {
        // given / when / then
        assertThatThrownBy(() -> SocialIdentity.of(null, "provider-subject", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("OAuth 제공자는 필수입니다.");
    }

    @Test
    void 빈_provider_email은_거부한다() {
        // given / when / then
        assertThatThrownBy(() -> SocialIdentity.of(OauthProvider.GOOGLE, "provider-subject", " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("OAuth 제공자 이메일은 254자 이하여야 합니다.");
    }

    @Test
    void provider_email이_254자를_초과하면_거부한다() {
        // given
        String providerEmail = "a".repeat(255);

        // when / then
        assertThatThrownBy(() -> SocialIdentity.of(OauthProvider.GOOGLE, "provider-subject", providerEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("OAuth 제공자 이메일은 254자 이하여야 합니다.");
    }
}

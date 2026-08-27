package com.classitda.authentication.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.classitda.authentication.application.identity.GoogleIdentity;
import com.classitda.authentication.application.identity.GoogleIdentityVerifier;
import com.classitda.authentication.application.token.LoginTokenIssuer;
import com.classitda.authentication.application.token.SignupTokenIssuer;
import com.classitda.authentication.application.token.result.IssuedLoginTokens;
import com.classitda.authentication.application.token.result.IssuedSignupToken;
import com.classitda.authentication.domain.AuthAccount;
import com.classitda.authentication.domain.OauthProvider;
import com.classitda.authentication.domain.repository.AuthAccountRepository;
import com.classitda.authentication.exception.AuthErrorCode;
import com.classitda.authentication.exception.AuthException;
import com.classitda.authentication.fixture.AuthAccountFixture;
import com.classitda.authentication.presentation.dto.login.GoogleLoginRequest;
import com.classitda.authentication.presentation.dto.login.LoginResponse;
import com.classitda.authentication.presentation.dto.login.RegisteredLoginResponse;
import com.classitda.authentication.presentation.dto.login.RegistrationRequiredLoginResponse;
import com.classitda.member.domain.Member;
import com.classitda.member.fixture.MemberFixture;
import com.classitda.support.MySqlDataJpaTest;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Import(SocialLoginService.class)
@MySqlDataJpaTest
class SocialLoginServiceTest {

    private static final String ID_TOKEN = "google-id-token";
    private static final String PROVIDER_SUBJECT = "google-subject";
    private static final String PROVIDER_EMAIL = "member@example.com";

    private final SocialLoginService socialLoginService;
    private final AuthAccountRepository authAccountRepository;
    private final EntityManager entityManager;

    @MockitoBean
    private GoogleIdentityVerifier googleIdentityVerifier;

    @MockitoBean
    private SignupTokenIssuer signupTokenIssuer;

    @MockitoBean
    private LoginTokenIssuer loginTokenIssuer;

    @Autowired
    SocialLoginServiceTest(
            SocialLoginService socialLoginService,
            AuthAccountRepository authAccountRepository,
            EntityManager entityManager
    ) {
        this.socialLoginService = socialLoginService;
        this.authAccountRepository = authAccountRepository;
        this.entityManager = entityManager;
    }

    @Test
    void 구글_계정이_미가입_상태면_가입_토큰을_반환한다() {
        // given
        GoogleIdentity identity = GoogleIdentity.of(PROVIDER_SUBJECT, PROVIDER_EMAIL);
        IssuedSignupToken issuedSignupToken = IssuedSignupToken.of("signup-token", 1800L);
        given(googleIdentityVerifier.verify(ID_TOKEN)).willReturn(identity);
        given(signupTokenIssuer.issueSignupToken(OauthProvider.GOOGLE, PROVIDER_SUBJECT, PROVIDER_EMAIL))
                .willReturn(issuedSignupToken);

        // when
        LoginResponse response = socialLoginService.loginWithGoogle(GoogleLoginRequest.from(ID_TOKEN));

        // then
        assertThat(response).isInstanceOfSatisfying(RegistrationRequiredLoginResponse.class, registrationRequired -> {
            assertThat(registrationRequired.status()).isEqualTo(LoginResponse.LoginStatus.REGISTRATION_REQUIRED);
            assertThat(registrationRequired.signupToken()).isEqualTo("signup-token");
            assertThat(registrationRequired.signupTokenExpiresIn()).isEqualTo(1800L);
        });
        verify(googleIdentityVerifier).verify(ID_TOKEN);
        verify(signupTokenIssuer).issueSignupToken(OauthProvider.GOOGLE, PROVIDER_SUBJECT, PROVIDER_EMAIL);
        verify(loginTokenIssuer, never()).issueLoginTokens(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void 기존_계정의_이메일이_동일하면_값을_유지하고_로그인_토큰을_반환한다() {
        // given
        AuthAccount authAccount = saveAuthAccount(PROVIDER_EMAIL);
        GoogleIdentity identity = GoogleIdentity.of(PROVIDER_SUBJECT, PROVIDER_EMAIL);
        IssuedLoginTokens issuedLoginTokens = IssuedLoginTokens.of("access-token", 3_600L, "refresh-token", 2592000L);
        given(googleIdentityVerifier.verify(ID_TOKEN)).willReturn(identity);
        given(loginTokenIssuer.issueLoginTokens(authAccount.getMemberId())).willReturn(issuedLoginTokens);

        // when
        LoginResponse response = socialLoginService.loginWithGoogle(GoogleLoginRequest.from(ID_TOKEN));
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(response).isInstanceOfSatisfying(RegisteredLoginResponse.class, registered -> {
            assertThat(registered.status()).isEqualTo(LoginResponse.LoginStatus.REGISTERED);
            assertThat(registered.accessToken()).isEqualTo("access-token");
            assertThat(registered.accessTokenExpiresIn()).isEqualTo(3_600L);
            assertThat(registered.refreshToken()).isEqualTo("refresh-token");
            assertThat(registered.refreshTokenExpiresIn()).isEqualTo(2592000L);
        });
        AuthAccount persistedAccount = findAuthAccount();
        assertThat(persistedAccount.getProviderEmail()).isEqualTo(PROVIDER_EMAIL);
        verify(signupTokenIssuer, never()).issueSignupToken(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void 기존_계정의_이메일이_다르면_갱신한_값을_저장한다() {
        // given
        AuthAccount authAccount = saveAuthAccount(PROVIDER_EMAIL);
        GoogleIdentity identity = GoogleIdentity.of(PROVIDER_SUBJECT, "changed@example.com");
        IssuedLoginTokens issuedLoginTokens = IssuedLoginTokens.of("access-token", 3_600L, "refresh-token", 2592000L);
        given(googleIdentityVerifier.verify(ID_TOKEN)).willReturn(identity);
        given(loginTokenIssuer.issueLoginTokens(authAccount.getMemberId())).willReturn(issuedLoginTokens);

        // when
        socialLoginService.loginWithGoogle(GoogleLoginRequest.from(ID_TOKEN));
        entityManager.flush();
        entityManager.clear();

        // then
        AuthAccount persistedAccount = findAuthAccount();
        assertThat(persistedAccount.getProviderEmail()).isEqualTo("changed@example.com");
    }

    @Test
    void 탈퇴_처리_중인_회원은_구글_로그인과_이메일_갱신이_차단된다() {
        // given
        Member member = MemberFixture.기본_회원();
        member.withdraw(LocalDateTime.of(2026, 8, 24, 15, 30));
        entityManager.persist(member);
        entityManager.flush();
        authAccountRepository.saveAndFlush(AuthAccountFixture.인증_계정(
                member.getId(),
                PROVIDER_SUBJECT,
                PROVIDER_EMAIL));
        GoogleIdentity identity = GoogleIdentity.of(PROVIDER_SUBJECT, "changed@example.com");
        given(googleIdentityVerifier.verify(ID_TOKEN)).willReturn(identity);

        // when / then
        assertThatThrownBy(() -> socialLoginService.loginWithGoogle(GoogleLoginRequest.from(ID_TOKEN)))
                .isInstanceOf(AuthException.class)
                .extracting(exception -> ((AuthException) exception).getErrorCode())
                .isEqualTo(AuthErrorCode.MEMBER_WITHDRAWAL_PENDING);
        entityManager.clear();
        assertThat(findAuthAccount().getProviderEmail()).isEqualTo(PROVIDER_EMAIL);
        verify(loginTokenIssuer, never()).issueLoginTokens(member.getId());
        verify(signupTokenIssuer, never()).issueSignupToken(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    private AuthAccount saveAuthAccount(String providerEmail) {
        Member member = MemberFixture.기본_회원();
        entityManager.persist(member);
        entityManager.flush();

        AuthAccount authAccount = AuthAccountFixture.인증_계정(
                member.getId(),
                PROVIDER_SUBJECT,
                providerEmail);
        return authAccountRepository.saveAndFlush(authAccount);
    }

    private AuthAccount findAuthAccount() {
        return authAccountRepository.findByProviderAndProviderSubject(OauthProvider.GOOGLE, PROVIDER_SUBJECT)
                .orElseThrow();
    }
}

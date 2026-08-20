package com.classitda.authentication.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.classitda.authentication.application.phone.PhoneVerificationStore;
import com.classitda.authentication.application.session.SignupSession;
import com.classitda.authentication.application.session.SignupSessionStore;
import com.classitda.authentication.application.token.LoginTokenIssuer;
import com.classitda.authentication.application.token.result.IssuedLoginTokens;
import com.classitda.authentication.domain.AuthAccount;
import com.classitda.authentication.domain.OauthProvider;
import com.classitda.authentication.domain.repository.AuthAccountRepository;
import com.classitda.authentication.exception.AuthErrorCode;
import com.classitda.authentication.exception.AuthException;
import com.classitda.authentication.presentation.dto.signup.SignupRequest;
import com.classitda.authentication.presentation.dto.signup.SignupResponse;
import com.classitda.member.domain.repository.MemberRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class SignupServiceTest {

    private static final String SIGNUP_JTI = "signup-jti-sensitive";
    private static final String PROVIDER_SUBJECT = "provider-subject-sensitive";
    private static final String PROVIDER_EMAIL = "provider-email-sensitive@example.com";
    private static final String PHONE_NUMBER = "01012345678";
    private static final SignupRequest REQUEST = SignupRequest.of("민감한회원이름", List.of(1L, 2L));
    private static final SignupSession SESSION = new SignupSession(
            OauthProvider.GOOGLE,
            PROVIDER_SUBJECT,
            PROVIDER_EMAIL
    );

    @Mock
    private SignupSessionStore signupSessionStore;

    @Mock
    private PhoneVerificationStore phoneVerificationStore;

    @Mock
    private SignupAccountCreator signupAccountCreator;

    @Mock
    private AuthAccountRepository authAccountRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private LoginTokenIssuer loginTokenIssuer;

    @InjectMocks
    private SignupService signupService;

    @Test
    void 가입_세션이_없거나_만료되면_AUTH_001이고_후속_작업을_하지_않는다() {
        // given
        given(signupSessionStore.findBySignupJti(SIGNUP_JTI)).willReturn(Optional.empty());

        // when / then
        assertAuthError(
                () -> signupService.complete(SIGNUP_JTI, REQUEST),
                AuthErrorCode.AUTHENTICATION_REQUIRED
        );
        verifyNoInteractions(phoneVerificationStore, signupAccountCreator, loginTokenIssuer);
        verify(signupSessionStore, never()).deleteBySignupJti(SIGNUP_JTI);
    }

    @Test
    void 인증_완료_전화번호가_없거나_만료되면_PHONE_008이고_가입_상태를_소비하지_않는다() {
        // given
        given(signupSessionStore.findBySignupJti(SIGNUP_JTI)).willReturn(Optional.of(SESSION));
        given(phoneVerificationStore.findVerifiedPhoneNumber(SIGNUP_JTI)).willReturn(Optional.empty());

        // when / then
        assertAuthError(
                () -> signupService.complete(SIGNUP_JTI, REQUEST),
                AuthErrorCode.VERIFIED_PHONE_UNAVAILABLE
        );
        verifyNoInteractions(signupAccountCreator, loginTokenIssuer);
        verify(signupSessionStore, never()).deleteBySignupJti(SIGNUP_JTI);
        verify(phoneVerificationStore, never()).deleteVerifiedPhoneNumber(SIGNUP_JTI);
    }

    @Test
    void 동일한_소셜_계정이_이미_있으면_전화번호_인증과_계정_생성을_건너뛰고_로그인에_성공한다() {
        // given
        AuthAccount existingAccount = existingAccount(42L);
        given(signupSessionStore.findBySignupJti(SIGNUP_JTI)).willReturn(Optional.of(SESSION));
        given(authAccountRepository.findByProviderAndProviderSubject(OauthProvider.GOOGLE, PROVIDER_SUBJECT))
                .willReturn(Optional.of(existingAccount));
        given(loginTokenIssuer.issueLoginTokens(42L)).willReturn(issuedTokens());

        // when
        SignupResponse response = signupService.complete(SIGNUP_JTI, REQUEST);

        // then
        assertThat(response).isEqualTo(SignupResponse.from(issuedTokens()));
        verify(phoneVerificationStore, never()).findVerifiedPhoneNumber(SIGNUP_JTI);
        verifyNoInteractions(signupAccountCreator, memberRepository);
        verify(signupSessionStore).deleteBySignupJti(SIGNUP_JTI);
        verify(phoneVerificationStore).deleteVerifiedPhoneNumber(SIGNUP_JTI);
    }

    @Test
    void 서버_상태를_정확히_전달하고_토큰_발급_후_가입_세션과_전화번호를_순서대로_정리한다() {
        // given
        givenServerState();
        given(signupAccountCreator.create(REQUEST, SESSION, PHONE_NUMBER)).willReturn(42L);
        given(loginTokenIssuer.issueLoginTokens(42L)).willReturn(issuedTokens());

        // when
        SignupResponse response = signupService.complete(SIGNUP_JTI, REQUEST);

        // then
        assertThat(response).isEqualTo(SignupResponse.from(issuedTokens()));
        InOrder order = inOrder(
                signupSessionStore,
                phoneVerificationStore,
                signupAccountCreator,
                loginTokenIssuer
        );
        order.verify(signupSessionStore).findBySignupJti(SIGNUP_JTI);
        order.verify(phoneVerificationStore).findVerifiedPhoneNumber(SIGNUP_JTI);
        order.verify(signupAccountCreator).create(REQUEST, SESSION, PHONE_NUMBER);
        order.verify(loginTokenIssuer).issueLoginTokens(42L);
        order.verify(signupSessionStore).deleteBySignupJti(SIGNUP_JTI);
        order.verify(phoneVerificationStore).deleteVerifiedPhoneNumber(SIGNUP_JTI);
    }

    @Test
    void 생성_rollback_후_동일한_소셜_계정이_확인되면_전화번호보다_우선해_로그인에_성공한다() {
        // given
        givenServerState();
        AuthAccount existingAccount = existingAccount(42L);
        given(authAccountRepository.findByProviderAndProviderSubject(OauthProvider.GOOGLE, PROVIDER_SUBJECT))
                .willReturn(Optional.empty(), Optional.of(existingAccount));
        given(signupAccountCreator.create(REQUEST, SESSION, PHONE_NUMBER))
                .willThrow(dataIntegrityViolation());
        given(loginTokenIssuer.issueLoginTokens(42L)).willReturn(issuedTokens());

        // when
        SignupResponse response = signupService.complete(SIGNUP_JTI, REQUEST);

        // then
        assertThat(response).isEqualTo(SignupResponse.from(issuedTokens()));
        verifyNoInteractions(memberRepository);
        verify(signupSessionStore).deleteBySignupJti(SIGNUP_JTI);
        verify(phoneVerificationStore).deleteVerifiedPhoneNumber(SIGNUP_JTI);
    }

    @Test
    void 생성_rollback_후_소셜_계정은_없고_전화번호만_있으면_PHONE_001이고_가입_상태를_유지한다() {
        // given
        givenServerState();
        given(authAccountRepository.findByProviderAndProviderSubject(OauthProvider.GOOGLE, PROVIDER_SUBJECT))
                .willReturn(Optional.empty());
        given(signupAccountCreator.create(REQUEST, SESSION, PHONE_NUMBER))
                .willThrow(dataIntegrityViolation());
        given(memberRepository.existsByPhoneNumber(PHONE_NUMBER)).willReturn(true);

        // when / then
        assertAuthError(
                () -> signupService.complete(SIGNUP_JTI, REQUEST),
                AuthErrorCode.PHONE_ALREADY_REGISTERED
        );
        assertNoTokenOrCleanup();
    }

    @Test
    void 알_수_없는_DB_제약은_민감정보_없는_내부오류이고_가입_상태를_유지한다(CapturedOutput output) {
        // given
        givenServerState();
        given(authAccountRepository.findByProviderAndProviderSubject(OauthProvider.GOOGLE, PROVIDER_SUBJECT))
                .willReturn(Optional.empty());
        given(signupAccountCreator.create(REQUEST, SESSION, PHONE_NUMBER))
                .willThrow(dataIntegrityViolation());
        given(memberRepository.existsByPhoneNumber(PHONE_NUMBER)).willReturn(false);

        // when / then
        assertThatThrownBy(() -> signupService.complete(SIGNUP_JTI, REQUEST))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("회원가입 데이터 무결성 제약을 확인할 수 없습니다.")
                .hasNoCause();
        assertNoTokenOrCleanup();
        assertSanitizedLog(output, "회원가입 데이터 무결성 처리 중 내부 오류가 발생했습니다.");
    }

    @Test
    void 가입_데이터_저장_실패는_민감정보_없는_내부오류이고_토큰이나_cleanup을_호출하지_않는다(CapturedOutput output) {
        // given
        givenServerState();
        given(signupAccountCreator.create(REQUEST, SESSION, PHONE_NUMBER))
                .willThrow(new IllegalArgumentException("sensitive-db-value"));

        // when / then
        assertThatThrownBy(() -> signupService.complete(SIGNUP_JTI, REQUEST))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("회원가입 데이터를 저장할 수 없습니다.")
                .hasNoCause();
        assertNoTokenOrCleanup();
        assertSanitizedLog(output, "회원가입 데이터 저장 중 내부 오류가 발생했습니다.");
    }

    @Test
    void 토큰_발급이_실패해도_두_가입_상태를_정리하고_민감정보_없는_내부오류를_반환한다(CapturedOutput output) {
        // given
        givenServerState();
        given(signupAccountCreator.create(REQUEST, SESSION, PHONE_NUMBER)).willReturn(42L);
        given(loginTokenIssuer.issueLoginTokens(42L))
                .willThrow(new IllegalStateException("sensitive-token-value"));

        // when / then
        assertThatThrownBy(() -> signupService.complete(SIGNUP_JTI, REQUEST))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("회원가입 후 로그인 토큰을 발급할 수 없습니다.")
                .hasNoCause();
        verify(signupSessionStore).deleteBySignupJti(SIGNUP_JTI);
        verify(phoneVerificationStore).deleteVerifiedPhoneNumber(SIGNUP_JTI);
        assertSanitizedLog(output, "회원가입 후 로그인 토큰 발급 중 내부 오류가 발생했습니다.");
    }

    @Test
    void 각_cleanup_실패는_독립적으로_시도하고_성공_응답을_유지하며_민감정보를_로그하지_않는다(CapturedOutput output) {
        // given
        givenServerState();
        given(signupAccountCreator.create(REQUEST, SESSION, PHONE_NUMBER)).willReturn(42L);
        given(loginTokenIssuer.issueLoginTokens(42L)).willReturn(issuedTokens());
        org.mockito.Mockito.doThrow(new IllegalStateException("sensitive-session-state"))
                .when(signupSessionStore).deleteBySignupJti(SIGNUP_JTI);
        org.mockito.Mockito.doThrow(new IllegalStateException("sensitive-phone-state"))
                .when(phoneVerificationStore).deleteVerifiedPhoneNumber(SIGNUP_JTI);

        // when
        SignupResponse response = signupService.complete(SIGNUP_JTI, REQUEST);

        // then
        assertThat(response).isEqualTo(SignupResponse.from(issuedTokens()));
        verify(signupSessionStore).deleteBySignupJti(SIGNUP_JTI);
        verify(phoneVerificationStore).deleteVerifiedPhoneNumber(SIGNUP_JTI);
        assertThat(output.getAll())
                .contains(
                        "가입 완료 후 가입 세션 정리에 실패했습니다.",
                        "가입 완료 후 인증 휴대전화 상태 정리에 실패했습니다."
                );
        assertSanitizedLog(output, "exceptionType=java.lang.IllegalStateException");
    }

    @Test
    void 손상된_가입_세션_조회는_민감정보_없는_내부오류이고_후속_작업을_하지_않는다(CapturedOutput output) {
        // given
        given(signupSessionStore.findBySignupJti(SIGNUP_JTI))
                .willThrow(new IllegalStateException("sensitive-provider-state"));

        // when / then
        assertThatThrownBy(() -> signupService.complete(SIGNUP_JTI, REQUEST))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("가입 세션을 확인할 수 없습니다.")
                .hasNoCause();
        verifyNoInteractions(phoneVerificationStore, signupAccountCreator, loginTokenIssuer);
        assertSanitizedLog(output, "가입 세션 조회 중 내부 오류가 발생했습니다.");
    }

    private void givenServerState() {
        given(signupSessionStore.findBySignupJti(SIGNUP_JTI)).willReturn(Optional.of(SESSION));
        given(authAccountRepository.findByProviderAndProviderSubject(OauthProvider.GOOGLE, PROVIDER_SUBJECT))
                .willReturn(Optional.empty());
        given(phoneVerificationStore.findVerifiedPhoneNumber(SIGNUP_JTI))
                .willReturn(Optional.of(PHONE_NUMBER));
    }

    private AuthAccount existingAccount(Long memberId) {
        return AuthAccount.builder()
                .memberId(memberId)
                .provider(OauthProvider.GOOGLE)
                .providerSubject(PROVIDER_SUBJECT)
                .providerEmail(PROVIDER_EMAIL)
                .build();
    }

    private IssuedLoginTokens issuedTokens() {
        return IssuedLoginTokens.of("access-token-sensitive", 3_600L, "refresh-token-sensitive", 2592000L);
    }

    private DataIntegrityViolationException dataIntegrityViolation() {
        return new DataIntegrityViolationException("sensitive-data-integrity-value");
    }

    private void assertAuthError(Runnable action, AuthErrorCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOf(AuthException.class)
                .extracting(exception -> ((AuthException) exception).getErrorCode())
                .isEqualTo(expected);
    }

    private void assertNoTokenOrCleanup() {
        verifyNoInteractions(loginTokenIssuer);
        verify(signupSessionStore, never()).deleteBySignupJti(SIGNUP_JTI);
        verify(phoneVerificationStore, never()).deleteVerifiedPhoneNumber(SIGNUP_JTI);
    }

    private void assertSanitizedLog(CapturedOutput output, String expectedContext) {
        assertThat(output.getAll())
                .contains(expectedContext)
                .doesNotContain(
                        SIGNUP_JTI,
                        PROVIDER_SUBJECT,
                        PROVIDER_EMAIL,
                        PHONE_NUMBER,
                        REQUEST.name(),
                        REQUEST.agreedTermIds().toString(),
                        "access-token-sensitive",
                        "refresh-token-sensitive",
                        "sensitive-db-value",
                        "sensitive-token-value",
                        "sensitive-session-state",
                        "sensitive-phone-state",
                        "sensitive-provider-state",
                        "sensitive-data-integrity-value"
                );
    }
}

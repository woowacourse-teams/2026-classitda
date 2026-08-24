package com.classitda.authentication.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.classitda.authentication.application.session.RefreshSession;
import com.classitda.authentication.application.session.RefreshSessionStore;
import com.classitda.authentication.application.token.LoginTokenIssuer;
import com.classitda.authentication.application.token.RefreshTokenIssuer;
import com.classitda.authentication.application.token.RefreshTokenVerifier;
import com.classitda.authentication.application.token.result.IssuedAccessToken;
import com.classitda.authentication.application.token.result.IssuedRefreshToken;
import com.classitda.authentication.exception.AuthErrorCode;
import com.classitda.authentication.exception.AuthException;
import com.classitda.authentication.infra.security.properties.TokenProperties;
import com.classitda.authentication.presentation.dto.token.RefreshTokenRequest;
import com.classitda.authentication.presentation.dto.token.LoginTokenResponse;
import com.classitda.member.domain.repository.MemberRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class RefreshTokenServiceTest {

    private static final String OLD_TOKEN =
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA.BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB";
    private static final String NEW_TOKEN =
            "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC.DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD";
    private static final String OLD_SESSION_ID = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String NEW_SESSION_ID = "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC";
    private static final String OLD_HASH = "a".repeat(64);
    private static final String NEW_HASH = "b".repeat(64);
    private static final Long MEMBER_ID = 987_654_321L;
    private static final long REFRESH_TTL_SECONDS = 2_592_000L;

    @Mock
    private RefreshTokenIssuer refreshTokenIssuer;

    @Mock
    private RefreshTokenVerifier refreshTokenVerifier;

    @Mock
    private RefreshSessionStore refreshSessionStore;

    @Mock
    private LoginTokenIssuer loginTokenIssuer;

    @Mock
    private MemberRepository memberRepository;

    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        TokenProperties tokenProperties = new TokenProperties(
                Duration.ofMinutes(30),
                Duration.ofHours(1),
                Duration.ofDays(30)
        );
        refreshTokenService = new RefreshTokenService(
                refreshTokenIssuer,
                refreshTokenVerifier,
                refreshSessionStore,
                loginTokenIssuer,
                memberRepository,
                tokenProperties
        );
    }

    @Test
    void Redis_세션의_회원_ID로_액세스와_리프레시_토큰을_회전한다() {
        // given
        RefreshSession oldSession = activeSession();
        IssuedRefreshToken generated = generatedRefreshToken();
        givenValidOldSession(oldSession);
        given(loginTokenIssuer.issueAccessToken(MEMBER_ID))
                .willReturn(IssuedAccessToken.of("access-token", 3_600L));
        given(refreshTokenIssuer.issue()).willReturn(generated);
        given(refreshSessionStore.rotate(
                org.mockito.ArgumentMatchers.eq(OLD_SESSION_ID),
                org.mockito.ArgumentMatchers.eq(oldSession),
                org.mockito.ArgumentMatchers.eq(NEW_SESSION_ID),
                org.mockito.ArgumentMatchers.any(RefreshSession.class),
                org.mockito.ArgumentMatchers.eq(REFRESH_TTL_SECONDS)
        )).willReturn(RefreshSessionStore.RotateOutcome.ROTATED);
        long before = Instant.now().plusSeconds(REFRESH_TTL_SECONDS).getEpochSecond();

        // when
        LoginTokenResponse response = refreshTokenService.refresh(RefreshTokenRequest.from(OLD_TOKEN));

        // then
        long after = Instant.now().plusSeconds(REFRESH_TTL_SECONDS).getEpochSecond();
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.accessTokenExpiresIn()).isEqualTo(3_600L);
        assertThat(response.refreshToken()).isEqualTo(NEW_TOKEN);
        assertThat(response.refreshTokenExpiresIn()).isEqualTo(REFRESH_TTL_SECONDS);
        InOrder order = inOrder(refreshTokenVerifier, refreshSessionStore, loginTokenIssuer, refreshTokenIssuer);
        order.verify(refreshTokenVerifier).extractSessionId(OLD_TOKEN);
        order.verify(refreshSessionStore).findBySessionId(OLD_SESSION_ID);
        order.verify(refreshTokenVerifier).matches(OLD_TOKEN, OLD_HASH);
        order.verify(loginTokenIssuer).issueAccessToken(MEMBER_ID);
        order.verify(refreshTokenIssuer).issue();
        order.verify(refreshSessionStore).rotate(
                org.mockito.ArgumentMatchers.eq(OLD_SESSION_ID),
                org.mockito.ArgumentMatchers.eq(oldSession),
                org.mockito.ArgumentMatchers.eq(NEW_SESSION_ID),
                org.mockito.ArgumentMatchers.argThat(session ->
                        session.tokenHash().equals(NEW_HASH)
                                && session.memberId().equals(MEMBER_ID)
                                && session.expiresAtEpochSecond() >= before
                                && session.expiresAtEpochSecond() <= after),
                org.mockito.ArgumentMatchers.eq(REFRESH_TTL_SECONDS)
        );
    }

    @Test
    void 형식이_잘못된_토큰은_Redis_key를_조회하기_전에_AUTH_008로_거부한다() {
        // given
        given(refreshTokenVerifier.extractSessionId("malformed-sensitive-token"))
                .willThrow(new IllegalArgumentException("sensitive-codec-message"));

        // when / then
        assertAuthError(
                () -> refreshTokenService.refresh(RefreshTokenRequest.from("malformed-sensitive-token")),
                AuthErrorCode.REFRESH_TOKEN_INVALID
        );
        verify(refreshTokenVerifier, never()).matches(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
        verifyNoInteractions(refreshTokenIssuer);
        verifyNoInteractions(refreshSessionStore, loginTokenIssuer);
    }

    @Test
    void 세션이_없거나_만료되거나_해시가_다르면_모두_AUTH_008이다() {
        // given
        givenTokenParsing();
        given(refreshSessionStore.findBySessionId(OLD_SESSION_ID))
                .willReturn(
                        Optional.empty(),
                        Optional.of(RefreshSession.of(OLD_HASH, MEMBER_ID, Instant.now().minusSeconds(1).getEpochSecond())),
                        Optional.of(activeSession())
                );
        given(refreshTokenVerifier.matches(OLD_TOKEN, OLD_HASH)).willReturn(false);

        // when / then
        for (int attempt = 0; attempt < 3; attempt++) {
            assertAuthError(
                    () -> refreshTokenService.refresh(RefreshTokenRequest.from(OLD_TOKEN)),
                    AuthErrorCode.REFRESH_TOKEN_INVALID
            );
        }
        verifyNoInteractions(loginTokenIssuer);
        verify(refreshSessionStore, never()).rotate(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyLong()
        );
    }

    @Test
    void 탈퇴_처리_중인_회원의_리프레시_토큰은_AUTH_008로_거부한다() {
        // given
        RefreshSession oldSession = activeSession();
        givenValidOldSession(oldSession);
        given(memberRepository.existsByIdAndWithdrawalRequestedAtIsNull(MEMBER_ID)).willReturn(false);

        // when / then
        assertAuthError(
                () -> refreshTokenService.refresh(RefreshTokenRequest.from(OLD_TOKEN)),
                AuthErrorCode.REFRESH_TOKEN_INVALID
        );
        verifyNoInteractions(loginTokenIssuer, refreshTokenIssuer);
        verify(refreshSessionStore, never()).rotate(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyLong()
        );
    }

    @Test
    void 액세스_토큰_발급이_실패하면_후보_리프레시_토큰을_만들거나_세션을_회전하지_않는다(CapturedOutput output) {
        // given
        givenValidOldSession(activeSession());
        given(loginTokenIssuer.issueAccessToken(MEMBER_ID))
                .willThrow(new IllegalStateException("sensitive-jwt-message"));

        // when / then
        assertSanitizedInternalFailure(
                () -> refreshTokenService.refresh(RefreshTokenRequest.from(OLD_TOKEN)),
                output
        );
        verify(refreshTokenIssuer, never()).issue();
        verify(refreshSessionStore, never()).rotate(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyLong()
        );
    }

    @Test
    void Redis_조회_실패는_민감정보_없는_내부오류로_변환한다(CapturedOutput output) {
        // given
        givenTokenParsing();
        given(refreshSessionStore.findBySessionId(OLD_SESSION_ID))
                .willThrow(new IllegalStateException("sensitive-redis-read-message"));

        // when / then
        assertSanitizedInternalFailure(
                () -> refreshTokenService.refresh(RefreshTokenRequest.from(OLD_TOKEN)),
                output
        );
        verifyNoInteractions(loginTokenIssuer);
    }

    @Test
    void Redis_회전_실패는_후보_토큰을_노출하지_않는_내부오류다(CapturedOutput output) {
        // given
        RefreshSession oldSession = activeSession();
        givenValidOldSession(oldSession);
        given(loginTokenIssuer.issueAccessToken(MEMBER_ID))
                .willReturn(IssuedAccessToken.of("sensitive-access-token", 3_600L));
        given(refreshTokenIssuer.issue()).willReturn(generatedRefreshToken());
        given(refreshSessionStore.rotate(
                org.mockito.ArgumentMatchers.eq(OLD_SESSION_ID),
                org.mockito.ArgumentMatchers.eq(oldSession),
                org.mockito.ArgumentMatchers.eq(NEW_SESSION_ID),
                org.mockito.ArgumentMatchers.any(RefreshSession.class),
                org.mockito.ArgumentMatchers.eq(REFRESH_TTL_SECONDS)
        )).willThrow(new IllegalStateException("sensitive-redis-script-message"));

        // when / then
        assertSanitizedInternalFailure(
                () -> refreshTokenService.refresh(RefreshTokenRequest.from(OLD_TOKEN)),
                output
        );
    }

    @Test
    void 원자_회전의_동시_패자는_AUTH_008이고_새_key_충돌은_내부오류다() {
        // given
        RefreshSession oldSession = activeSession();
        givenValidOldSession(oldSession);
        given(loginTokenIssuer.issueAccessToken(MEMBER_ID))
                .willReturn(IssuedAccessToken.of("access-token", 3_600L));
        given(refreshTokenIssuer.issue()).willReturn(generatedRefreshToken());
        given(refreshSessionStore.rotate(
                org.mockito.ArgumentMatchers.eq(OLD_SESSION_ID),
                org.mockito.ArgumentMatchers.eq(oldSession),
                org.mockito.ArgumentMatchers.eq(NEW_SESSION_ID),
                org.mockito.ArgumentMatchers.any(RefreshSession.class),
                org.mockito.ArgumentMatchers.eq(REFRESH_TTL_SECONDS)
        )).willReturn(
                RefreshSessionStore.RotateOutcome.OLD_SESSION_MISMATCH,
                RefreshSessionStore.RotateOutcome.NEW_SESSION_CONFLICT
        );

        // when / then
        assertAuthError(
                () -> refreshTokenService.refresh(RefreshTokenRequest.from(OLD_TOKEN)),
                AuthErrorCode.REFRESH_TOKEN_INVALID
        );
        assertThatThrownBy(() -> refreshTokenService.refresh(RefreshTokenRequest.from(OLD_TOKEN)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("리프레시 토큰 갱신 중 내부 오류가 발생했습니다.")
                .hasNoCause();
    }

    private void givenValidOldSession(RefreshSession oldSession) {
        givenTokenParsing();
        given(refreshSessionStore.findBySessionId(OLD_SESSION_ID)).willReturn(Optional.of(oldSession));
        given(refreshTokenVerifier.matches(OLD_TOKEN, oldSession.tokenHash())).willReturn(true);
        given(memberRepository.existsByIdAndWithdrawalRequestedAtIsNull(oldSession.memberId())).willReturn(true);
    }

    private void givenTokenParsing() {
        given(refreshTokenVerifier.extractSessionId(OLD_TOKEN)).willReturn(OLD_SESSION_ID);
    }

    private RefreshSession activeSession() {
        return RefreshSession.of(OLD_HASH, MEMBER_ID, Instant.now().plusSeconds(600).getEpochSecond());
    }

    private IssuedRefreshToken generatedRefreshToken() {
        return IssuedRefreshToken.of(NEW_TOKEN, NEW_SESSION_ID, NEW_HASH);
    }

    private void assertAuthError(Runnable action, AuthErrorCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOf(AuthException.class)
                .extracting(exception -> ((AuthException) exception).getErrorCode())
                .isEqualTo(expected);
    }

    private void assertSanitizedInternalFailure(Runnable action, CapturedOutput output) {
        assertThatThrownBy(action::run)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("리프레시 토큰 갱신 중 내부 오류가 발생했습니다.")
                .hasNoCause();
        assertThat(output.getAll())
                .contains("리프레시 토큰 갱신 중 내부 오류가 발생했습니다.")
                .doesNotContain(
                        OLD_TOKEN,
                        NEW_TOKEN,
                        OLD_HASH,
                        NEW_HASH,
                        OLD_SESSION_ID,
                        NEW_SESSION_ID,
                        MEMBER_ID.toString(),
                        "auth:refresh:",
                        "sensitive-codec-message",
                        "sensitive-jwt-message",
                        "sensitive-redis-read-message",
                        "sensitive-redis-script-message",
                        "sensitive-access-token"
                );
    }
}

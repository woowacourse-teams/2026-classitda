package com.classitda.authentication.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.classitda.authentication.application.session.RefreshSession;
import com.classitda.authentication.application.session.RefreshSessionStore;
import com.classitda.authentication.application.token.RefreshTokenVerifier;
import com.classitda.authentication.presentation.dto.logout.LogoutRequest;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class LogoutServiceTest {

    private static final String REFRESH_TOKEN =
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA.BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB";
    private static final String SESSION_ID = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
    private static final String TOKEN_HASH = "a".repeat(64);
    private static final Long MEMBER_ID = 987_654_321L;

    @Mock
    private RefreshTokenVerifier refreshTokenVerifier;

    @Mock
    private RefreshSessionStore refreshSessionStore;

    private LogoutService logoutService;

    @BeforeEach
    void setUp() {
        logoutService = new LogoutService(refreshTokenVerifier, refreshSessionStore);
    }

    @Test
    void 인증_회원과_토큰이_활성_세션에_일치하면_정확한_세션만_삭제한다() {
        // given
        RefreshSession session = activeSession(MEMBER_ID, TOKEN_HASH);
        givenMatchingSession(session);
        given(refreshSessionStore.deleteIfMatches(SESSION_ID, session))
                .willReturn(RefreshSessionStore.DeleteOutcome.DELETED);

        // when
        logoutService.logout(MEMBER_ID, LogoutRequest.from(REFRESH_TOKEN));

        // then
        verify(refreshSessionStore).deleteIfMatches(SESSION_ID, session);
    }

    @Test
    void 세션이_없거나_만료되면_멱등하게_완료하고_삭제하지_않는다() {
        // given
        given(refreshTokenVerifier.extractSessionId(REFRESH_TOKEN)).willReturn(SESSION_ID);
        given(refreshSessionStore.findBySessionId(SESSION_ID)).willReturn(
                Optional.empty(),
                Optional.of(RefreshSession.of(
                        TOKEN_HASH,
                        MEMBER_ID,
                        Instant.now().minusSeconds(1).getEpochSecond()
                ))
        );

        // when
        logoutService.logout(MEMBER_ID, LogoutRequest.from(REFRESH_TOKEN));
        logoutService.logout(MEMBER_ID, LogoutRequest.from(REFRESH_TOKEN));

        // then
        verify(refreshSessionStore, never()).deleteIfMatches(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void 다른_회원이나_다른_해시의_세션은_멱등하게_보존한다() {
        // given
        RefreshSession otherMemberSession = activeSession(MEMBER_ID + 1L, TOKEN_HASH);
        RefreshSession otherHashSession = activeSession(MEMBER_ID, "b".repeat(64));
        given(refreshTokenVerifier.extractSessionId(REFRESH_TOKEN)).willReturn(SESSION_ID);
        given(refreshSessionStore.findBySessionId(SESSION_ID)).willReturn(
                Optional.of(otherMemberSession),
                Optional.of(otherHashSession)
        );
        given(refreshTokenVerifier.matches(REFRESH_TOKEN, otherHashSession.tokenHash())).willReturn(false);

        // when
        logoutService.logout(MEMBER_ID, LogoutRequest.from(REFRESH_TOKEN));
        logoutService.logout(MEMBER_ID, LogoutRequest.from(REFRESH_TOKEN));

        // then
        verify(refreshTokenVerifier).matches(REFRESH_TOKEN, otherHashSession.tokenHash());
        verify(refreshSessionStore, never()).deleteIfMatches(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void 조건부_삭제의_동시_패자는_멱등하게_완료한다() {
        // given
        RefreshSession session = activeSession(MEMBER_ID, TOKEN_HASH);
        givenMatchingSession(session);
        given(refreshSessionStore.deleteIfMatches(SESSION_ID, session))
                .willReturn(RefreshSessionStore.DeleteOutcome.SESSION_MISMATCH);

        // when
        logoutService.logout(MEMBER_ID, LogoutRequest.from(REFRESH_TOKEN));

        // then
        verify(refreshSessionStore).deleteIfMatches(SESSION_ID, session);
    }

    @Test
    void 방어적_null_요청_입력은_민감정보_없는_내부오류다(CapturedOutput output) {
        // given
        given(refreshTokenVerifier.extractSessionId(null))
                .willThrow(new IllegalArgumentException("sensitive-codec-message"));

        // when / then
        assertSanitizedInternalFailure(() -> logoutService.logout(MEMBER_ID, null), output);
        verifyNoInteractions(refreshSessionStore);
    }

    @Test
    void Redis_조회_실패와_손상_세션은_민감정보_없는_내부오류다(CapturedOutput output) {
        // given
        given(refreshTokenVerifier.extractSessionId(REFRESH_TOKEN)).willReturn(SESSION_ID);
        given(refreshSessionStore.findBySessionId(SESSION_ID)).willThrow(
                new IllegalStateException("sensitive-corrupt-tokenHash-memberId-sessionId-auth:refresh:"),
                new IllegalStateException("sensitive-redis-read-message")
        );

        // when / then
        assertSanitizedInternalFailure(
                () -> logoutService.logout(MEMBER_ID, LogoutRequest.from(REFRESH_TOKEN)),
                output
        );
        assertSanitizedInternalFailure(
                () -> logoutService.logout(MEMBER_ID, LogoutRequest.from(REFRESH_TOKEN)),
                output
        );
    }

    @Test
    void Redis_삭제_실패와_알_수_없는_결과는_민감정보_없는_내부오류다(CapturedOutput output) {
        // given
        RefreshSession session = activeSession(MEMBER_ID, TOKEN_HASH);
        givenMatchingSession(session);
        given(refreshSessionStore.deleteIfMatches(SESSION_ID, session)).willThrow(
                new IllegalStateException("sensitive-redis-delete-message")
        );

        // when / then
        assertSanitizedInternalFailure(
                () -> logoutService.logout(MEMBER_ID, LogoutRequest.from(REFRESH_TOKEN)),
                output
        );

        // given
        willReturn(null).given(refreshSessionStore).deleteIfMatches(SESSION_ID, session);

        // when / then
        assertSanitizedInternalFailure(
                () -> logoutService.logout(MEMBER_ID, LogoutRequest.from(REFRESH_TOKEN)),
                output
        );
    }

    private void givenMatchingSession(RefreshSession session) {
        given(refreshTokenVerifier.extractSessionId(REFRESH_TOKEN)).willReturn(SESSION_ID);
        given(refreshSessionStore.findBySessionId(SESSION_ID)).willReturn(Optional.of(session));
        given(refreshTokenVerifier.matches(REFRESH_TOKEN, session.tokenHash())).willReturn(true);
    }

    private RefreshSession activeSession(Long memberId, String tokenHash) {
        return RefreshSession.of(tokenHash, memberId, Instant.now().plusSeconds(600).getEpochSecond());
    }

    private void assertSanitizedInternalFailure(Runnable action, CapturedOutput output) {
        assertThatThrownBy(action::run)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("로그아웃 중 내부 오류가 발생했습니다.")
                .hasNoCause();
        assertThat(output.getAll())
                .contains("로그아웃 중 내부 오류가 발생했습니다.")
                .doesNotContain(
                        REFRESH_TOKEN,
                        TOKEN_HASH,
                        SESSION_ID,
                        MEMBER_ID.toString(),
                        "auth:refresh:",
                        "sensitive-corrupt-tokenHash-memberId-sessionId-auth:refresh:",
                        "sensitive-redis-read-message",
                        "sensitive-redis-delete-message",
                        "sensitive-codec-message"
                );
    }
}

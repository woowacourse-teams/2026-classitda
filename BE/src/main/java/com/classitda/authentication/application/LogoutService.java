package com.classitda.authentication.application;

import com.classitda.authentication.application.session.RefreshSession;
import com.classitda.authentication.application.session.RefreshSessionStore;
import com.classitda.authentication.application.token.RefreshTokenVerifier;
import com.classitda.authentication.presentation.dto.logout.LogoutRequest;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class LogoutService {

    private final RefreshTokenVerifier refreshTokenVerifier;
    private final RefreshSessionStore refreshSessionStore;

    public void logout(Long memberId, LogoutRequest request) {
        try {
            String refreshToken = request == null ? null : request.refreshToken();
            revokeCurrentRefreshSession(memberId, refreshToken);
        } catch (RuntimeException exception) {
            log.error(
                    "로그아웃 중 내부 오류가 발생했습니다. exceptionType={}",
                    exception.getClass().getName()
            );
            throw new IllegalStateException("로그아웃 중 내부 오류가 발생했습니다.");
        }
    }

    private void revokeCurrentRefreshSession(Long memberId, String refreshToken) {
        String sessionId = refreshTokenVerifier.extractSessionId(refreshToken);
        RefreshSession session = refreshSessionStore.findBySessionId(sessionId).orElse(null);

        if (session == null
                || Instant.now().getEpochSecond() >= session.expiresAtEpochSecond()
                || !memberId.equals(session.memberId())
                || !refreshTokenVerifier.matches(refreshToken, session.tokenHash())) {
            return;
        }

        RefreshSessionStore.DeleteOutcome outcome = refreshSessionStore.deleteIfMatches(sessionId, session);
        if (outcome != RefreshSessionStore.DeleteOutcome.DELETED
                && outcome != RefreshSessionStore.DeleteOutcome.SESSION_MISMATCH) {
            throw new IllegalStateException("리프레시 세션을 삭제할 수 없습니다.");
        }
    }
}

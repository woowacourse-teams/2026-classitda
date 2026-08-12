package com.classitda.authentication.application;

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
import com.classitda.authentication.presentation.dto.token.RefreshTokenResponse;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RefreshTokenService {

    private final RefreshTokenIssuer refreshTokenIssuer;
    private final RefreshTokenVerifier refreshTokenVerifier;
    private final RefreshSessionStore refreshSessionStore;
    private final LoginTokenIssuer loginTokenIssuer;
    private final TokenProperties tokenProperties;

    public RefreshTokenResponse refresh(RefreshTokenRequest request) {
        try {
            return refreshInternal(request);
        } catch (AuthException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            log.error(
                    "리프레시 토큰 갱신 중 내부 오류가 발생했습니다. exceptionType={}",
                    exception.getClass().getName()
            );
            throw new IllegalStateException("리프레시 토큰 갱신 중 내부 오류가 발생했습니다.");
        }
    }

    private RefreshTokenResponse refreshInternal(RefreshTokenRequest request) {
        String refreshToken = request == null ? null : request.refreshToken();
        String oldSessionId = extractSessionId(refreshToken);
        RefreshSession oldSession = refreshSessionStore.findBySessionId(oldSessionId)
                .orElseThrow(this::invalidRefreshToken);

        if (Instant.now().getEpochSecond() >= oldSession.expiresAtEpochSecond()
                || !refreshTokenVerifier.matches(refreshToken, oldSession.tokenHash())) {
            throw invalidRefreshToken();
        }

        IssuedAccessToken accessToken = loginTokenIssuer.issueAccessToken(oldSession.memberId());
        IssuedRefreshToken newRefreshToken = refreshTokenIssuer.issue();
        long refreshTokenExpiresIn = tokenProperties.refreshTtl().toSeconds();
        RefreshSession newSession = RefreshSession.of(
                newRefreshToken.tokenHash(),
                oldSession.memberId(),
                Instant.now().plusSeconds(refreshTokenExpiresIn).getEpochSecond()
        );

        RefreshSessionStore.RotateOutcome outcome = refreshSessionStore.rotate(
                oldSessionId,
                oldSession,
                newRefreshToken.sessionId(),
                newSession,
                refreshTokenExpiresIn
        );
        if (outcome == RefreshSessionStore.RotateOutcome.OLD_SESSION_MISMATCH) {
            throw invalidRefreshToken();
        }
        if (outcome != RefreshSessionStore.RotateOutcome.ROTATED) {
            throw new IllegalStateException("리프레시 세션을 회전할 수 없습니다.");
        }

        return RefreshTokenResponse.of(accessToken, newRefreshToken, refreshTokenExpiresIn);
    }

    private String extractSessionId(String refreshToken) {
        try {
            return refreshTokenVerifier.extractSessionId(refreshToken);
        } catch (IllegalArgumentException exception) {
            throw invalidRefreshToken();
        }
    }

    private AuthException invalidRefreshToken() {
        return new AuthException(AuthErrorCode.REFRESH_TOKEN_INVALID);
    }
}

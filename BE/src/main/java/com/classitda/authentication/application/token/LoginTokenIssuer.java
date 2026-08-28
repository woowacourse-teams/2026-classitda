package com.classitda.authentication.application.token;

import com.classitda.authentication.application.session.RefreshSession;
import com.classitda.authentication.application.session.RefreshSessionStore;
import com.classitda.authentication.application.token.result.IssuedAccessToken;
import com.classitda.authentication.application.token.result.IssuedLoginTokens;
import com.classitda.authentication.application.token.result.IssuedRefreshToken;
import com.classitda.authentication.domain.TokenUse;
import com.classitda.authentication.infra.security.jwt.JwtTokenEncoder;
import com.classitda.authentication.infra.security.properties.TokenProperties;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LoginTokenIssuer {

    private final RefreshTokenIssuer refreshTokenIssuer;
    private final RefreshSessionStore refreshSessionStore;
    private final JwtTokenEncoder jwtTokenEncoder;
    private final TokenProperties tokenProperties;

    public IssuedLoginTokens issueLoginTokens(Long memberId) {
        IssuedAccessToken accessToken = issueAccessToken(memberId);
        IssuedRefreshToken refreshToken = refreshTokenIssuer.issue();
        long refreshTokenExpiresIn = tokenProperties.refreshTtl().toSeconds();
        RefreshSession refreshSession = RefreshSession.of(
                refreshToken.tokenHash(),
                memberId,
                Instant.now().plusSeconds(refreshTokenExpiresIn).getEpochSecond()
        );
        refreshSessionStore.save(
                refreshToken.sessionId(),
                refreshSession,
                refreshTokenExpiresIn
        );

        return IssuedLoginTokens.of(
                accessToken.accessToken(),
                accessToken.accessTokenExpiresIn(),
                refreshToken.refreshToken(),
                refreshTokenExpiresIn
        );
    }

    public IssuedAccessToken issueAccessToken(Long memberId) {
        try {
            String accessToken = jwtTokenEncoder.encode(
                    TokenUse.ACCESS,
                    memberId.toString(),
                    UUID.randomUUID().toString(),
                    tokenProperties.accessTtl()
            );

            return IssuedAccessToken.of(accessToken, tokenProperties.accessTtl().toSeconds());
        } catch (RuntimeException exception) {
            throw new IllegalStateException("액세스 토큰을 발급할 수 없습니다.");
        }
    }
}

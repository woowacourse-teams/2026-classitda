package com.classitda.authentication.application.token.result;

public record IssuedRefreshToken(
        String refreshToken,
        String sessionId,
        String tokenHash
) {

    public static IssuedRefreshToken of(
            String refreshToken,
            String sessionId,
            String tokenHash
    ) {
        return new IssuedRefreshToken(refreshToken, sessionId, tokenHash);
    }
}

package com.classitda.authentication.application.token.result;

public record IssuedLoginTokens(
        String accessToken,
        long accessTokenExpiresIn,
        String refreshToken,
        long refreshTokenExpiresIn
) {

    public static IssuedLoginTokens of(
            String accessToken,
            long accessTokenExpiresIn,
            String refreshToken,
            long refreshTokenExpiresIn
    ) {
        return new IssuedLoginTokens(
                accessToken,
                accessTokenExpiresIn,
                refreshToken,
                refreshTokenExpiresIn);
    }
}

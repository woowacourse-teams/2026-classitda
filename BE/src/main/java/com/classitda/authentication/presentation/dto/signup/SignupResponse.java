package com.classitda.authentication.presentation.dto.signup;

import com.classitda.authentication.application.token.IssuedLoginTokens;

public record SignupResponse(
        String accessToken,
        long accessTokenExpiresIn,
        String refreshToken,
        long refreshTokenExpiresIn
) {
    public static SignupResponse from(IssuedLoginTokens issuedLoginTokens) {
        return new SignupResponse(
                issuedLoginTokens.accessToken(),
                issuedLoginTokens.accessTokenExpiresIn(),
                issuedLoginTokens.refreshToken(),
                issuedLoginTokens.refreshTokenExpiresIn());
    }
}

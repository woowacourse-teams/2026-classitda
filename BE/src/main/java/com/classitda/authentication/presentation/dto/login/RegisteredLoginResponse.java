package com.classitda.authentication.presentation.dto.login;

public record RegisteredLoginResponse(
        LoginStatus status,
        String accessToken,
        long accessTokenExpiresIn,
        String refreshToken,
        long refreshTokenExpiresIn
) implements LoginResponse {
}

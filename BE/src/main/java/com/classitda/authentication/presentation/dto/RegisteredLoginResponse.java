package com.classitda.authentication.presentation.dto;

public record RegisteredLoginResponse(
        LoginStatus status,
        String accessToken,
        long accessTokenExpiresIn,
        String refreshToken,
        long refreshTokenExpiresIn
) implements LoginResponse {
}

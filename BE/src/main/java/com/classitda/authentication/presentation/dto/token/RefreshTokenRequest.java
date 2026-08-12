package com.classitda.authentication.presentation.dto.token;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RefreshTokenRequest(
        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9_-]{43}\\.[A-Za-z0-9_-]{43}$")
        String refreshToken
) {

    public static RefreshTokenRequest from(String refreshToken) {
        return new RefreshTokenRequest(refreshToken);
    }
}

package com.classitda.authentication.presentation.dto.logout;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LogoutRequest(
        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9_-]{43}\\.[A-Za-z0-9_-]{43}$")
        String refreshToken
) {

    public static LogoutRequest from(String refreshToken) {
        return new LogoutRequest(refreshToken);
    }
}

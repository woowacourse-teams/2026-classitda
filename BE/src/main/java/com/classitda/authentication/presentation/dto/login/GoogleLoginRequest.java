package com.classitda.authentication.presentation.dto.login;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @NotBlank(message = "Google ID 토큰은 필수입니다.")
        String idToken
) {

    public static GoogleLoginRequest from(String idToken) {
        return new GoogleLoginRequest(idToken);
    }
}

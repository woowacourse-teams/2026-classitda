package com.classitda.authentication.presentation.dto.login;

import jakarta.validation.constraints.NotBlank;

public record SocialLoginRequest(
        @NotBlank(message = "소셜 로그인 ID 토큰은 필수입니다.")
        String idToken
) {

    public static SocialLoginRequest from(String idToken) {
        return new SocialLoginRequest(idToken);
    }
}

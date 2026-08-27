package com.classitda.authentication.presentation.dto.login;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SocialLoginRequest(
        @NotBlank(message = "소셜 로그인 ID 토큰은 필수입니다.")
        String idToken,

        @NotBlank(message = "소셜 로그인 rawNonce는 필수입니다.")
        @Size(min = 43, max = 43, message = "소셜 로그인 rawNonce는 43자여야 합니다.")
        @Pattern(regexp = "[A-Za-z0-9_-]+", message = "소셜 로그인 rawNonce 형식이 올바르지 않습니다.")
        String rawNonce
) {

    public static SocialLoginRequest of(String idToken, String rawNonce) {
        return new SocialLoginRequest(idToken, rawNonce);
    }
}

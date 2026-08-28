package com.classitda.authentication.presentation.dto.login;

public record RegistrationRequiredLoginResponse(
        LoginStatus status,
        String signupToken,
        long signupTokenExpiresIn
) implements LoginResponse {
}

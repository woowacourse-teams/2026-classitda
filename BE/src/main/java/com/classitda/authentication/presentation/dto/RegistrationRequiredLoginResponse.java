package com.classitda.authentication.presentation.dto;

public record RegistrationRequiredLoginResponse(
        LoginStatus status,
        String signupToken,
        long signupTokenExpiresIn
) implements LoginResponse {
}

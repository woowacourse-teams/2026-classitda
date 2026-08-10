package com.classitda.authentication.presentation.dto;

import com.classitda.authentication.application.token.IssuedLoginTokens;
import com.classitda.authentication.application.token.IssuedSignupToken;

public sealed interface LoginResponse permits RegisteredLoginResponse, RegistrationRequiredLoginResponse {

    static LoginResponse registered(IssuedLoginTokens issuedLoginTokens) {
        return new RegisteredLoginResponse(
                LoginStatus.REGISTERED,
                issuedLoginTokens.accessToken(),
                issuedLoginTokens.accessTokenExpiresIn(),
                issuedLoginTokens.refreshToken(),
                issuedLoginTokens.refreshTokenExpiresIn());
    }

    static LoginResponse registrationRequired(IssuedSignupToken issuedSignupToken) {
        return new RegistrationRequiredLoginResponse(
                LoginStatus.REGISTRATION_REQUIRED,
                issuedSignupToken.signupToken(),
                issuedSignupToken.signupTokenExpiresIn());
    }

    enum LoginStatus {
        REGISTERED,
        REGISTRATION_REQUIRED
    }
}

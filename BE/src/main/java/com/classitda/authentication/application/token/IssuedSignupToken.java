package com.classitda.authentication.application.token;

public record IssuedSignupToken(String signupToken, long signupTokenExpiresIn) {

    public static IssuedSignupToken of(String signupToken, long signupTokenExpiresIn) {
        return new IssuedSignupToken(signupToken, signupTokenExpiresIn);
    }
}

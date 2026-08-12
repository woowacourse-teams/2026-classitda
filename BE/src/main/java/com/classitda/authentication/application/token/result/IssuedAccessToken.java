package com.classitda.authentication.application.token.result;

public record IssuedAccessToken(String accessToken, long accessTokenExpiresIn) {

    public static IssuedAccessToken of(String accessToken, long accessTokenExpiresIn) {
        return new IssuedAccessToken(accessToken, accessTokenExpiresIn);
    }
}

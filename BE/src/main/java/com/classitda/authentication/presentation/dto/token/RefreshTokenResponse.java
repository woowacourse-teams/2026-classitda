package com.classitda.authentication.presentation.dto.token;

import com.classitda.authentication.application.token.result.IssuedAccessToken;
import com.classitda.authentication.application.token.result.IssuedRefreshToken;

public record RefreshTokenResponse(
        String accessToken,
        long accessTokenExpiresIn,
        String refreshToken,
        long refreshTokenExpiresIn
) {

    public static RefreshTokenResponse of(
            IssuedAccessToken accessToken,
            IssuedRefreshToken refreshToken,
            long refreshTokenExpiresIn
    ) {
        return new RefreshTokenResponse(
                accessToken.accessToken(),
                accessToken.accessTokenExpiresIn(),
                refreshToken.refreshToken(),
                refreshTokenExpiresIn
        );
    }
}

package com.classitda.authentication.application.token;

import com.classitda.authentication.application.token.result.IssuedRefreshToken;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenIssuer {

    private static final int RANDOM_VALUE_BYTE_LENGTH = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public IssuedRefreshToken issue() {
        String sessionId = randomUrlSafeValue();
        String refreshToken = sessionId + "." + randomUrlSafeValue();

        return IssuedRefreshToken.of(
                refreshToken,
                sessionId,
                RefreshTokenHasher.hash(refreshToken)
        );
    }

    private String randomUrlSafeValue() {
        byte[] randomBytes = new byte[RANDOM_VALUE_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}

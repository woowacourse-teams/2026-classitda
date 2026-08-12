package com.classitda.authentication.application.token;

import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenVerifier {

    private static final Pattern TOKEN_PATTERN =
            Pattern.compile("^[A-Za-z0-9_-]{43}\\.[A-Za-z0-9_-]{43}$");

    public String extractSessionId(String refreshToken) {
        validate(refreshToken);
        return refreshToken.substring(0, 43);
    }

    public boolean matches(String refreshToken, String storedHash) {
        validate(refreshToken);
        return RefreshTokenHasher.matches(refreshToken, storedHash);
    }

    private void validate(String refreshToken) {
        if (refreshToken == null || !TOKEN_PATTERN.matcher(refreshToken).matches()) {
            throw new IllegalArgumentException("리프레시 토큰 형식이 올바르지 않습니다.");
        }
    }
}

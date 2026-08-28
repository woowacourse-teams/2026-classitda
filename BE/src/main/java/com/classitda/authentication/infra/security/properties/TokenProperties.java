package com.classitda.authentication.infra.security.properties;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.token")
public record TokenProperties(Duration signupTtl, Duration accessTtl, Duration refreshTtl) {

    private static final Duration SIGNUP_TTL = Duration.ofMinutes(30);
    private static final Duration ACCESS_TTL = Duration.ofHours(1);
    private static final Duration REFRESH_TTL = Duration.ofDays(30);

    public TokenProperties {
        requireFixedTtl(signupTtl, SIGNUP_TTL, "회원가입", "30분");
        requireFixedTtl(accessTtl, ACCESS_TTL, "액세스", "1시간");
        requireFixedTtl(refreshTtl, REFRESH_TTL, "리프레시", "30일");
    }

    private static void requireFixedTtl(
            Duration actual,
            Duration expected,
            String tokenName,
            String expectedDescription
    ) {
        if (!expected.equals(actual)) {
            throw new IllegalArgumentException(
                    tokenName + " 토큰 만료 시간은 " + expectedDescription + "이어야 합니다.");
        }
    }
}

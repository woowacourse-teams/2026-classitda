package com.classitda.authentication.application.token;

import com.classitda.authentication.domain.TokenUse;
import com.classitda.authentication.infra.security.jwt.JwtTokenEncoder;
import com.classitda.authentication.infra.security.properties.TokenProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
@Service
public class LoginTokenIssuer {

    private static final String REFRESH_SESSION_KEY_PREFIX = "auth:refresh:";
    private static final int RANDOM_VALUE_BYTE_LENGTH = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final JwtTokenEncoder jwtTokenEncoder;
    private final TokenProperties tokenProperties;

    public IssuedLoginTokens issueLoginTokens(Long memberId) {
        String accessToken = issueAccessToken(memberId);
        String refreshToken = issueRefreshToken(memberId);

        return IssuedLoginTokens.of(
                accessToken,
                tokenProperties.accessTtl().toSeconds(),
                refreshToken,
                tokenProperties.refreshTtl().toSeconds());
    }

    private String issueAccessToken(Long memberId) {
        return jwtTokenEncoder.encode(
                TokenUse.ACCESS,
                memberId.toString(),
                UUID.randomUUID().toString(),
                tokenProperties.accessTtl());
    }

    private String issueRefreshToken(Long memberId) {
        String sessionId = randomUrlSafeValue();
        String refreshToken = sessionId + "." + randomUrlSafeValue();
        Instant expiresAt = Instant.now().plus(tokenProperties.refreshTtl());

        RefreshSessionValue sessionValue = new RefreshSessionValue(
                sha256(refreshToken),
                memberId,
                expiresAt.getEpochSecond());

        redisTemplate.opsForValue().set(
                REFRESH_SESSION_KEY_PREFIX + sessionId,
                serialize(sessionValue),
                tokenProperties.refreshTtl());

        return refreshToken;
    }

    private String randomUrlSafeValue() {
        byte[] randomBytes = new byte[RANDOM_VALUE_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String sha256(String value) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(messageDigest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("리프레시 토큰 해시를 생성할 수 없습니다.", exception);
        }
    }

    private String serialize(RefreshSessionValue sessionValue) {
        try {
            return objectMapper.writeValueAsString(sessionValue);
        } catch (JacksonException exception) {
            throw new IllegalStateException("리프레시 세션을 직렬화할 수 없습니다.", exception);
        }
    }

    private record RefreshSessionValue(
            String tokenDigest,
            Long memberId,
            long expiresAtEpochSecond
    ) {
    }
}

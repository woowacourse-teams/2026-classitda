package com.classitda.authentication.application;

import com.classitda.authentication.domain.OauthProvider;
import com.classitda.authentication.domain.TokenUse;
import com.classitda.authentication.exception.AuthErrorCode;
import com.classitda.authentication.exception.AuthException;
import com.classitda.authentication.infra.security.jwt.JwtContract;
import com.classitda.authentication.infra.security.properties.TokenProperties;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
@Service
public class SignupService {

    private static final String SIGNUP_SESSION_KEY_PREFIX = "signup:session:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final JwtEncoder jwtEncoder;
    private final TokenProperties tokenProperties;

    public String issueSignupToken(
            OauthProvider provider,
            String providerSubject,
            String profileImageUrl
    ) {
        validateProvider(provider);
        validateProviderSubject(providerSubject);

        String signupJti = UUID.randomUUID().toString();
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(tokenProperties.signupTtl());
        String signupToken = encodeSignupToken(signupJti, issuedAt, expiresAt);

        saveSignupSession(signupJti, provider, providerSubject, profileImageUrl);
        return signupToken;
    }

    public boolean hasActiveSignupSession(String signupJti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(signupSessionKey(signupJti)));
    }

    private void validateProvider(OauthProvider provider) {
        if (provider == null) {
            throw new AuthException(AuthErrorCode.AUTH_ACCOUNT_PROVIDER_REQUIRED);
        }
    }

    private void validateProviderSubject(String providerSubject) {
        if (providerSubject == null || providerSubject.isBlank()) {
            throw new AuthException(AuthErrorCode.AUTH_ACCOUNT_PROVIDER_SUBJECT_REQUIRED);
        }
    }

    private String encodeSignupToken(
            String signupJti,
            Instant issuedAt,
            Instant expiresAt
    ) {
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(JwtContract.ISSUER)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .id(signupJti)
                .subject(signupJti)
                .claim(JwtContract.TOKEN_USE_CLAIM, TokenUse.SIGNUP.name())
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private void saveSignupSession(
            String signupJti,
            OauthProvider provider,
            String providerSubject,
            String profileImageUrl
    ) {
        SignupSessionValue sessionValue = new SignupSessionValue(provider, providerSubject, profileImageUrl);
        redisTemplate.opsForValue().set(
                signupSessionKey(signupJti),
                serialize(sessionValue),
                tokenProperties.signupTtl());
    }

    private String serialize(SignupSessionValue sessionValue) {
        try {
            return objectMapper.writeValueAsString(sessionValue);
        } catch (JacksonException exception) {
            throw new IllegalStateException("가입 세션을 직렬화할 수 없습니다.", exception);
        }
    }

    private String signupSessionKey(String signupJti) {
        return SIGNUP_SESSION_KEY_PREFIX + signupJti;
    }

    private record SignupSessionValue(
            OauthProvider provider,
            String providerSubject,
            String profileImageUrl
    ) {
    }
}

package com.classitda.authentication.application.session;

import com.classitda.authentication.domain.OauthProvider;
import com.classitda.authentication.infra.security.properties.TokenProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
@Component
public class SignupSessionRegistry {

    private static final String SIGNUP_SESSION_KEY_PREFIX = "signup:session:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final TokenProperties tokenProperties;

    public void save(
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

    public boolean hasActiveSession(String signupJti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(signupSessionKey(signupJti)));
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

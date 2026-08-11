package com.classitda.authentication.application.session;

import com.classitda.authentication.infra.security.properties.TokenProperties;
import java.util.Optional;
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

    public void save(String signupJti, SignupSession session) {
        redisTemplate.opsForValue().set(
                signupSessionKey(signupJti),
                serialize(session),
                tokenProperties.signupTtl());
    }

    public boolean hasActiveSession(String signupJti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(signupSessionKey(signupJti)));
    }

    public Optional<SignupSession> findBySignupJti(String signupJti) {
        String serializedSession = redisTemplate.opsForValue().get(signupSessionKey(signupJti));
        if (serializedSession == null) {
            return Optional.empty();
        }

        return Optional.of(deserialize(serializedSession));
    }

    public void deleteBySignupJti(String signupJti) {
        redisTemplate.delete(signupSessionKey(signupJti));
    }

    private String serialize(SignupSession session) {
        try {
            return objectMapper.writeValueAsString(session);
        } catch (JacksonException exception) {
            throw new IllegalStateException("가입 세션을 직렬화할 수 없습니다.");
        }
    }

    private SignupSession deserialize(String serializedSession) {
        try {
            SignupSession session = objectMapper.readValue(serializedSession, SignupSession.class);
            if (session == null) {
                throw new IllegalStateException("가입 세션이 올바르지 않습니다.");
            }
            return session;
        } catch (JacksonException exception) {
            throw new IllegalStateException("가입 세션이 올바르지 않습니다.");
        }
    }

    private String signupSessionKey(String signupJti) {
        return SIGNUP_SESSION_KEY_PREFIX + signupJti;
    }
}

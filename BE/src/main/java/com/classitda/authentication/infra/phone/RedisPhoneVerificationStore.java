package com.classitda.authentication.infra.phone;

import com.classitda.authentication.application.phone.PhoneVerificationStore;
import com.classitda.authentication.application.phone.PhoneVerificationState;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RedisPhoneVerificationStore implements PhoneVerificationStore {

    private static final String VERIFICATION_KEY_PREFIX = "signup:phone-verification:";
    private static final String ACTIVE_KEY_PREFIX = "signup:phone-active:";
    private static final String COOLDOWN_KEY_PREFIX = "signup:phone-cooldown:";

    private static final RedisScript<Long> SAVE_IF_COOLDOWN_EXPIRED_SCRIPT =
            RedisScript.of(
                    new ClassPathResource(
                            "redis/phone-verification/save-if-cooldown-expired.lua"
                    ),
                    Long.class
            );
    private static final RedisScript<Long> DELETE_IF_ACTIVE_SCRIPT =
            RedisScript.of(
                    new ClassPathResource(
                            "redis/phone-verification/delete-if-active.lua"
                    ),
                    Long.class
            );

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean saveIfCooldownExpired(
            PhoneVerificationState state,
            long verificationTtlSeconds,
            long cooldownTtlSeconds
    ) {
        Long result = redisTemplate.execute(
                SAVE_IF_COOLDOWN_EXPIRED_SCRIPT,
                List.of(
                        verificationKey(state.verificationId()),
                        activeKey(state.signupJti(), state.phoneHmac()),
                        cooldownKey(state.signupJti(), state.phoneHmac())),
                state.verificationId(),
                state.signupJti(),
                state.phoneNumber(),
                state.otpDigest(),
                String.valueOf(verificationTtlSeconds),
                String.valueOf(cooldownTtlSeconds),
                VERIFICATION_KEY_PREFIX);

        if (result == null) {
            throw new IllegalStateException("휴대전화 인증번호 발송 상태를 저장할 수 없습니다.");
        }
        if (result == 0L) {
            return true;
        }
        if (result == 1L) {
            return false;
        }
        throw new IllegalStateException("알 수 없는 휴대전화 인증번호 발송 처리 결과입니다.");
    }

    @Override
    public void deleteIfActive(PhoneVerificationState state) {
        Long result = redisTemplate.execute(
                DELETE_IF_ACTIVE_SCRIPT,
                List.of(
                        verificationKey(state.verificationId()),
                        activeKey(state.signupJti(), state.phoneHmac())),
                state.verificationId());

        if (result == null) {
            throw new IllegalStateException("휴대전화 인증번호 발송 상태를 정리할 수 없습니다.");
        }
        if (result != 0L && result != 1L) {
            throw new IllegalStateException("알 수 없는 휴대전화 인증번호 발송 정리 결과입니다.");
        }
    }

    private String verificationKey(String verificationId) {
        return VERIFICATION_KEY_PREFIX + verificationId;
    }

    private String activeKey(String signupJti, String phoneHmac) {
        return ACTIVE_KEY_PREFIX + signupJti + ":" + phoneHmac;
    }

    private String cooldownKey(String signupJti, String phoneHmac) {
        return COOLDOWN_KEY_PREFIX + signupJti + ":" + phoneHmac;
    }
}

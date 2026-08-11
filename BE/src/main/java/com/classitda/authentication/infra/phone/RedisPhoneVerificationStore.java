package com.classitda.authentication.infra.phone;

import com.classitda.authentication.application.phone.PhoneVerificationHasher;
import com.classitda.authentication.application.phone.PhoneVerificationStore;
import com.classitda.authentication.application.phone.PhoneVerificationState;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
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
    private static final String VERIFIED_PHONE_KEY_PREFIX = "signup:verified-phone:";

    private static final Pattern CANONICAL_PHONE_NUMBER_PATTERN = Pattern.compile("^\\+8210[0-9]{8}$");
    private static final Pattern OTP_DIGEST_PATTERN = Pattern.compile("^[0-9a-f]{64}$");

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
    private static final RedisScript<Long> CONFIRM_SCRIPT =
            RedisScript.of(
                    new ClassPathResource(
                            "redis/phone-verification/confirm.lua"
                    ),
                    Long.class
            );

    private final StringRedisTemplate redisTemplate;
    private final PhoneVerificationHasher phoneVerificationHasher;

    @Override
    public Optional<PhoneVerificationState> findByVerificationId(String verificationId) {
        Map<Object, Object> fields = redisTemplate.opsForHash().entries(verificationKey(verificationId));
        if (fields.isEmpty()) {
            return Optional.empty();
        }

        String signupJti = readRequiredField(fields, "signupJti");
        String phoneNumber = readRequiredField(fields, "phoneNumber");
        String otpDigest = readRequiredField(fields, "otpDigest");
        validateStateFieldFormats(phoneNumber, otpDigest);

        return Optional.of(
                new PhoneVerificationState(
                        verificationId,
                        signupJti,
                        phoneNumber,
                        otpDigest
                )
        );
    }

    @Override
    public boolean saveIfCooldownExpired(PhoneVerificationState state, long verificationTtlSeconds, long cooldownTtlSeconds) {
        String phoneHmac = phoneVerificationHasher.hashPhoneNumber(state.phoneNumber());
        Long result = redisTemplate.execute(
                SAVE_IF_COOLDOWN_EXPIRED_SCRIPT,
                List.of(
                        verificationKey(state.verificationId()),
                        activeKey(state.signupJti(), phoneHmac),
                        cooldownKey(state.signupJti(), phoneHmac)
                ),
                state.verificationId(),
                state.signupJti(),
                state.phoneNumber(),
                state.otpDigest(),
                String.valueOf(verificationTtlSeconds),
                String.valueOf(cooldownTtlSeconds),
                VERIFICATION_KEY_PREFIX
        );

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
    public ConfirmOutcome confirm(
            PhoneVerificationState state,
            boolean otpMatches,
            int maxAttempts,
            long verifiedPhoneTtlSeconds
    ) {
        String phoneHmac = phoneVerificationHasher.hashPhoneNumber(state.phoneNumber());
        Long result = redisTemplate.execute(
                CONFIRM_SCRIPT,
                List.of(
                        verificationKey(state.verificationId()),
                        activeKey(state.signupJti(), phoneHmac),
                        verifiedPhoneKey(state.signupJti())
                ),
                state.verificationId(),
                state.signupJti(),
                state.otpDigest(),
                otpMatches ? "1" : "0",
                String.valueOf(maxAttempts),
                String.valueOf(verifiedPhoneTtlSeconds)
        );

        if (result == null) {
            throw new IllegalStateException("휴대전화 인증번호를 확인할 수 없습니다.");
        }
        return switch (result.intValue()) {
            case 0 -> ConfirmOutcome.CONFIRMED;
            case 1 -> ConfirmOutcome.UNAVAILABLE;
            case 2 -> ConfirmOutcome.SESSION_MISMATCH;
            case 3 -> ConfirmOutcome.OTP_INVALID;
            case 4 -> ConfirmOutcome.ATTEMPTS_EXCEEDED;
            default -> throw new IllegalStateException("알 수 없는 휴대전화 인증번호 확인 결과입니다.");
        };
    }

    @Override
    public void deleteIfActive(PhoneVerificationState state) {
        String phoneHmac = phoneVerificationHasher.hashPhoneNumber(state.phoneNumber());
        Long result = redisTemplate.execute(
                DELETE_IF_ACTIVE_SCRIPT,
                List.of(
                        verificationKey(state.verificationId()),
                        activeKey(state.signupJti(), phoneHmac)),
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

    private String readRequiredField(Map<Object, Object> fields, String fieldName) {
        Object value = fields.get(fieldName);
        if (!(value instanceof String stringValue) || stringValue.isBlank()) {
            throw new IllegalStateException("휴대전화 인증번호 확인 상태가 올바르지 않습니다.");
        }
        return stringValue;
    }

    private void validateStateFieldFormats(String phoneNumber, String otpDigest) {
        if (!CANONICAL_PHONE_NUMBER_PATTERN.matcher(phoneNumber).matches()
                || !OTP_DIGEST_PATTERN.matcher(otpDigest).matches()) {
            throw new IllegalStateException("휴대전화 인증번호 확인 상태가 올바르지 않습니다.");
        }
    }

    private String activeKey(String signupJti, String phoneHmac) {
        return ACTIVE_KEY_PREFIX + signupJti + ":" + phoneHmac;
    }

    private String cooldownKey(String signupJti, String phoneHmac) {
        return COOLDOWN_KEY_PREFIX + signupJti + ":" + phoneHmac;
    }

    private String verifiedPhoneKey(String signupJti) {
        return VERIFIED_PHONE_KEY_PREFIX + signupJti;
    }
}

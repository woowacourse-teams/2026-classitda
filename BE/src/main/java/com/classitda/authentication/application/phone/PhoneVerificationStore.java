package com.classitda.authentication.application.phone;

import java.util.Optional;

public interface PhoneVerificationStore {

    enum ConfirmOutcome {
        CONFIRMED,
        UNAVAILABLE,
        SESSION_MISMATCH,
        OTP_INVALID,
        ATTEMPTS_EXCEEDED
    }

    Optional<PhoneVerificationState> findByVerificationId(String verificationId);

    boolean saveIfCooldownExpired(PhoneVerificationState state, long verificationTtlSeconds, long cooldownTtlSeconds);

    ConfirmOutcome confirm(PhoneVerificationState state, boolean otpMatches, int maxAttempts, long verifiedPhoneTtlSeconds);

    void deleteIfActive(PhoneVerificationState state);
}

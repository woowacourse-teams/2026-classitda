package com.classitda.authentication.application.phone;

public interface PhoneVerificationStore {

    boolean saveIfCooldownExpired(
            PhoneVerificationState state,
            long verificationTtlSeconds,
            long cooldownTtlSeconds
    );

    void deleteIfActive(PhoneVerificationState state);
}

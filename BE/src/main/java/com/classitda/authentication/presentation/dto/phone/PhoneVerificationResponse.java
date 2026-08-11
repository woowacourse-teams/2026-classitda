package com.classitda.authentication.presentation.dto.phone;

public record PhoneVerificationResponse(
        String verificationId,
        long expiresInSeconds,
        long resendAfterSeconds
) {

    public static PhoneVerificationResponse of(
            String verificationId,
            long expiresInSeconds,
            long resendAfterSeconds
    ) {
        return new PhoneVerificationResponse(verificationId, expiresInSeconds, resendAfterSeconds);
    }
}

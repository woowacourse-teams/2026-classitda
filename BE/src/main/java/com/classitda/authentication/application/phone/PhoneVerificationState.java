package com.classitda.authentication.application.phone;

public record PhoneVerificationState(
        String verificationId,
        String signupJti,
        String phoneNumber,
        String otpDigest
) {
}

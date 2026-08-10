package com.classitda.authentication.application.phone;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PhoneVerificationHasher {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final SecretKey phoneVerificationHmacKey;

    public String hashPhoneNumber(String phoneNumber) {
        return hash("phone:" + phoneNumber);
    }

    public String hashOtp(
            String signupJti,
            String verificationId,
            String phoneNumber,
            String otp
    ) {
        return hash("otp:" + signupJti + ":" + verificationId + ":" + phoneNumber + ":" + otp);
    }

    private String hash(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(phoneVerificationHmacKey);
            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException exception) {
            throw new IllegalStateException("휴대전화 인증 HMAC을 계산할 수 없습니다.", exception);
        }
    }
}

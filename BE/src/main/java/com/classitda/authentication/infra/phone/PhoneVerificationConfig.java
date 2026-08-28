package com.classitda.authentication.infra.phone;

import com.classitda.authentication.application.phone.OtpGenerator;
import com.classitda.authentication.infra.sms.SmsProperties;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@EnableConfigurationProperties(SmsProperties.class)
@Configuration
public class PhoneVerificationConfig {

    private static final int MINIMUM_HMAC_KEY_LENGTH_BYTES = 32;
    private static final int OTP_BOUND = 1_000_000;
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Pattern FIXED_OTP_PATTERN = Pattern.compile("^[0-9]{6}$");

    @Bean
    public SecretKey phoneVerificationHmacKey(
            @Value("${auth.phone.key-hmac-secret-base64:}") String encodedKey
    ) {
        if (encodedKey == null || encodedKey.isBlank()) {
            throw new IllegalArgumentException("휴대전화 인증 HMAC 키 설정은 필수입니다.");
        }

        byte[] decodedKey;
        try {
            decodedKey = Base64.getDecoder().decode(encodedKey);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("휴대전화 인증 HMAC 키 설정이 올바른 Base64 형식이 아닙니다.");
        }

        if (decodedKey.length < MINIMUM_HMAC_KEY_LENGTH_BYTES) {
            throw new IllegalArgumentException("휴대전화 인증 HMAC 키는 32바이트 이상이어야 합니다.");
        }
        return new SecretKeySpec(decodedKey, HMAC_ALGORITHM);
    }

    @Profile("local")
    @Bean
    public OtpGenerator localOtpGenerator(SmsProperties properties) {
        String fixedOtp = properties.fixedOtp();
        if (fixedOtp == null || !FIXED_OTP_PATTERN.matcher(fixedOtp).matches()) {
            throw new IllegalArgumentException("local SMS 고정 인증번호는 숫자 6자리여야 합니다.");
        }
        return properties::fixedOtp;
    }

    @Profile("!local")
    @Bean
    public OtpGenerator secureOtpGenerator(SmsProperties properties) {
        String fixedOtp = properties.fixedOtp();
        if (fixedOtp != null && !fixedOtp.isBlank()) {
            throw new IllegalArgumentException("non-local profile에서는 local SMS 고정 인증번호를 사용할 수 없습니다.");
        }
        SecureRandom secureRandom = new SecureRandom();
        return () -> String.format(Locale.ROOT, "%06d", secureRandom.nextInt(OTP_BOUND));
    }
}

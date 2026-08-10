package com.classitda.authentication.infra.sms;

import com.classitda.authentication.application.sms.SmsSender;
import java.util.regex.Pattern;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@EnableConfigurationProperties(SmsProperties.class)
@Configuration
public class SmsConfiguration {

    private static final Pattern FIXED_OTP_PATTERN = Pattern.compile("^[0-9]{6}$");

    @Profile("local")
    @Bean
    public SmsSender localSmsSender(SmsProperties properties) {
        String fixedOtp = properties.fixedOtp();
        if (fixedOtp == null || !FIXED_OTP_PATTERN.matcher(fixedOtp).matches()) {
            throw new IllegalArgumentException("local SMS 고정 인증번호는 숫자 6자리여야 합니다.");
        }
        return new LocalNoopSmsSender();
    }

    @Profile("!local")
    @Bean
    public SmsSender unavailableSmsSender(SmsProperties properties) {
        String fixedOtp = properties.fixedOtp();
        if (fixedOtp != null && !fixedOtp.isBlank()) {
            throw new IllegalArgumentException("non-local profile에서는 local SMS 고정 인증번호를 사용할 수 없습니다.");
        }
        return new UnavailableSmsSender();
    }
}

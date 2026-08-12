package com.classitda.authentication.infra.sms;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.sms.local")
public record SmsProperties(String fixedOtp) {
}

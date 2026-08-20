package com.classitda.authentication.infra.sms;

import java.util.regex.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.sms.sens")
public record NcpSensProperties(
        String serviceId,
        String accessKey,
        String secretKey,
        String senderNumber
) {

    private static final Pattern DIGITS_PATTERN = Pattern.compile("^[0-9]+$");

    public NcpSensProperties {
        requireNotBlank(serviceId, "NCP SENS SMS Service ID는 필수입니다.");
        requireNotBlank(accessKey, "NCP SENS Access Key는 필수입니다.");
        requireNotBlank(secretKey, "NCP SENS Secret Key는 필수입니다.");
        requireNotBlank(senderNumber, "NCP SENS 발신번호는 필수입니다.");
        if (!DIGITS_PATTERN.matcher(senderNumber).matches()) {
            throw new IllegalArgumentException("NCP SENS 발신번호는 숫자만 사용할 수 있습니다.");
        }
    }

    private static void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}

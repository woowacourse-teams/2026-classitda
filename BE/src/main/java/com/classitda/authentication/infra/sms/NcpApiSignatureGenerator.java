package com.classitda.authentication.infra.sms;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.http.HttpMethod;

public class NcpApiSignatureGenerator {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    public String generate(
            HttpMethod method,
            String uri,
            long timestamp,
            String accessKey,
            String secretKey
    ) {
        String message = method.name() + " " + uri + "\n" + timestamp + "\n" + accessKey;

        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] signature = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("NCP API 서명을 생성할 수 없습니다.", exception);
        }
    }
}

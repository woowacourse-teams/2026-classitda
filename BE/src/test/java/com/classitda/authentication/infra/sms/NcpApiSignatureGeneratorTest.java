package com.classitda.authentication.infra.sms;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

class NcpApiSignatureGeneratorTest {

    private final NcpApiSignatureGenerator signatureGenerator = new NcpApiSignatureGenerator();

    @Test
    void NCP_API_요청_정보를_HmacSHA256으로_서명한다() {
        // given
        String uri = "/sms/v2/services/ncp:sms:kr:test:sens/messages";
        long timestamp = 1_700_000_000_000L;
        String accessKey = "test-access-key";
        String secretKey = "test-secret-key";

        // when
        String signature = signatureGenerator.generate(
                HttpMethod.POST,
                uri,
                timestamp,
                accessKey,
                secretKey
        );

        // then
        assertThat(signature).isEqualTo("ryaUsxpU3DgFbG9i8d709vGcsCoto9J7J/YDYCpHgl0=");
    }
}

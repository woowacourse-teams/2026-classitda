package com.classitda.authentication.infra.sms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withAccepted;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;

import com.classitda.authentication.exception.AuthErrorCode;
import com.classitda.authentication.exception.AuthException;
import java.net.SocketTimeoutException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class NcpSensSmsSenderTest {

    private static final String SERVICE_ID = "ncp:sms:kr:test:sens";
    private static final String ACCESS_KEY = "test-access-key";
    private static final String SECRET_KEY = "test-secret-key";
    private static final String SENDER_NUMBER = "01012345678";
    private static final String RECEIVER_NUMBER = "01087654321";
    private static final String OTP = "123456";
    private static final long TIMESTAMP = 1_700_000_000_000L;
    private static final String REQUEST_URI = "/sms/v2/services/" + SERVICE_ID + "/messages";
    private static final String REQUEST_URL = NcpSensSmsSender.BASE_URL + REQUEST_URI;

    private MockRestServiceServer server;
    private NcpSensSmsSender smsSender;

    @BeforeEach
    void setUp() {
        RestClient.Builder restClientBuilder = RestClient.builder().baseUrl(NcpSensSmsSender.BASE_URL);
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
        smsSender = new NcpSensSmsSender(
                restClientBuilder.build(),
                new NcpSensProperties(SERVICE_ID, ACCESS_KEY, SECRET_KEY, SENDER_NUMBER),
                new NcpApiSignatureGenerator(),
                Clock.fixed(Instant.ofEpochMilli(TIMESTAMP), ZoneOffset.UTC)
        );
    }

    @Test
    void NCP_SENS에_인증번호_SMS_발송을_요청한다() {
        // given
        server.expect(requestTo(REQUEST_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(header("x-ncp-apigw-timestamp", String.valueOf(TIMESTAMP)))
                .andExpect(header("x-ncp-iam-access-key", ACCESS_KEY))
                .andExpect(header(
                        "x-ncp-apigw-signature-v2",
                        "ryaUsxpU3DgFbG9i8d709vGcsCoto9J7J/YDYCpHgl0="
                ))
                .andExpect(content().json("""
                        {
                          "type": "SMS",
                          "contentType": "COMM",
                          "countryCode": "82",
                          "from": "01012345678",
                          "content": "[클래스잇다] 인증번호는 123456입니다.\n 3분 안에 입력해 주세요.",
                          "messages": [
                            {"to": "01087654321"}
                          ]
                        }
                        """))
                .andRespond(withAccepted());

        // when
        smsSender.send(RECEIVER_NUMBER, OTP);

        // then
        server.verify();
    }

    @Test
    void NCP_SENS가_오류를_응답하면_문자_발송_실패로_변환한다() {
        // given
        server.expect(requestTo(REQUEST_URL))
                .andRespond(withServerError());

        // when
        Throwable exception = catchThrowable(() -> smsSender.send(RECEIVER_NUMBER, OTP));

        // then
        assertDeliveryFailed(exception);
        server.verify();
    }

    @Test
    void NCP_SENS_요청이_타임아웃되면_문자_발송_실패로_변환한다() {
        // given
        server.expect(requestTo(REQUEST_URL))
                .andRespond(withException(new SocketTimeoutException("test timeout")));

        // when
        Throwable exception = catchThrowable(() -> smsSender.send(RECEIVER_NUMBER, OTP));

        // then
        assertDeliveryFailed(exception);
        server.verify();
    }

    private void assertDeliveryFailed(Throwable exception) {
        assertThat(exception)
                .isInstanceOf(AuthException.class)
                .satisfies(throwable -> assertThat(((AuthException) throwable).getErrorCode())
                        .isEqualTo(AuthErrorCode.PHONE_DELIVERY_FAILED));
    }
}

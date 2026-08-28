package com.classitda.authentication.infra.sms;

import com.classitda.authentication.application.phone.SmsSender;
import com.classitda.authentication.exception.AuthErrorCode;
import com.classitda.authentication.exception.AuthException;
import java.time.Clock;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
public class NcpSensSmsSender implements SmsSender {

    public static final String BASE_URL = "https://sens.apigw.ntruss.com";

    private static final String MESSAGE_TEMPLATE = "[클래스잇다] 인증번호는 %s입니다.\n3분 안에 입력해주세요.";

    private final RestClient restClient;
    private final NcpSensProperties properties;
    private final NcpApiSignatureGenerator signatureGenerator;
    private final Clock clock;

    public NcpSensSmsSender(
            RestClient restClient,
            NcpSensProperties properties,
            NcpApiSignatureGenerator signatureGenerator,
            Clock clock
    ) {
        this.restClient = restClient;
        this.properties = properties;
        this.signatureGenerator = signatureGenerator;
        this.clock = clock;
    }

    @Override
    public void send(String phoneNumber, String otp) {
        String uri = "/sms/v2/services/" + properties.serviceId() + "/messages";
        long timestamp = clock.millis();
        String signature = signatureGenerator.generate(
                HttpMethod.POST,
                uri,
                timestamp,
                properties.accessKey(),
                properties.secretKey()
        );
        NcpSensSendRequest request = NcpSensSendRequest.of(
                properties.senderNumber(),
                phoneNumber,
                MESSAGE_TEMPLATE.formatted(otp)
        );

        try {
            restClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("x-ncp-apigw-timestamp", String.valueOf(timestamp))
                    .header("x-ncp-iam-access-key", properties.accessKey())
                    .header("x-ncp-apigw-signature-v2", signature)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            log.warn("NCP SENS SMS 발송 요청이 거부되었습니다. status={}", exception.getStatusCode().value());
            throw new AuthException(AuthErrorCode.PHONE_DELIVERY_FAILED);
        } catch (RestClientException exception) {
            log.warn("NCP SENS SMS 발송 요청을 완료하지 못했습니다. exceptionType={}", exception.getClass().getName());
            throw new AuthException(AuthErrorCode.PHONE_DELIVERY_FAILED);
        }
    }

    private record NcpSensSendRequest(
            String type,
            String contentType,
            String countryCode,
            String from,
            String content,
            List<NcpSensMessage> messages
    ) {

        private static NcpSensSendRequest of(String senderNumber, String receiverNumber, String content) {
            return new NcpSensSendRequest(
                    "SMS",
                    "COMM",
                    "82",
                    senderNumber,
                    content,
                    List.of(NcpSensMessage.from(receiverNumber))
            );
        }
    }

    private record NcpSensMessage(String to) {

        private static NcpSensMessage from(String receiverNumber) {
            return new NcpSensMessage(receiverNumber);
        }
    }
}

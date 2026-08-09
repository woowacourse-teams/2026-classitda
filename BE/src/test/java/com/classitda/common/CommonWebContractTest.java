package com.classitda.common;

import com.classitda.common.config.ApiVersionConfig;
import com.classitda.common.exception.ClassitdaException;
import com.classitda.common.exception.CommonErrorCode;
import com.classitda.common.exception.GlobalExceptionHandler;
import jakarta.validation.constraints.Positive;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AutoConfigureRestTestClient
@Import({ApiVersionConfig.class, GlobalExceptionHandler.class, CommonWebContractTest.TestController.class})
@WebMvcTest(controllers = CommonWebContractTest.TestController.class)
class CommonWebContractTest {

    private final RestTestClient client;

    @Autowired
    CommonWebContractTest(RestTestClient client) {
        this.client = client;
    }

    @Test
    void 버전_1_요청은_성공_DTO를_감싸지_않고_반환한다() {
        // given / when
        RestTestClient.ResponseSpec response = client.get()
                .uri("/test/success")
                .header("X-API-Version", "1")
                .exchange();

        // then
        response.expectStatus().isOk()
                .expectBody()
                .json("""
                        {"value":"ok"}
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 버전_헤더가_없으면_API_001을_반환한다() {
        // given / when
        RestTestClient.ResponseSpec response = client.get()
                .uri("/test/success")
                .exchange();

        // then
        assertError(response, 400, "API-001", "X-API-Version 헤더는 필수입니다.");
    }

    @Test
    void 지원하지_않는_버전은_API_002를_반환한다() {
        // given / when
        RestTestClient.ResponseSpec response = client.get()
                .uri("/test/success")
                .header("X-API-Version", "2")
                .exchange();

        // then
        assertError(response, 400, "API-002", "지원하지 않는 API 버전입니다.");
    }

    @Test
    void 형식이_잘못된_버전은_API_002를_반환한다() {
        // given / when
        RestTestClient.ResponseSpec response = client.get()
                .uri("/test/success")
                .header("X-API-Version", "not-a-version")
                .exchange();

        // then
        assertError(response, 400, "API-002", "지원하지 않는 API 버전입니다.");
    }

    @Test
    void 빈_검증값은_COMMON_001을_반환한다() {
        // given / when
        RestTestClient.ResponseSpec response = client.get()
                .uri(uriBuilder -> uriBuilder.path("/test/validated")
                        .queryParam("value", "0")
                        .build())
                .header("X-API-Version", "1")
                .exchange();

        // then
        assertError(response, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
    }

    @Test
    void 공통_예외는_자신의_코드와_상태를_반환한다() {
        // given / when
        RestTestClient.ResponseSpec response = client.get()
                .uri("/test/common-exception")
                .header("X-API-Version", "1")
                .exchange();

        // then
        assertError(response, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
    }

    @Test
    void 예상하지_못한_예외는_메시지를_노출하지_않고_COMMON_002를_반환한다() {
        // given / when
        RestTestClient.ResponseSpec response = client.get()
                .uri("/test/unexpected")
                .header("X-API-Version", "1")
                .exchange();

        // then
        assertError(response, 500, "COMMON-002", "서버 내부 오류가 발생했습니다.");
    }

    @Test
    void 존재하지_않는_API_경로는_COMMON_003을_반환한다() {
        // given / when
        RestTestClient.ResponseSpec response = client.get()
                .uri("/test/not-found")
                .header("X-API-Version", "1")
                .exchange();

        // then
        assertError(response, 404, "COMMON-003", "요청한 API를 찾을 수 없습니다.");
    }

    private void assertError(RestTestClient.ResponseSpec response, int status, String code, String message) {
        response.expectStatus().isEqualTo(status)
                .expectBody()
                .json("""
                        {"code":"%s","message":"%s"}
                        """.formatted(code, message), JsonCompareMode.STRICT);
    }

    @RestController
    @RequestMapping("/test")
    public static class TestController {

        @GetMapping(path = "/success", version = "1")
        public TestResponse success() {
            return TestResponse.from("ok");
        }

        @GetMapping(path = "/validated", version = "1")
        public TestResponse validated(
                @Positive @RequestParam int value
        ) {
            return TestResponse.from(String.valueOf(value));
        }

        @GetMapping(path = "/common-exception", version = "1")
        public TestResponse commonException() {
            throw new ClassitdaException(CommonErrorCode.INVALID_INPUT);
        }

        @GetMapping(path = "/unexpected", version = "1")
        public TestResponse unexpected() {
            throw new IllegalStateException("외부에 노출되면 안 되는 메시지");
        }
    }

    public record TestResponse(String value) {

        public static TestResponse from(String value) {
            return new TestResponse(value);
        }
    }
}

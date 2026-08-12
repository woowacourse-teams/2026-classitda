package com.classitda.authentication.presentation;

import static org.assertj.core.api.Assertions.assertThat;

import com.classitda.authentication.presentation.dto.signup.SignupRequest;
import com.classitda.authentication.presentation.dto.token.RefreshTokenRequest;
import com.classitda.authentication.presentation.dto.token.RefreshTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class AuthControllerApiTest {

    @Test
    void 토큰_갱신_OpenAPI는_필수_body와_정확한_성공_오류_계약만_문서화한다() throws NoSuchMethodException {
        // given
        java.lang.reflect.Method method = AuthControllerApi.class
                .getMethod("refreshToken", RefreshTokenRequest.class);
        Operation operation = method.getAnnotation(Operation.class);
        ApiResponses responses = method.getAnnotation(ApiResponses.class);

        // when
        ApiResponse success = response(responses, "200");
        ApiResponse invalidInput = response(responses, "400");
        ApiResponse invalidRefresh = response(responses, "401");
        ApiResponse internalFailure = response(responses, "500");

        // then
        assertThat(operation.requestBody().required()).isTrue();
        assertThat(operation.requestBody().content()).singleElement().satisfies(content -> {
            assertThat(content.mediaType()).isEqualTo("application/json");
            assertThat(content.schema().implementation()).isEqualTo(RefreshTokenRequest.class);
            assertThat(content.examples())
                    .extracting(ExampleObject::value)
                    .allSatisfy(value -> assertThat(value)
                            .containsOnlyOnce("\"refreshToken\"")
                            .doesNotContain("memberId", "sessionId", "tokenHash", "auth:refresh:"));
        });
        assertThat(success.content()).singleElement().satisfies(content -> {
            assertThat(content.schema().implementation()).isEqualTo(RefreshTokenResponse.class);
            assertThat(content.examples())
                    .extracting(ExampleObject::value)
                    .singleElement()
                    .satisfies(value -> assertThat(value)
                            .contains(
                                    "\"accessToken\"",
                                    "\"accessTokenExpiresIn\":900",
                                    "\"refreshToken\"",
                                    "\"refreshTokenExpiresIn\":2592000"
                            )
                            .doesNotContain("memberId", "sessionId", "tokenHash", "auth:refresh:"));
        });
        assertThat(exampleValues(invalidInput)).anyMatch(value -> value.contains("\"code\":\"COMMON-001\""));
        assertThat(exampleValues(invalidInput)).anyMatch(value -> value.contains("\"code\":\"API-001\""));
        assertThat(exampleValues(invalidInput)).anyMatch(value -> value.contains("\"code\":\"API-002\""));
        assertThat(exampleValues(invalidRefresh))
                .containsExactly("{\"code\":\"AUTH-008\",\"message\":\"리프레시 토큰이 유효하지 않습니다.\"}");
        assertThat(exampleValues(internalFailure))
                .containsExactly("{\"code\":\"COMMON-002\",\"message\":\"서버 내부 오류가 발생했습니다.\"}");
        assertThat(Arrays.stream(responses.value()).map(ApiResponse::responseCode))
                .containsExactlyInAnyOrder("200", "400", "401", "500");
    }

    @Test
    void 회원가입_OpenAPI는_동일_소셜_성공과_전화번호_충돌만_문서화하고_AUTH_008은_노출하지_않는다()
            throws NoSuchMethodException {
        // given
        ApiResponses signupResponses = AuthControllerApi.class
                .getMethod("completeSignup", Jwt.class, SignupRequest.class)
                .getAnnotation(ApiResponses.class);
        ApiResponse created = response(signupResponses, "201");
        ApiResponse conflict = response(signupResponses, "409");
        ApiResponse internalServerError = response(signupResponses, "500");

        // when / then
        assertThat(exampleValues(created))
                .anySatisfy(value -> assertThat(value)
                        .contains("\"accessToken\"", "\"refreshToken\""));
        assertThat(exampleValues(conflict))
                .anySatisfy(value -> assertThat(value).contains("\"code\":\"PHONE-001\""));
        assertThat(Arrays.stream(signupResponses.value())
                .flatMap(response -> Arrays.stream(response.content()))
                .flatMap(content -> Arrays.stream(content.examples()))
                .toList()).allSatisfy(example -> {
                    assertThat(example.name())
                            .doesNotContain("AUTH-008", "SOCIAL_ACCOUNT_ALREADY_REGISTERED");
                    assertThat(example.value())
                            .doesNotContain("AUTH-008", "이미 가입된 소셜 계정입니다.");
                });
        assertThat(internalServerError.content()).singleElement().satisfies(content ->
                assertThat(content.examples())
                        .extracting(ExampleObject::value)
                        .anySatisfy(value -> assertThat(value).contains("\"code\":\"COMMON-002\""))
        );
    }

    private java.util.List<String> exampleValues(ApiResponse response) {
        return Arrays.stream(response.content())
                .flatMap(content -> Arrays.stream(content.examples()))
                .map(ExampleObject::value)
                .toList();
    }

    private ApiResponse response(ApiResponses responses, String status) {
        return Arrays.stream(responses.value())
                .filter(response -> response.responseCode().equals(status))
                .findFirst()
                .orElseThrow();
    }
}

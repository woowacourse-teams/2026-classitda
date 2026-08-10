package com.classitda.authentication.presentation;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.classitda.authentication.application.SocialLoginService;
import com.classitda.authentication.application.token.IssuedLoginTokens;
import com.classitda.authentication.application.token.IssuedSignupToken;
import com.classitda.authentication.infra.security.AuthenticationErrorHandler;
import com.classitda.authentication.infra.security.SecurityConfig;
import com.classitda.authentication.infra.security.jwt.JwtAuthenticationConverter;
import com.classitda.authentication.presentation.dto.GoogleLoginRequest;
import com.classitda.authentication.presentation.dto.LoginResponse;
import com.classitda.common.config.ApiVersionConfig;
import com.classitda.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.client.RestTestClient;

@Import({
        ApiVersionConfig.class,
        GlobalExceptionHandler.class,
        SecurityConfig.class,
        AuthenticationErrorHandler.class,
        JwtAuthenticationConverter.class,
        AuthControllerTest.TestSecurityConfiguration.class
})
@AutoConfigureRestTestClient
@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest {

    private static final String ID_TOKEN = "google-id-token";

    private final RestTestClient client;

    @MockitoBean
    private SocialLoginService socialLoginService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    AuthControllerTest(RestTestClient client) {
        this.client = client;
    }

    @Test
    void 미가입_구글_계정은_가입_토큰_필드만_반환한다() {
        // given
        GoogleLoginRequest request = GoogleLoginRequest.from(ID_TOKEN);
        LoginResponse response = LoginResponse.registrationRequired(IssuedSignupToken.of("signup-token", 1800L));
        given(socialLoginService.loginWithGoogle(request)).willReturn(response);

        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/auth/google")
                .header("X-API-Version", "1")
                .body(request)
                .exchange();

        // then
        result.expectStatus().isOk()
                .expectBody()
                .json("""
                        {
                          "status": "REGISTRATION_REQUIRED",
                          "signupToken": "signup-token",
                          "signupTokenExpiresIn": 1800
                        }
                        """, JsonCompareMode.STRICT);
        verify(socialLoginService).loginWithGoogle(request);
    }

    @Test
    void 기존_구글_계정은_로그인_토큰_필드만_반환한다() {
        // given
        GoogleLoginRequest request = GoogleLoginRequest.from(ID_TOKEN);
        LoginResponse response = LoginResponse.registered(
                IssuedLoginTokens.of("access-token", 900L, "refresh-token", 2592000L));
        given(socialLoginService.loginWithGoogle(request)).willReturn(response);

        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/auth/google")
                .header("X-API-Version", "1")
                .body(request)
                .exchange();

        // then
        result.expectStatus().isOk()
                .expectBody()
                .json("""
                        {
                          "status": "REGISTERED",
                          "accessToken": "access-token",
                          "accessTokenExpiresIn": 900,
                          "refreshToken": "refresh-token",
                          "refreshTokenExpiresIn": 2592000
                        }
                        """, JsonCompareMode.STRICT);
        verify(socialLoginService).loginWithGoogle(request);
    }

    @Test
    void 빈_구글_ID_토큰은_COMMON_001을_반환한다() {
        // given / when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/auth/google")
                .header("X-API-Version", "1")
                .body(GoogleLoginRequest.from(" "))
                .exchange();

        // then
        assertError(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
        verifyNoInteractions(socialLoginService);
    }

    @Test
    void 버전_헤더가_없으면_API_001을_반환한다() {
        // given / when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/auth/google")
                .body(GoogleLoginRequest.from(ID_TOKEN))
                .exchange();

        // then
        assertError(result, 400, "API-001", "X-API-Version 헤더는 필수입니다.");
        verifyNoInteractions(socialLoginService);
    }

    @Test
    void 지원하지_않는_버전은_API_002를_반환한다() {
        // given / when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/auth/google")
                .header("X-API-Version", "2")
                .body(GoogleLoginRequest.from(ID_TOKEN))
                .exchange();

        // then
        assertError(result, 400, "API-002", "지원하지 않는 API 버전입니다.");
        verifyNoInteractions(socialLoginService);
    }

    private void assertError(
            RestTestClient.ResponseSpec response,
            int status,
            String code,
            String message
    ) {
        response.expectStatus().isEqualTo(status)
                .expectBody()
                .json("""
                        {"code":"%s","message":"%s"}
                        """.formatted(code, message), JsonCompareMode.STRICT);
    }

    @EnableWebSecurity
    @TestConfiguration(proxyBeanMethods = false)
    static class TestSecurityConfiguration {
    }
}

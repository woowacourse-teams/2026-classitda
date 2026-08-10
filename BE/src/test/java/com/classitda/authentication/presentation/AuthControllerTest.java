package com.classitda.authentication.presentation;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.classitda.authentication.application.SocialLoginService;
import com.classitda.authentication.application.phone.PhoneVerificationService;
import com.classitda.authentication.application.token.IssuedLoginTokens;
import com.classitda.authentication.application.token.IssuedSignupToken;
import com.classitda.authentication.domain.TokenUse;
import com.classitda.authentication.infra.security.AuthenticationErrorHandler;
import com.classitda.authentication.infra.security.SecurityConfig;
import com.classitda.authentication.infra.security.jwt.JwtAuthenticationConverter;
import com.classitda.authentication.presentation.dto.GoogleLoginRequest;
import com.classitda.authentication.presentation.dto.LoginResponse;
import com.classitda.authentication.presentation.dto.PhoneVerificationResponse;
import com.classitda.authentication.presentation.dto.PhoneVerificationSendRequest;
import com.classitda.common.config.ApiVersionConfig;
import com.classitda.common.exception.GlobalExceptionHandler;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.Jwt;
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
    private PhoneVerificationService phoneVerificationService;

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

    @Test
    void 가입_토큰으로_휴대전화_인증번호를_발송하면_201과_엄격한_응답을_반환한다() {
        // given
        PhoneVerificationSendRequest request = PhoneVerificationSendRequest.from("+821012345678");
        PhoneVerificationResponse applicationResult = PhoneVerificationResponse.of("verification-id", 180L, 60L);
        given(jwtDecoder.decode("signup-token")).willReturn(jwt("signup-jti", TokenUse.SIGNUP));
        given(phoneVerificationService.send("signup-jti", request.phoneNumber())).willReturn(applicationResult);

        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/auth/phone-verifications")
                .header("X-API-Version", "1")
                .header("Authorization", "Bearer signup-token")
                .body(request)
                .exchange();

        // then
        result.expectStatus().isCreated()
                .expectBody()
                .json("""
                        {
                          "verificationId": "verification-id",
                          "expiresInSeconds": 180,
                          "resendAfterSeconds": 60
                        }
                        """, JsonCompareMode.STRICT);
        verify(phoneVerificationService).send("signup-jti", request.phoneNumber());
    }

    @Test
    void 올바르지_않은_휴대전화_번호들은_COMMON_001을_반환한다() {
        // given
        given(jwtDecoder.decode("signup-token")).willReturn(jwt("signup-jti", TokenUse.SIGNUP));
        String[] invalidPhoneNumbers = {" ", "01012345678", "+8210-1234-5678", "+8210 1234 5678", "+12025550123"};

        // when / then
        for (String invalidPhoneNumber : invalidPhoneNumbers) {
            RestTestClient.ResponseSpec result = client.post()
                    .uri("/api/auth/phone-verifications")
                    .header("X-API-Version", "1")
                    .header("Authorization", "Bearer signup-token")
                    .body(PhoneVerificationSendRequest.from(invalidPhoneNumber))
                    .exchange();

            assertError(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
        }
        verifyNoInteractions(phoneVerificationService);
    }

    @Test
    void 휴대전화_인증번호_발송에서_버전_헤더가_없으면_API_001을_반환한다() {
        // given
        given(jwtDecoder.decode("signup-token")).willReturn(jwt("signup-jti", TokenUse.SIGNUP));

        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/auth/phone-verifications")
                .header("Authorization", "Bearer signup-token")
                .body(PhoneVerificationSendRequest.from("+821012345678"))
                .exchange();

        // then
        assertError(result, 400, "API-001", "X-API-Version 헤더는 필수입니다.");
        verifyNoInteractions(phoneVerificationService);
    }

    @Test
    void 휴대전화_인증번호_발송에서_인증이_없으면_AUTH_001을_반환한다() {
        // given / when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/auth/phone-verifications")
                .header("X-API-Version", "1")
                .body(PhoneVerificationSendRequest.from("+821012345678"))
                .exchange();

        // then
        assertError(result, 401, "AUTH-001", "인증이 필요합니다.");
        verifyNoInteractions(phoneVerificationService);
    }

    @Test
    void 액세스_토큰으로_휴대전화_인증번호를_발송하면_AUTH_002를_반환한다() {
        // given
        given(jwtDecoder.decode("access-token")).willReturn(jwt("access-jti", TokenUse.ACCESS));

        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/auth/phone-verifications")
                .header("X-API-Version", "1")
                .header("Authorization", "Bearer access-token")
                .body(PhoneVerificationSendRequest.from("+821012345678"))
                .exchange();

        // then
        assertError(result, 403, "AUTH-002", "접근 권한이 없습니다.");
        verifyNoInteractions(phoneVerificationService);
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

    private Jwt jwt(String jti, TokenUse tokenUse) {
        Instant issuedAt = Instant.now();
        return Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .subject(jti)
                .claim("jti", jti)
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plusSeconds(1800))
                .claim("token_use", tokenUse.name())
                .build();
    }

    @EnableWebSecurity
    @TestConfiguration(proxyBeanMethods = false)
    static class TestSecurityConfiguration {
    }
}

package com.classitda.authentication.presentation;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.classitda.authentication.application.SocialLoginService;
import com.classitda.authentication.application.phone.PhoneVerificationService;
import com.classitda.authentication.application.token.IssuedLoginTokens;
import com.classitda.authentication.application.token.IssuedSignupToken;
import com.classitda.authentication.domain.TokenUse;
import com.classitda.authentication.exception.AuthErrorCode;
import com.classitda.authentication.exception.AuthException;
import com.classitda.authentication.infra.security.AuthenticationErrorHandler;
import com.classitda.authentication.infra.security.SecurityConfig;
import com.classitda.authentication.infra.security.jwt.JwtAuthenticationConverter;
import com.classitda.authentication.presentation.dto.GoogleLoginRequest;
import com.classitda.authentication.presentation.dto.LoginResponse;
import com.classitda.authentication.presentation.dto.PhoneVerificationConfirmRequest;
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
    private static final String VERIFICATION_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String OTP = "123456";

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

    @Test
    void 가입_토큰으로_휴대전화_인증번호를_확인하면_204와_빈_body를_반환한다() {
        // given
        given(jwtDecoder.decode("signup-token")).willReturn(jwt("signup-jti", TokenUse.SIGNUP));

        // when
        RestTestClient.ResponseSpec result = confirm("signup-token", VERIFICATION_ID, OTP, "1");

        // then
        result.expectStatus().isNoContent()
                .expectBody().isEmpty();
        verify(phoneVerificationService).confirm("signup-jti", VERIFICATION_ID, OTP);
    }

    @Test
    void canonical_UUID가_아닌_인증_요청_ID들은_COMMON_001을_반환한다() {
        // given
        given(jwtDecoder.decode("signup-token")).willReturn(jwt("signup-jti", TokenUse.SIGNUP));
        String[] invalidVerificationIds = {
                "not-a-uuid",
                "550E8400-E29B-41D4-A716-446655440000",
                "550e8400-e29b-01d4-a716-446655440000",
                "550e8400-e29b-41d4-c716-446655440000"
        };

        // when / then
        for (String invalidVerificationId : invalidVerificationIds) {
            RestTestClient.ResponseSpec result = confirm("signup-token", invalidVerificationId, OTP, "1");

            assertError(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
        }
        verifyNoInteractions(phoneVerificationService);
    }

    @Test
    void 올바르지_않은_휴대전화_인증번호들은_COMMON_001을_반환한다() {
        // given
        given(jwtDecoder.decode("signup-token")).willReturn(jwt("signup-jti", TokenUse.SIGNUP));
        String[] invalidOtps = {"", " ", "12345", "1234567", "12345a"};

        // when / then
        for (String invalidOtp : invalidOtps) {
            RestTestClient.ResponseSpec result = confirm("signup-token", VERIFICATION_ID, invalidOtp, "1");

            assertError(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
        }
        verifyNoInteractions(phoneVerificationService);
    }

    @Test
    void 휴대전화_인증번호_확인에서_버전_헤더가_없으면_API_001을_반환한다() {
        // given
        given(jwtDecoder.decode("signup-token")).willReturn(jwt("signup-jti", TokenUse.SIGNUP));

        // when
        RestTestClient.ResponseSpec result = confirm("signup-token", VERIFICATION_ID, OTP, null);

        // then
        assertError(result, 400, "API-001", "X-API-Version 헤더는 필수입니다.");
        verifyNoInteractions(phoneVerificationService);
    }

    @Test
    void 휴대전화_인증번호_확인에서_지원하지_않는_버전은_API_002를_반환한다() {
        // given
        given(jwtDecoder.decode("signup-token")).willReturn(jwt("signup-jti", TokenUse.SIGNUP));

        // when
        RestTestClient.ResponseSpec result = confirm("signup-token", VERIFICATION_ID, OTP, "2");

        // then
        assertError(result, 400, "API-002", "지원하지 않는 API 버전입니다.");
        verifyNoInteractions(phoneVerificationService);
    }

    @Test
    void 휴대전화_인증번호_확인에서_인증이_없으면_AUTH_001을_반환한다() {
        // given / when
        RestTestClient.ResponseSpec result = confirm(null, VERIFICATION_ID, OTP, "1");

        // then
        assertError(result, 401, "AUTH-001", "인증이 필요합니다.");
        verifyNoInteractions(phoneVerificationService);
    }

    @Test
    void 액세스_토큰으로_휴대전화_인증번호를_확인하면_AUTH_002를_반환한다() {
        // given
        given(jwtDecoder.decode("access-token")).willReturn(jwt("access-jti", TokenUse.ACCESS));

        // when
        RestTestClient.ResponseSpec result = confirm("access-token", VERIFICATION_ID, OTP, "1");

        // then
        assertError(result, 403, "AUTH-002", "접근 권한이 없습니다.");
        verifyNoInteractions(phoneVerificationService);
    }

    @Test
    void 사용할_수_없는_휴대전화_인증_요청은_PHONE_003을_반환한다() {
        // given
        given(jwtDecoder.decode("signup-token")).willReturn(jwt("signup-jti", TokenUse.SIGNUP));
        willThrow(new AuthException(AuthErrorCode.PHONE_VERIFICATION_UNAVAILABLE))
                .given(phoneVerificationService).confirm("signup-jti", VERIFICATION_ID, OTP);

        // when
        RestTestClient.ResponseSpec result = confirm("signup-token", VERIFICATION_ID, OTP, "1");

        // then
        assertError(result, 410, "PHONE-003", "인증 요청이 만료되었거나 이미 처리되어 유효하지 않습니다.");
    }

    @Test
    void 잘못된_휴대전화_인증번호는_PHONE_004를_반환한다() {
        // given
        given(jwtDecoder.decode("signup-token")).willReturn(jwt("signup-jti", TokenUse.SIGNUP));
        willThrow(new AuthException(AuthErrorCode.PHONE_OTP_INVALID))
                .given(phoneVerificationService).confirm("signup-jti", VERIFICATION_ID, OTP);

        // when
        RestTestClient.ResponseSpec result = confirm("signup-token", VERIFICATION_ID, OTP, "1");

        // then
        assertError(result, 400, "PHONE-004", "인증번호가 올바르지 않습니다.");
    }

    @Test
    void 휴대전화_인증번호_오답_한도를_넘으면_PHONE_005를_반환한다() {
        // given
        given(jwtDecoder.decode("signup-token")).willReturn(jwt("signup-jti", TokenUse.SIGNUP));
        willThrow(new AuthException(AuthErrorCode.PHONE_OTP_ATTEMPTS_EXCEEDED))
                .given(phoneVerificationService).confirm("signup-jti", VERIFICATION_ID, OTP);

        // when
        RestTestClient.ResponseSpec result = confirm("signup-token", VERIFICATION_ID, OTP, "1");

        // then
        assertError(result, 429, "PHONE-005", "인증번호 입력 가능 횟수를 초과했습니다. 다시 인증해 주세요.");
    }

    @Test
    void 다른_가입_세션의_휴대전화_인증_요청은_PHONE_006을_반환한다() {
        // given
        given(jwtDecoder.decode("signup-token")).willReturn(jwt("signup-jti", TokenUse.SIGNUP));
        willThrow(new AuthException(AuthErrorCode.PHONE_VERIFICATION_SESSION_MISMATCH))
                .given(phoneVerificationService).confirm("signup-jti", VERIFICATION_ID, OTP);

        // when
        RestTestClient.ResponseSpec result = confirm("signup-token", VERIFICATION_ID, OTP, "1");

        // then
        assertError(result, 403, "PHONE-006", "현재 가입 세션의 인증 요청이 아닙니다.");
    }

    private RestTestClient.ResponseSpec confirm(
            String token,
            String verificationId,
            String otp,
            String apiVersion
    ) {
        RestTestClient.RequestBodySpec request = client.post()
                .uri("/api/auth/phone-verifications/{verificationId}/confirm", verificationId);
        if (apiVersion != null) {
            request.header("X-API-Version", apiVersion);
        }
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return request.body(PhoneVerificationConfirmRequest.from(otp)).exchange();
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

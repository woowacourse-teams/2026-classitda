package com.classitda.authentication.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.classitda.authentication.application.LogoutService;
import com.classitda.authentication.application.RefreshTokenService;
import com.classitda.authentication.application.SignupService;
import com.classitda.authentication.application.SocialLoginService;
import com.classitda.authentication.application.phone.PhoneVerificationService;
import com.classitda.authentication.application.token.result.IssuedAccessToken;
import com.classitda.authentication.application.token.result.IssuedLoginTokens;
import com.classitda.authentication.application.token.result.IssuedRefreshToken;
import com.classitda.authentication.application.token.result.IssuedSignupToken;
import com.classitda.authentication.domain.OauthProvider;
import com.classitda.authentication.domain.TokenUse;
import com.classitda.authentication.exception.AuthErrorCode;
import com.classitda.authentication.exception.AuthException;
import com.classitda.authentication.infra.security.AuthenticationErrorHandler;
import com.classitda.authentication.infra.security.SecurityConfig;
import com.classitda.authentication.infra.security.jwt.JwtAuthenticationConverter;
import com.classitda.authentication.presentation.config.AuthenticationWebMvcConfig;
import com.classitda.authentication.presentation.dto.login.LoginResponse;
import com.classitda.authentication.presentation.dto.login.SocialLoginRequest;
import com.classitda.authentication.presentation.dto.logout.LogoutRequest;
import com.classitda.authentication.presentation.dto.phone.PhoneVerificationConfirmRequest;
import com.classitda.authentication.presentation.dto.phone.PhoneVerificationResponse;
import com.classitda.authentication.presentation.dto.phone.PhoneVerificationSendRequest;
import com.classitda.authentication.presentation.dto.signup.SignupRequest;
import com.classitda.authentication.presentation.dto.signup.SignupResponse;
import com.classitda.authentication.presentation.dto.token.RefreshTokenRequest;
import com.classitda.authentication.presentation.dto.token.LoginTokenResponse;
import com.classitda.authentication.presentation.resolver.CurrentMemberIdArgumentResolver;
import com.classitda.common.config.ApiVersionConfig;
import com.classitda.common.exception.ClassitdaException;
import com.classitda.common.exception.GlobalExceptionHandler;
import com.classitda.member.exception.MemberErrorCode;
import com.classitda.member.exception.MemberException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
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
        CurrentMemberIdArgumentResolver.class,
        AuthenticationWebMvcConfig.class,
        AuthControllerTest.TestSecurityConfiguration.class
})
@AutoConfigureRestTestClient
@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest {

    private static final String ID_TOKEN = "google-id-token";
    private static final String RAW_NONCE = "A".repeat(43);
    private static final String VERIFICATION_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String OTP = "123456";
    private static final String REFRESH_TOKEN =
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA.BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB";
    private static final String ROTATED_REFRESH_TOKEN =
            "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC.DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD";

    private final RestTestClient client;

    @MockitoBean
    private SocialLoginService socialLoginService;

    @MockitoBean
    private PhoneVerificationService phoneVerificationService;

    @MockitoBean
    private SignupService signupService;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @MockitoBean
    private LogoutService logoutService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    AuthControllerTest(RestTestClient client) {
        this.client = client;
    }

    @Test
    void 미가입_구글_계정은_가입_토큰_필드만_반환한다() {
        // given
        SocialLoginRequest request = SocialLoginRequest.of(ID_TOKEN, RAW_NONCE);
        LoginResponse response = LoginResponse.registrationRequired(IssuedSignupToken.of("signup-token", 1800L));
        given(socialLoginService.loginWithSocial(OauthProvider.GOOGLE, ID_TOKEN, RAW_NONCE)).willReturn(response);

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
        verify(socialLoginService).loginWithSocial(OauthProvider.GOOGLE, ID_TOKEN, RAW_NONCE);
    }

    @Test
    void 기존_구글_계정은_로그인_토큰_필드만_반환한다() {
        // given
        SocialLoginRequest request = SocialLoginRequest.of(ID_TOKEN, RAW_NONCE);
        LoginResponse response = LoginResponse.registered(
                IssuedLoginTokens.of("access-token", 3_600L, "refresh-token", 2592000L));
        given(socialLoginService.loginWithSocial(OauthProvider.GOOGLE, ID_TOKEN, RAW_NONCE)).willReturn(response);

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
                          "accessTokenExpiresIn": 3600,
                          "refreshToken": "refresh-token",
                          "refreshTokenExpiresIn": 2592000
                        }
                        """, JsonCompareMode.STRICT);
        verify(socialLoginService).loginWithSocial(OauthProvider.GOOGLE, ID_TOKEN, RAW_NONCE);
    }

    @Test
    void 애플_로그인은_공통_엔드포인트에서_APPLE_제공자로_처리한다() {
        // given
        String appleIdToken = "apple-id-token";
        SocialLoginRequest request = SocialLoginRequest.of(appleIdToken, RAW_NONCE);
        LoginResponse response = LoginResponse.registrationRequired(IssuedSignupToken.of("signup-token", 1800L));
        given(socialLoginService.loginWithSocial(OauthProvider.APPLE, appleIdToken, RAW_NONCE)).willReturn(response);

        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/auth/apple")
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
        verify(socialLoginService).loginWithSocial(OauthProvider.APPLE, appleIdToken, RAW_NONCE);
    }

    @Test
    void 탈퇴_처리_중인_구글_계정은_AUTH_009를_반환한다() {
        // given
        SocialLoginRequest request = SocialLoginRequest.of(ID_TOKEN, RAW_NONCE);
        given(socialLoginService.loginWithSocial(OauthProvider.GOOGLE, ID_TOKEN, RAW_NONCE))
                .willThrow(new AuthException(AuthErrorCode.MEMBER_WITHDRAWAL_PENDING));

        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/auth/google")
                .header("X-API-Version", "1")
                .body(request)
                .exchange();

        // then
        assertError(result, 403, "AUTH-009", "탈퇴 처리 중인 계정입니다.");
    }

    @Test
    void 빈_구글_ID_토큰은_COMMON_001을_반환한다() {
        // given / when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/auth/google")
                .header("X-API-Version", "1")
                .body(SocialLoginRequest.of(" ", RAW_NONCE))
                .exchange();

        // then
        assertError(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
        verifyNoInteractions(socialLoginService);
    }

    @Test
    void rawNonce가_누락되거나_공백이거나_형식이_잘못되면_COMMON_001을_반환한다() {
        // given
        String[] invalidBodies = {
                "{\"idToken\":\"google-id-token\"}",
                "{\"idToken\":\"google-id-token\",\"rawNonce\":null}",
                "{\"idToken\":\"google-id-token\",\"rawNonce\":\"\"}",
                "{\"idToken\":\"google-id-token\",\"rawNonce\":\" \"}",
                "{\"idToken\":\"google-id-token\",\"rawNonce\":\"%s\"}".formatted("A".repeat(42)),
                "{\"idToken\":\"google-id-token\",\"rawNonce\":\"%s\"}".formatted("A".repeat(44)),
                "{\"idToken\":\"google-id-token\",\"rawNonce\":\"%s+\"}".formatted("A".repeat(42))
        };

        // when / then
        for (String invalidBody : invalidBodies) {
            RestTestClient.ResponseSpec result = client.post()
                    .uri("/api/auth/google")
                    .header("X-API-Version", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(invalidBody)
                    .exchange();

            assertError(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
        }
        verifyNoInteractions(socialLoginService);
    }



    @Test
    void 인증_헤더_없이_리프레시_토큰을_갱신하면_200과_엄격한_회전_응답을_반환한다() {
        // given
        RefreshTokenRequest request = RefreshTokenRequest.from(REFRESH_TOKEN);
        LoginTokenResponse response = LoginTokenResponse.of(
                IssuedAccessToken.of("access-token", 3_600L),
                IssuedRefreshToken.of(
                        ROTATED_REFRESH_TOKEN,
                        "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                        "b".repeat(64)
                ),
                2_592_000L
        );
        given(refreshTokenService.refresh(request)).willReturn(response);

        // when
        RestTestClient.ResponseSpec result = refresh("1", request);

        // then
        result.expectStatus().isOk()
                .expectBody()
                .json("""
                        {
                          "accessToken": "access-token",
                          "accessTokenExpiresIn": 3600,
                          "refreshToken": "%s",
                          "refreshTokenExpiresIn": 2592000
                        }
                        """.formatted(ROTATED_REFRESH_TOKEN), JsonCompareMode.STRICT);
        verify(refreshTokenService).refresh(request);
    }

    @Test
    void 리프레시_요청_body가_없거나_null_blank이면_COMMON_001이고_서비스를_호출하지_않는다() {
        // given / when
        RestTestClient.ResponseSpec missingBody = client.post()
                .uri("/api/auth/tokens/refresh")
                .header("X-API-Version", "1")
                .exchange();
        String[] invalidBodies = {
                "{}",
                "{\"refreshToken\":null}",
                "{\"refreshToken\":\"\"}",
                "{\"refreshToken\":\" \"}"
        };

        // then
        assertError(missingBody, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
        for (String invalidBody : invalidBodies) {
            assertError(
                    refresh("1", invalidBody),
                    400,
                    "COMMON-001",
                    "요청 값이 올바르지 않습니다."
            );
        }
        verifyNoInteractions(refreshTokenService);
    }

    @Test
    void 리프레시_토큰의_구분자_segment_길이_문자_전체길이가_잘못되면_COMMON_001이다() {
        // given
        String[] invalidTokens = {
                "A".repeat(43),
                "A".repeat(43) + "." + "B".repeat(43) + "." + "C".repeat(43),
                "A".repeat(42) + "." + "B".repeat(43),
                "A".repeat(44) + "." + "B".repeat(43),
                "+" + "A".repeat(42) + "." + "B".repeat(43),
                REFRESH_TOKEN + "C"
        };

        // when / then
        for (String invalidToken : invalidTokens) {
            assertError(
                    refresh("1", RefreshTokenRequest.from(invalidToken)),
                    400,
                    "COMMON-001",
                    "요청 값이 올바르지 않습니다."
            );
        }
        verifyNoInteractions(refreshTokenService);
    }


    @Test
    void 유효하지_않은_리프레시_토큰은_AUTH_008을_반환하고_원문을_노출하지_않는다() {
        // given
        RefreshTokenRequest request = RefreshTokenRequest.from(REFRESH_TOKEN);
        given(refreshTokenService.refresh(request))
                .willThrow(new AuthException(AuthErrorCode.REFRESH_TOKEN_INVALID));

        // when
        RestTestClient.ResponseSpec result = refresh("1", request);

        // then
        assertError(result, 401, "AUTH-008", "리프레시 토큰이 유효하지 않습니다.");
    }

    @Test
    void 리프레시_내부_오류는_COMMON_002의_고정_응답만_반환한다() {
        // given
        RefreshTokenRequest request = RefreshTokenRequest.from(REFRESH_TOKEN);
        given(refreshTokenService.refresh(request))
                .willThrow(new IllegalStateException("리프레시 토큰 갱신 중 내부 오류가 발생했습니다."));

        // when
        RestTestClient.ResponseSpec result = refresh("1", request);

        // then
        assertError(result, 500, "COMMON-002", "서버 내부 오류가 발생했습니다.");
    }

    @Test
    void 액세스_토큰과_현재_리프레시_토큰으로_로그아웃하면_엄격한_빈_204를_반환한다() {
        // given
        LogoutRequest request = LogoutRequest.from(REFRESH_TOKEN);
        given(jwtDecoder.decode("access-token")).willReturn(jwt("42", TokenUse.ACCESS));

        // when
        RestTestClient.ResponseSpec result = logout("access-token", "1", request);

        // then
        result.expectStatus().isNoContent().expectBody().isEmpty();
        verify(logoutService).logout(42L, request);
    }


    @Test
    void 로그아웃에서_인증이_없거나_유효하지_않으면_AUTH_001이고_서비스를_호출하지_않는다() {
        // given
        LogoutRequest request = LogoutRequest.from(REFRESH_TOKEN);
        given(jwtDecoder.decode("invalid-token")).willThrow(new BadJwtException("invalid token"));

        // when
        RestTestClient.ResponseSpec missing = logout(null, "1", request);
        RestTestClient.ResponseSpec invalid = logout("invalid-token", "1", request);

        // then
        assertError(missing, 401, "AUTH-001", "인증이 필요합니다.");
        assertError(invalid, 401, "AUTH-001", "인증이 필요합니다.");
        verifyNoInteractions(logoutService);
    }

    @Test
    void 가입_토큰으로_로그아웃하면_AUTH_002이고_서비스를_호출하지_않는다() {
        // given
        given(jwtDecoder.decode("signup-token")).willReturn(jwt("signup-jti", TokenUse.SIGNUP));

        // when
        RestTestClient.ResponseSpec result = logout(
                "signup-token",
                "1",
                LogoutRequest.from(REFRESH_TOKEN)
        );

        // then
        assertError(result, 403, "AUTH-002", "접근 권한이 없습니다.");
        verifyNoInteractions(logoutService);
    }

    @Test
    void 숫자가_아닌_회원_subject의_액세스_토큰으로_로그아웃하면_COMMON_002이고_서비스를_호출하지_않는다() {
        // given
        given(jwtDecoder.decode("malformed-subject-token"))
                .willReturn(jwt("sensitive-member-subject", TokenUse.ACCESS));

        // when
        RestTestClient.ResponseSpec result = logout(
                "malformed-subject-token",
                "1",
                LogoutRequest.from(REFRESH_TOKEN)
        );

        // then
        result.expectStatus().isEqualTo(500)
                .expectBody()
                .consumeWith(response -> assertThat(new String(response.getResponseBody()))
                        .isEqualTo("{\"code\":\"COMMON-002\",\"message\":\"서버 내부 오류가 발생했습니다.\"}")
                        .doesNotContain("malformed-subject-token", "sensitive-member-subject"));
        verifyNoInteractions(logoutService);
    }

    @Test
    void 로그아웃_body가_없거나_null_blank이면_COMMON_001이고_서비스를_호출하지_않는다() {
        // given
        given(jwtDecoder.decode("access-token")).willReturn(jwt("42", TokenUse.ACCESS));
        String[] invalidBodies = {
                "{",
                "null",
                "{}",
                "{\"refreshToken\":null}",
                "{\"refreshToken\":\"\"}",
                "{\"refreshToken\":\" \"}"
        };

        // when
        RestTestClient.ResponseSpec missingBody = client.post()
                .uri("/api/auth/logout")
                .header("X-API-Version", "1")
                .header("Authorization", "Bearer access-token")
                .exchange();

        // then
        assertError(missingBody, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
        for (String invalidBody : invalidBodies) {
            assertError(logout("access-token", "1", invalidBody), 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
        }
        verifyNoInteractions(logoutService);
    }

    @Test
    void 로그아웃_토큰_형식이_잘못되면_COMMON_001이고_서비스를_호출하지_않는다() {
        // given
        given(jwtDecoder.decode("access-token")).willReturn(jwt("42", TokenUse.ACCESS));
        String[] invalidTokens = {
                "A".repeat(43),
                "A".repeat(42) + "." + "B".repeat(43),
                "A".repeat(43) + "." + "B".repeat(44),
                "+" + "A".repeat(42) + "." + "B".repeat(43),
                REFRESH_TOKEN + ".C"
        };

        // when / then
        for (String invalidToken : invalidTokens) {
            assertError(
                    logout("access-token", "1", LogoutRequest.from(invalidToken)),
                    400,
                    "COMMON-001",
                    "요청 값이 올바르지 않습니다."
            );
        }
        verifyNoInteractions(logoutService);
    }

    @Test
    void 로그아웃_내부_오류는_민감정보_없는_COMMON_002의_고정_응답만_반환한다() {
        // given
        LogoutRequest request = LogoutRequest.from(REFRESH_TOKEN);
        given(jwtDecoder.decode("access-token")).willReturn(jwt("42", TokenUse.ACCESS));
        willThrow(new IllegalStateException("sensitive-tokenHash-sessionId-memberId-auth:refresh:"))
                .given(logoutService).logout(42L, request);

        // when
        RestTestClient.ResponseSpec result = logout("access-token", "1", request);

        // then
        result.expectStatus().isEqualTo(500)
                .expectBody()
                .consumeWith(response -> assertThat(new String(response.getResponseBody()))
                        .isEqualTo("{\"code\":\"COMMON-002\",\"message\":\"서버 내부 오류가 발생했습니다.\"}")
                        .doesNotContain(
                                REFRESH_TOKEN,
                                "access-token",
                                "tokenHash",
                                "sessionId",
                                "memberId",
                                "auth:refresh:"
                        ));
    }

    @Test
    void 가입_토큰으로_휴대전화_인증번호를_발송하면_201과_엄격한_응답을_반환한다() {
        // given
        PhoneVerificationSendRequest request = PhoneVerificationSendRequest.from("01012345678");
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
        String[] invalidPhoneNumbers = {" ", "01112345678", "0101234567", "010-1234-5678", "+821012345678"};

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
    void 휴대전화_인증번호_발송에서_인증이_없으면_AUTH_001을_반환한다() {
        // given / when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/auth/phone-verifications")
                .header("X-API-Version", "1")
                .body(PhoneVerificationSendRequest.from("01012345678"))
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
                .body(PhoneVerificationSendRequest.from("01012345678"))
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

    @Test
    void 가입_토큰으로_회원가입을_완료하면_JTI를_위임하고_201과_엄격한_토큰_응답을_반환한다() {
        // given
        SignupRequest request = SignupRequest.of("홍길동", List.of(1L, 2L));
        SignupResponse response = SignupResponse.from(
                IssuedLoginTokens.of("access-token", 3_600L, "refresh-token", 2592000L)
        );
        given(jwtDecoder.decode("signup-token")).willReturn(jwt("signup-jti", TokenUse.SIGNUP));
        given(signupService.complete("signup-jti", request)).willReturn(response);

        // when
        RestTestClient.ResponseSpec result = signup("signup-token", "1", request);

        // then
        result.expectStatus().isCreated()
                .expectBody()
                .json("""
                        {
                          "accessToken": "access-token",
                          "accessTokenExpiresIn": 3600,
                          "refreshToken": "refresh-token",
                          "refreshTokenExpiresIn": 2592000
                        }
                        """, JsonCompareMode.STRICT);
        verify(signupService).complete("signup-jti", request);
    }

    @Test
    void 클라이언트가_전화번호와_소셜정보와_JTI를_보내도_가입_DTO에는_반영되지_않는다() {
        // given
        SignupRequest expectedRequest = SignupRequest.of("홍길동", List.of(1L, 2L));
        SignupResponse response = SignupResponse.from(
                IssuedLoginTokens.of("access-token", 3_600L, "refresh-token", 2592000L)
        );
        given(jwtDecoder.decode("signup-token")).willReturn(jwt("server-jti", TokenUse.SIGNUP));
        given(signupService.complete("server-jti", expectedRequest)).willReturn(response);
        String maliciousRequest = """
                {
                  "name": "홍길동",
                  "agreedTermIds": [1, 2],
                  "signupJti": "client-jti",
                  "phoneNumber": "01099999999",
                  "provider": "GOOGLE",
                  "providerSubject": "client-subject",
                  "providerEmail": "client@example.com"
                }
                """;

        // when
        RestTestClient.ResponseSpec result = signup("signup-token", "1", maliciousRequest);

        // then
        result.expectStatus().isCreated();
        verify(signupService).complete("server-jti", expectedRequest);
    }

    @Test
    void 회원가입_이름이_비어있거나_50자를_초과하면_COMMON_001이고_서비스를_호출하지_않는다() {
        // given
        given(jwtDecoder.decode("signup-token")).willReturn(jwt("signup-jti", TokenUse.SIGNUP));
        String[] invalidNames = {"", " ", "가".repeat(51)};

        // when / then
        for (String invalidName : invalidNames) {
            RestTestClient.ResponseSpec result = signup(
                    "signup-token",
                    "1",
                    SignupRequest.of(invalidName, List.of(1L))
            );

            assertError(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
        }
        verifyNoInteractions(signupService);
    }

    @Test
    void 회원가입_약관_목록이_없거나_원소가_양수가_아니면_COMMON_001이고_서비스를_호출하지_않는다() {
        // given
        given(jwtDecoder.decode("signup-token")).willReturn(jwt("signup-jti", TokenUse.SIGNUP));
        String[] invalidBodies = {
                "{\"name\":\"홍길동\",\"agreedTermIds\":null}",
                "{\"name\":\"홍길동\",\"agreedTermIds\":[]}",
                "{\"name\":\"홍길동\",\"agreedTermIds\":[null]}",
                "{\"name\":\"홍길동\",\"agreedTermIds\":[0]}",
                "{\"name\":\"홍길동\",\"agreedTermIds\":[-1]}"
        };

        // when / then
        for (String invalidBody : invalidBodies) {
            RestTestClient.ResponseSpec result = signup("signup-token", "1", invalidBody);

            assertError(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
        }
        verifyNoInteractions(signupService);
    }


    @Test
    void 회원가입에서_인증이_없거나_유효하지_않으면_AUTH_001이고_서비스를_호출하지_않는다() {
        // given
        SignupRequest request = SignupRequest.of("홍길동", List.of(1L, 2L));
        given(jwtDecoder.decode("invalid-token")).willThrow(new BadJwtException("invalid token"));

        // when
        RestTestClient.ResponseSpec missing = signup(null, "1", request);
        RestTestClient.ResponseSpec invalid = signup("invalid-token", "1", request);

        // then
        assertError(missing, 401, "AUTH-001", "인증이 필요합니다.");
        assertError(invalid, 401, "AUTH-001", "인증이 필요합니다.");
        verifyNoInteractions(signupService);
    }

    @Test
    void 액세스_토큰으로_회원가입하면_AUTH_002이고_서비스를_호출하지_않는다() {
        // given
        given(jwtDecoder.decode("access-token")).willReturn(jwt("access-jti", TokenUse.ACCESS));

        // when
        RestTestClient.ResponseSpec result = signup(
                "access-token",
                "1",
                SignupRequest.of("홍길동", List.of(1L, 2L))
        );

        // then
        assertError(result, 403, "AUTH-002", "접근 권한이 없습니다.");
        verifyNoInteractions(signupService);
    }

    @Test
    void 회원가입_제품_오류는_정해진_code와_status로_반환한다() {
        // given
        SignupRequest request = SignupRequest.of("홍길동", List.of(1L, 2L));
        given(jwtDecoder.decode("signup-token")).willReturn(jwt("signup-jti", TokenUse.SIGNUP));
        ClassitdaException[] exceptions = {
                new AuthException(AuthErrorCode.VERIFIED_PHONE_UNAVAILABLE),
                new AuthException(AuthErrorCode.PHONE_ALREADY_REGISTERED),
                new MemberException(MemberErrorCode.REQUIRED_TERM_AGREEMENT_MISSING),
                new MemberException(MemberErrorCode.TERM_NOT_FOUND),
                new MemberException(MemberErrorCode.TERM_ID_DUPLICATED),
                new MemberException(MemberErrorCode.TERM_STALE)
        };
        int[] statuses = {410, 409, 400, 400, 400, 409};
        String[] codes = {"PHONE-008", "PHONE-001", "TERM-001", "TERM-002", "TERM-003", "TERM-004"};
        String[] messages = {
                "인증이 완료된 휴대전화 번호가 없거나 만료되었습니다.",
                "이미 가입된 휴대전화 번호입니다.",
                "필수 약관에 모두 동의해야 합니다.",
                "존재하지 않는 약관이 포함되어 있습니다.",
                "중복된 약관 ID가 포함되어 있습니다.",
                "약관이 변경되었습니다. 최신 약관을 다시 확인해 주세요."
        };

        // when / then
        for (int index = 0; index < exceptions.length; index++) {
            willThrow(exceptions[index])
                    .given(signupService).complete("signup-jti", request);
            RestTestClient.ResponseSpec result = signup("signup-token", "1", request);

            assertError(result, statuses[index], codes[index], messages[index]);
        }
    }

    private RestTestClient.ResponseSpec signup(
            String token,
            String apiVersion,
            Object body
    ) {
        RestTestClient.RequestBodySpec request = client.post().uri("/api/auth/signup");
        if (apiVersion != null) {
            request.header("X-API-Version", apiVersion);
        }
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        if (body instanceof String) {
            request.contentType(MediaType.APPLICATION_JSON);
        }
        return request.body(body).exchange();
    }

    private RestTestClient.ResponseSpec refresh(String apiVersion, Object body) {
        RestTestClient.RequestBodySpec request = client.post().uri("/api/auth/tokens/refresh");
        if (apiVersion != null) {
            request.header("X-API-Version", apiVersion);
        }
        if (body instanceof String) {
            request.contentType(MediaType.APPLICATION_JSON);
        }
        return request.body(body).exchange();
    }

    private RestTestClient.ResponseSpec logout(
            String token,
            String apiVersion,
            Object body
    ) {
        RestTestClient.RequestBodySpec request = client.post().uri("/api/auth/logout");
        if (apiVersion != null) {
            request.header("X-API-Version", apiVersion);
        }
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        if (body instanceof String) {
            request.contentType(MediaType.APPLICATION_JSON);
        }
        return request.body(body).exchange();
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

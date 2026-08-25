package com.classitda.authentication.presentation;

import com.classitda.authentication.presentation.annotation.CurrentMemberId;
import com.classitda.authentication.presentation.dto.login.GoogleLoginRequest;
import com.classitda.authentication.presentation.dto.login.LoginResponse;
import com.classitda.authentication.presentation.dto.login.RegisteredLoginResponse;
import com.classitda.authentication.presentation.dto.login.RegistrationRequiredLoginResponse;
import com.classitda.authentication.presentation.dto.logout.LogoutRequest;
import com.classitda.authentication.presentation.dto.phone.PhoneVerificationConfirmRequest;
import com.classitda.authentication.presentation.dto.phone.PhoneVerificationResponse;
import com.classitda.authentication.presentation.dto.phone.PhoneVerificationSendRequest;
import com.classitda.authentication.presentation.dto.signup.SignupRequest;
import com.classitda.authentication.presentation.dto.signup.SignupResponse;
import com.classitda.authentication.presentation.dto.token.RefreshTokenRequest;
import com.classitda.authentication.presentation.dto.token.LoginTokenResponse;
import com.classitda.common.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

@Tag(name = "인증", description = "소셜 로그인, 인증 토큰 발급·폐기, 휴대전화 인증과 회원가입 API")
public interface AuthControllerApi {

    @Operation(
            summary = "Google 로그인",
            description = "Google ID 토큰을 검증합니다. 가입된 회원에게는 Access Token과 Refresh Token을, "
                    + "가입하지 않은 사용자에게는 회원가입에 사용할 Signup Token을 발급합니다.",
            requestBody = @RequestBody(
                    required = true,
                    description = "Google에서 발급받은 ID 토큰",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GoogleLoginRequest.class),
                            examples = @ExampleObject(value = "{\"idToken\":\"google-id-token\"}")
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Google 로그인 처리 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(oneOf = {
                                    RegisteredLoginResponse.class,
                                    RegistrationRequiredLoginResponse.class
                            }),
                            examples = {
                                    @ExampleObject(
                                            name = "REGISTERED",
                                            summary = "가입된 회원",
                                            value = "{\"status\":\"REGISTERED\",\"accessToken\":\"access-token\","
                                                    + "\"accessTokenExpiresIn\":3600,\"refreshToken\":\"refresh-token\","
                                                    + "\"refreshTokenExpiresIn\":2592000}"
                                    ),
                                    @ExampleObject(
                                            name = "REGISTRATION_REQUIRED",
                                            summary = "회원가입이 필요한 사용자",
                                            value = "{\"status\":\"REGISTRATION_REQUIRED\","
                                                    + "\"signupToken\":\"signup-token\","
                                                    + "\"signupTokenExpiresIn\":1800}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값 또는 API 버전이 올바르지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "INVALID_INPUT",
                                            value = "{\"code\":\"COMMON-001\",\"message\":\"요청 값이 올바르지 않습니다.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "API_VERSION_REQUIRED",
                                            value = "{\"code\":\"API-001\",\"message\":\"X-API-Version 헤더는 필수입니다.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "API_VERSION_UNSUPPORTED",
                                            value = "{\"code\":\"API-002\",\"message\":\"지원하지 않는 API 버전입니다.\"}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Google ID 토큰이 유효하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"code\":\"AUTH-006\",\"message\":\"Google ID 토큰이 유효하지 않습니다.\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "가입된 계정이 탈퇴 처리 중임",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"code\":\"AUTH-009\",\"message\":\"탈퇴 처리 중인 계정입니다.\"}"
                            )
                    )
            )
    })
    LoginResponse loginWithGoogle(
            @Valid GoogleLoginRequest request
    );

    @Operation(
            summary = "로그인 토큰 갱신",
            description = "유효한 Refresh Token을 한 번 사용해 새 Access Token과 Refresh Token을 발급합니다. "
                    + "성공한 요청의 기존 Refresh Token은 다시 사용할 수 없습니다.",
            requestBody = @RequestBody(
                    required = true,
                    description = "로그인 시 발급받은 opaque Refresh Token",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RefreshTokenRequest.class),
                            examples = @ExampleObject(
                                    value = "{\"refreshToken\":"
                                            + "\"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA."
                                            + "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB\"}"
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Access Token과 Refresh Token 회전 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginTokenResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"accessToken\":\"access-token\",\"accessTokenExpiresIn\":3600,"
                                            + "\"refreshToken\":\"new-refresh-token\","
                                            + "\"refreshTokenExpiresIn\":2592000}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값 또는 API 버전이 올바르지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "INVALID_INPUT",
                                            value = "{\"code\":\"COMMON-001\","
                                                    + "\"message\":\"요청 값이 올바르지 않습니다.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "API_VERSION_REQUIRED",
                                            value = "{\"code\":\"API-001\","
                                                    + "\"message\":\"X-API-Version 헤더는 필수입니다.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "API_VERSION_UNSUPPORTED",
                                            value = "{\"code\":\"API-002\","
                                                    + "\"message\":\"지원하지 않는 API 버전입니다.\"}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Refresh Token이 만료, 위조, 소비되었거나 회원이 탈퇴 처리 중이어서 유효하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"code\":\"AUTH-008\","
                                            + "\"message\":\"리프레시 토큰이 유효하지 않습니다.\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "리프레시 세션 또는 토큰 발급 내부 처리 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"code\":\"COMMON-002\","
                                            + "\"message\":\"서버 내부 오류가 발생했습니다.\"}"
                            )
                    )
            )
    })
    LoginTokenResponse refreshToken(
            @Valid RefreshTokenRequest request
    );

    @Operation(
            summary = "현재 기기 로그아웃",
            description = "인증된 회원의 현재 기기에서 제출한 Refresh Token에 대응하는 현재 로그인 세션만 "
                    + "폐기합니다. 세션이 없거나 만료되었거나 제출한 인증 정보와 일치하지 않아도 멱등하게 "
                    + "완료하며 다른 기기의 로그인 세션은 폐기하지 않습니다.\n\n"
                    + "Access Token 차단 목록을 사용하지 "
                    + "않으므로 이미 발급된 Access Token은 최대 1시간 동안 자연 만료 전까지 유효할 수 있습니다.\n\n"
                    + "클라이언트는 서버 응답의 성공 또는 실패 여부와 관계없이 로컬 Access Token, Refresh Token, "
                    + "기기 내 민감정보를 반드시 삭제해야 합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @RequestBody(
                    required = true,
                    description = "현재 기기 로그인에 사용 중인 opaque Refresh Token",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LogoutRequest.class),
                            examples = @ExampleObject(
                                    value = "{\"refreshToken\":"
                                            + "\"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA."
                                            + "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB\"}"
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "서버 로그아웃 처리 또는 멱등 처리 완료. 응답 본문 없음"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값 또는 API 버전이 올바르지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "INVALID_INPUT",
                                            value = "{\"code\":\"COMMON-001\","
                                                    + "\"message\":\"요청 값이 올바르지 않습니다.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "API_VERSION_REQUIRED",
                                            value = "{\"code\":\"API-001\","
                                                    + "\"message\":\"X-API-Version 헤더는 필수입니다.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "API_VERSION_UNSUPPORTED",
                                            value = "{\"code\":\"API-002\","
                                                    + "\"message\":\"지원하지 않는 API 버전입니다.\"}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Access Token이 없거나 유효하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"code\":\"AUTH-001\",\"message\":\"인증이 필요합니다.\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access Token이 아닌 인증 토큰을 사용함",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"code\":\"AUTH-002\",\"message\":\"접근 권한이 없습니다.\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "로그인 세션이 손상되었거나 내부 처리에 실패함",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"code\":\"COMMON-002\","
                                            + "\"message\":\"서버 내부 오류가 발생했습니다.\"}"
                            )
                    )
            )
    })
    ResponseEntity<Void> logout(
            @Parameter(hidden = true) @CurrentMemberId Long memberId,
            @Valid LogoutRequest request
    );

    @Operation(
            summary = "휴대전화 인증번호 발송",
            description = "Signup Token 사용자의 canonical 휴대전화 번호로 인증번호를 발송합니다. "
                    + "같은 가입 사용자와 번호 조합은 60초 후 재발송할 수 있습니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @RequestBody(
                    required = true,
                    description = "010으로 시작하는 숫자 11자리 휴대전화 번호",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PhoneVerificationSendRequest.class),
                            examples = @ExampleObject(value = "{\"phoneNumber\":\"01012345678\"}")
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "인증번호 발송 접수 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PhoneVerificationResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"verificationId\":\"550e8400-e29b-41d4-a716-446655440000\","
                                            + "\"expiresInSeconds\":180,\"resendAfterSeconds\":60}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값 또는 API 버전이 올바르지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "INVALID_INPUT",
                                            value = "{\"code\":\"COMMON-001\","
                                                    + "\"message\":\"요청 값이 올바르지 않습니다.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "API_VERSION_REQUIRED",
                                            value = "{\"code\":\"API-001\","
                                                    + "\"message\":\"X-API-Version 헤더는 필수입니다.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "API_VERSION_UNSUPPORTED",
                                            value = "{\"code\":\"API-002\","
                                                    + "\"message\":\"지원하지 않는 API 버전입니다.\"}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Signup Token이 없거나 유효하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"code\":\"AUTH-001\",\"message\":\"인증이 필요합니다.\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Signup Token이 아닌 인증 토큰을 사용함",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"code\":\"AUTH-002\",\"message\":\"접근 권한이 없습니다.\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 가입된 휴대전화 번호",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"code\":\"PHONE-001\","
                                            + "\"message\":\"이미 가입된 휴대전화 번호입니다.\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "재발송 대기",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "RESEND_COOLDOWN",
                                    value = "{\"code\":\"PHONE-002\","
                                            + "\"message\":\"인증번호 재발송은 잠시 후 다시 시도해 주세요.\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "SMS provider 미연동 또는 발송 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"code\":\"PHONE-007\","
                                            + "\"message\":\"문자 인증번호를 발송할 수 없습니다.\"}"
                            )
                    )
            )
    })
    ResponseEntity<PhoneVerificationResponse> sendPhoneVerification(
            @Parameter(hidden = true) Jwt signupJwt,
            @Valid PhoneVerificationSendRequest request
    );

    @Operation(
            summary = "휴대전화 인증번호 확인",
            description = "Signup Token에 속한 휴대전화 인증 요청의 6자리 인증번호를 확인합니다. "
                    + "성공한 인증 요청은 다시 사용할 수 없습니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @RequestBody(
                    required = true,
                    description = "문자로 전달받은 6자리 인증번호",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PhoneVerificationConfirmRequest.class),
                            examples = @ExampleObject(value = "{\"otp\":\"123456\"}")
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "휴대전화 인증 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값, API 버전 또는 인증번호가 올바르지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "INVALID_INPUT",
                                            value = "{\"code\":\"COMMON-001\","
                                                    + "\"message\":\"요청 값이 올바르지 않습니다.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "API_VERSION_REQUIRED",
                                            value = "{\"code\":\"API-001\","
                                                    + "\"message\":\"X-API-Version 헤더는 필수입니다.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "API_VERSION_UNSUPPORTED",
                                            value = "{\"code\":\"API-002\","
                                                    + "\"message\":\"지원하지 않는 API 버전입니다.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "OTP_INVALID",
                                            value = "{\"code\":\"PHONE-004\","
                                                    + "\"message\":\"인증번호가 올바르지 않습니다.\"}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Signup Token이 없거나 유효하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"code\":\"AUTH-001\",\"message\":\"인증이 필요합니다.\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Signup Token이 아니거나 다른 가입 세션의 인증 요청임",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "ACCESS_DENIED",
                                            value = "{\"code\":\"AUTH-002\",\"message\":\"접근 권한이 없습니다.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "SESSION_MISMATCH",
                                            value = "{\"code\":\"PHONE-006\","
                                                    + "\"message\":\"현재 가입 세션의 인증 요청이 아닙니다.\"}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "410",
                    description = "인증 요청이 만료되었거나 유효하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"code\":\"PHONE-003\","
                                            + "\"message\":\"인증 요청이 만료되었거나 이미 처리되어 유효하지 않습니다.\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "인증번호 입력 가능 횟수 초과",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"code\":\"PHONE-005\","
                                            + "\"message\":\"인증번호 입력 가능 횟수를 초과했습니다. 다시 인증해 주세요.\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "휴대전화 인증 상태가 손상되었거나 내부 처리 결과가 올바르지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"code\":\"COMMON-002\","
                                            + "\"message\":\"서버 내부 오류가 발생했습니다.\"}"
                            )
                    )
            )
    })
    ResponseEntity<Void> confirmPhoneVerification(
            @Parameter(hidden = true) Jwt signupJwt,
            @Parameter(
                    required = true,
                    description = "휴대전화 인증 요청 UUID",
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")
            String verificationId,
            @Valid PhoneVerificationConfirmRequest request
    );

    @Operation(
            summary = "회원가입 완료",
            description = "Signup Token의 가입 세션을 사용해 신규 회원 계정을 생성하거나 동일한 소셜 계정의 "
                    + "기존 회원 로그인을 완료하고 Access Token과 Refresh Token을 발급합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @RequestBody(
                    required = true,
                    description = "회원 이름과 동의한 최신 약관 row ID 목록",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SignupRequest.class),
                            examples = @ExampleObject(value = "{\"name\":\"홍길동\",\"agreedTermIds\":[1,2]}")
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "회원가입 완료 및 로그인 토큰 발급 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SignupResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"accessToken\":\"access-token\",\"accessTokenExpiresIn\":3600,"
                                            + "\"refreshToken\":\"refresh-token\","
                                            + "\"refreshTokenExpiresIn\":2592000}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값, 약관 동의 또는 API 버전이 올바르지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "INVALID_INPUT",
                                            value = "{\"code\":\"COMMON-001\","
                                                    + "\"message\":\"요청 값이 올바르지 않습니다.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "API_VERSION_REQUIRED",
                                            value = "{\"code\":\"API-001\","
                                                    + "\"message\":\"X-API-Version 헤더는 필수입니다.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "API_VERSION_UNSUPPORTED",
                                            value = "{\"code\":\"API-002\","
                                                    + "\"message\":\"지원하지 않는 API 버전입니다.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "REQUIRED_TERM_MISSING",
                                            value = "{\"code\":\"TERM-001\","
                                                    + "\"message\":\"필수 약관에 모두 동의해야 합니다.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "TERM_NOT_FOUND",
                                            value = "{\"code\":\"TERM-002\","
                                                    + "\"message\":\"존재하지 않는 약관이 포함되어 있습니다.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "TERM_ID_DUPLICATED",
                                            value = "{\"code\":\"TERM-003\","
                                                    + "\"message\":\"중복된 약관 ID가 포함되어 있습니다.\"}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Signup Token 또는 활성 가입 세션이 없거나 유효하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"code\":\"AUTH-001\",\"message\":\"인증이 필요합니다.\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Signup Token이 아닌 인증 토큰을 사용함",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"code\":\"AUTH-002\",\"message\":\"접근 권한이 없습니다.\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 가입된 전화번호 또는 변경된 약관",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "PHONE_ALREADY_REGISTERED",
                                            value = "{\"code\":\"PHONE-001\","
                                                    + "\"message\":\"이미 가입된 휴대전화 번호입니다.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "TERM_STALE",
                                            value = "{\"code\":\"TERM-004\","
                                                    + "\"message\":\"약관이 변경되었습니다. 최신 약관을 다시 확인해 주세요.\"}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "410",
                    description = "인증 완료 휴대전화 번호가 없거나 만료됨",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"code\":\"PHONE-008\","
                                            + "\"message\":\"인증이 완료된 휴대전화 번호가 없거나 만료되었습니다.\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "가입 상태 손상, 알 수 없는 데이터 무결성 오류, 토큰 또는 내부 처리 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"code\":\"COMMON-002\","
                                            + "\"message\":\"서버 내부 오류가 발생했습니다.\"}"
                            )
                    )
            )
    })
    ResponseEntity<SignupResponse> completeSignup(
            @Parameter(hidden = true) Jwt signupJwt,
            @Valid SignupRequest request
    );
}

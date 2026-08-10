package com.classitda.authentication.presentation;

import com.classitda.authentication.presentation.dto.GoogleLoginRequest;
import com.classitda.authentication.presentation.dto.LoginResponse;
import com.classitda.authentication.presentation.dto.PhoneVerificationResponse;
import com.classitda.authentication.presentation.dto.PhoneVerificationSendRequest;
import com.classitda.authentication.presentation.dto.RegisteredLoginResponse;
import com.classitda.authentication.presentation.dto.RegistrationRequiredLoginResponse;
import com.classitda.common.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

@Tag(name = "인증", description = "소셜 로그인, 인증 토큰 발급과 휴대전화 인증 API")
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
                                                    + "\"accessTokenExpiresIn\":900,\"refreshToken\":\"refresh-token\","
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
            )
    })
    LoginResponse loginWithGoogle(GoogleLoginRequest request);

    @Operation(
            summary = "휴대전화 인증번호 발송",
            description = "Signup Token 사용자의 canonical 휴대전화 번호로 인증번호를 발송합니다. "
                    + "같은 가입 사용자와 번호 조합은 60초 후 재발송할 수 있습니다.",
            requestBody = @RequestBody(
                    required = true,
                    description = "+8210으로 시작하는 canonical 휴대전화 번호",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PhoneVerificationSendRequest.class),
                            examples = @ExampleObject(value = "{\"phoneNumber\":\"+821012345678\"}")
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
            PhoneVerificationSendRequest request
    );
}
